package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.entity.*;
import com.ct.wms.event.NotificationEvent;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.utils.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 申请Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyMapper applyMapper;
    private final ApplyDetailMapper applyDetailMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final OutboundService outboundService;
    private final InventoryService inventoryService;
    private final OrderNoGenerator orderNoGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Apply> listApplies(Integer pageNum, Integer pageSize, Long warehouseId,
                                   Integer status, String startDate, String endDate,
                                   Long applicantId, Long approverId, String keyword) {
        Page<Apply> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();

        // 部门管理员只能查看本部门的申请（数据隔离）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUser = (UserDetailsImpl) auth.getPrincipal();
            Role currentRole = roleMapper.selectById(currentUser.getRoleId());
            if (currentRole != null && "DEPT_ADMIN".equals(currentRole.getRoleCode())) {
                User currentUserEntity = userMapper.selectById(currentUser.getId());
                if (currentUserEntity != null && currentUserEntity.getDeptId() != null) {
                    wrapper.eq(Apply::getDeptId, currentUserEntity.getDeptId());
                }
            }
        }

        if (warehouseId != null) {
            wrapper.eq(Apply::getWarehouseId, warehouseId);
        }

        if (status != null) {
            wrapper.eq(Apply::getStatus, status);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Apply::getApplyTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(Apply::getApplyTime, endDate + " 23:59:59");
        }

        if (applicantId != null) {
            wrapper.eq(Apply::getApplicantId, applicantId);
        }

        if (approverId != null) {
            wrapper.eq(Apply::getApproverId, approverId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Apply::getApplyNo, keyword);
        }

        wrapper.orderByDesc(Apply::getCreateTime);

        Page<Apply> result = applyMapper.selectPage(page, wrapper);

        // 批量填充关联数据
        fillApplyInfoBatch(result.getRecords());

        return result;
    }

    @Override
    public Page<Apply> listMyApplies(Integer pageNum, Integer pageSize, Integer status) {
        Long applicantId = getCurrentUserId();

        Page<Apply> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Apply::getApplicantId, applicantId);

        if (status != null) {
            wrapper.eq(Apply::getStatus, status);
        }

        wrapper.orderByDesc(Apply::getCreateTime);

        Page<Apply> result = applyMapper.selectPage(page, wrapper);

        // 批量填充关联数据
        fillApplyInfoBatch(result.getRecords());

        return result;
    }

    @Override
    public Page<Apply> listPendingApplies(Integer pageNum, Integer pageSize) {
        // 获取当前用户
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 查询当前用户管理的仓库
        LambdaQueryWrapper<Warehouse> warehouseWrapper = new LambdaQueryWrapper<>();
        warehouseWrapper.eq(Warehouse::getManagerId, userId);
        List<Warehouse> warehouses = warehouseMapper.selectList(warehouseWrapper);

        if (warehouses.isEmpty()) {
            // 如果不是仓管员，返回空列表
            return new Page<>(pageNum, pageSize);
        }

        // 查询这些仓库的待审批申请
        Page<Apply> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();
        // 收集仓库ID列表，避免空列表导致的SQL语法错误
        List<Long> warehouseIds = warehouses.stream()
                .map(Warehouse::getId)
                .collect(Collectors.toList());
        if (!warehouseIds.isEmpty()) {
            wrapper.in(Apply::getWarehouseId, warehouseIds);
        }
        wrapper.eq(Apply::getStatus, ApplyStatus.PENDING.getValue());
        wrapper.orderByAsc(Apply::getApplyTime);

        Page<Apply> result = applyMapper.selectPage(page, wrapper);

        // 批量填充关联数据
        fillApplyInfoBatch(result.getRecords());

        return result;
    }

    @Override
    public Apply getApplyById(Long id) {
        Apply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(404, "申请单不存在");
        }

        // 填充关联数据
        fillApplyInfo(apply);

        // 查询明细
        LambdaQueryWrapper<ApplyDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplyDetail::getApplyId, id);
        List<ApplyDetail> details = applyDetailMapper.selectList(wrapper);

        // 填充明细物资信息
        details.forEach(detail -> {
            Material material = materialMapper.selectById(detail.getMaterialId());
            if (material != null) {
                detail.setMaterialName(material.getMaterialName());
                detail.setMaterialCode(material.getMaterialCode());
                detail.setSpec(material.getSpec());
                detail.setUnit(material.getUnit());
            }
        });

        apply.setDetails(details);

        return apply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApply(ApplyDTO dto) {
        // 获取当前用户
        Long applicantId = getCurrentUserId();
        User applicant = userMapper.selectById(applicantId);

        if (applicant == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查仓库是否存在且属于申请人所在部门
        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }
        if (!warehouse.getDeptId().equals(applicant.getDeptId())) {
            throw new BusinessException(403, "只能向本部门仓库提交申请");
        }

        // 查询部门信息
        Dept dept = deptMapper.selectById(applicant.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 生成申请单号: SQ_部门编码_YYYYMMDD_流水号
        String applyNo = generateApplyNo(dept.getDeptCode());

        // 创建申请单
        Apply apply = new Apply();
        apply.setApplyNo(applyNo);
        apply.setWarehouseId(dto.getWarehouseId());
        apply.setApplicantId(applicantId);
        apply.setApplicantName(applicant.getRealName());
        apply.setApplicantPhone(applicant.getPhone());
        apply.setDeptId(applicant.getDeptId());
        apply.setDeptName(dept.getDeptName());
        apply.setApplyTime(LocalDateTime.now());
        apply.setStatus(ApplyStatus.PENDING);
        apply.setApplyReason(dto.getApplyReason());
        apply.setPurpose(dto.getApplyReason());

        applyMapper.insert(apply);
        log.info("创建申请单: id={}, applyNo={}", apply.getId(), applyNo);

        // 创建申请明细
        for (ApplyDTO.ApplyDetailDTO detailDTO : dto.getDetails()) {
            // 检查物资是否存在
            Material material = materialMapper.selectById(detailDTO.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + detailDTO.getMaterialId());
            }

            ApplyDetail detail = new ApplyDetail();
            detail.setApplyId(apply.getId());
            detail.setMaterialId(detailDTO.getMaterialId());
            detail.setMaterialName(material.getMaterialName());
            detail.setMaterialCode(material.getMaterialCode());
            detail.setSpec(material.getSpec());
            detail.setUnit(material.getUnit());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setRemark(detailDTO.getRemark());

            applyDetailMapper.insert(detail);

            log.info("申请明细: materialId={}, quantity={}", detailDTO.getMaterialId(), detailDTO.getQuantity());
        }

        log.info("申请单创建成功: applyNo={}", applyNo);

        // 通知仓库管理员有新申请待审批（事务提交后发送）
        if (warehouse.getManagerId() != null) {
            eventPublisher.publishEvent(new NotificationEvent(NotificationMessageDTO.builder()
                    .receiverId(warehouse.getManagerId())
                    .messageType(MessageType.APPLY_SUBMIT.getValue())
                    .title("新的物资申请待审批")
                    .content(String.format("员工 %s 提交了申请单 %s，请及时审批。",
                            applicant.getRealName(), applyNo))
                    .relatedId(apply.getId())
                    .relatedType(3)
                    .sendWechat(true)
                    .build()));
        }

        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApply(ApprovalDTO dto) {
        // 获取当前用户（审批人）
        Long approverId = getCurrentUserId();
        User approver = userMapper.selectById(approverId);

        if (approver == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 查询申请单
        Apply apply = applyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException(404, "申请单不存在");
        }

        // 检查申请单状态
        if (!ApplyStatus.PENDING.equals(apply.getStatus())) {
            throw new BusinessException(400, "申请单状态不正确，当前状态: " + apply.getStatus());
        }

        // 检查权限：仓管员或部门管理员可以审批
        Warehouse warehouse = warehouseMapper.selectById(apply.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        // 检查是否是仓库管理员（增加null检查）
        boolean isWarehouseManager = warehouse.getManagerId() != null
                && warehouse.getManagerId().equals(approverId);

        // 检查是否是部门管理员（从Role表查询roleCode，避免非数据库字段为null）
        boolean isDeptAdmin = false;
        if (!isWarehouseManager && approver != null) {
            Role approverRole = roleMapper.selectById(approver.getRoleId());
            if (approverRole != null && "DEPT_ADMIN".equals(approverRole.getRoleCode())) {
                // 部门管理员只能审批本部门的申请
                if (approver.getDeptId() != null && approver.getDeptId().equals(apply.getDeptId())) {
                    isDeptAdmin = true;
                }
            }
        }

        if (!isWarehouseManager && !isDeptAdmin) {
            throw new BusinessException(403, "无权审批此申请单");
        }

        if (dto.getApprovalResult() == 1) {
            // 条件更新抢占状态（防止并发重复审批：两个审批人同时通过会导致库存双重锁定、生成两张出库单）
            claimApplyStatus(apply.getId(), ApplyStatus.APPROVED, approverId, dto.getApprovalRemark());

            // 查询申请明细
            LambdaQueryWrapper<ApplyDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(ApplyDetail::getApplyId, apply.getId());
            List<ApplyDetail> applyDetails = applyDetailMapper.selectList(detailWrapper);

            // 锁定库存（使用乐观锁重试机制）
            // 任一明细库存不足即抛异常，整个事务回滚，状态抢占与已锁定的部分一并撤销
            for (ApplyDetail detail : applyDetails) {
                boolean locked = inventoryService.lockInventory(
                        apply.getWarehouseId(),
                        detail.getMaterialId(),
                        detail.getQuantity()
                );
                if (!locked) {
                    Material material = materialMapper.selectById(detail.getMaterialId());
                    throw new BusinessException(1001, "库存不足: " + (material != null ? material.getMaterialName() : "物资ID:" + detail.getMaterialId()));
                }
            }

            log.info("审批通过: applyNo={}, approverId={}", apply.getApplyNo(), approverId);

            // 自动创建出库单（状态为待取货，库存已预扣）
            Long outboundId = outboundService.createOutboundFromApply(
                    apply.getId(),
                    apply.getWarehouseId(),
                    apply.getApplicantId(),
                    approverId
            );

            log.info("自动创建出库单: applyId={}, outboundId={}", apply.getId(), outboundId);

            // 通知申请人审批通过（事务提交后发送）
            eventPublisher.publishEvent(new NotificationEvent(NotificationMessageDTO.builder()
                    .receiverId(apply.getApplicantId())
                    .messageType(MessageType.APPLY_APPROVED.getValue())
                    .title("申请审批通过")
                    .content(String.format("您的申请单 %s 已审批通过，请在7天内前往仓库取货，逾期将自动取消。",
                            apply.getApplyNo()))
                    .relatedId(apply.getId())
                    .relatedType(3)
                    .sendWechat(true)
                    .build()));

        } else if (dto.getApprovalResult() == 2) {
            // 审批拒绝（同样条件更新抢占状态）
            claimApplyStatus(apply.getId(), ApplyStatus.REJECTED, approverId, dto.getApprovalRemark());

            log.info("审批拒绝: applyNo={}, approverId={}, reason={}",
                    apply.getApplyNo(), approverId, dto.getApprovalRemark());

            // 通知申请人审批被拒绝（事务提交后发送）
            eventPublisher.publishEvent(new NotificationEvent(NotificationMessageDTO.builder()
                    .receiverId(apply.getApplicantId())
                    .messageType(MessageType.APPLY_REJECTED.getValue())
                    .title("申请审批未通过")
                    .content(String.format("您的申请单 %s 未通过审批。%s",
                            apply.getApplyNo(),
                            StringUtils.hasText(dto.getApprovalRemark()) ? "原因: " + dto.getApprovalRemark() : ""))
                    .relatedId(apply.getId())
                    .relatedType(3)
                    .sendWechat(true)
                    .build()));

        } else {
            throw new BusinessException(400, "审批结果无效");
        }
    }

    /**
     * 条件更新抢占申请单状态（仅从待审批状态迁移），失败说明已被其他人处理
     * <p>
     * 注意：审批意见持久化到 approvalOpinion 列（approvalRemark 是非数据库字段）
     */
    private void claimApplyStatus(Long applyId, ApplyStatus toStatus, Long approverId, String approvalRemark) {
        LambdaUpdateWrapper<Apply> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Apply::getId, applyId)
                .eq(Apply::getStatus, ApplyStatus.PENDING)
                .set(Apply::getStatus, toStatus)
                .set(Apply::getApproverId, approverId)
                .set(Apply::getApprovalTime, LocalDateTime.now())
                .set(Apply::getApprovalOpinion, approvalRemark);
        if (ApplyStatus.REJECTED.equals(toStatus)) {
            wrapper.set(Apply::getRejectReason, approvalRemark);
        }
        if (applyMapper.update(null, wrapper) == 0) {
            throw new BusinessException(400, "申请单已被处理，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApply(Long id) {
        // 获取当前用户
        Long userId = getCurrentUserId();

        // 查询申请单
        Apply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(404, "申请单不存在");
        }

        // 检查权限：只能取消自己的申请
        if (!apply.getApplicantId().equals(userId)) {
            throw new BusinessException(403, "无权取消此申请单");
        }

        // 检查状态：只能取消待审批或已审批（待取货）的申请
        ApplyStatus fromStatus = apply.getStatus();
        if (!ApplyStatus.PENDING.equals(fromStatus) && !ApplyStatus.APPROVED.equals(fromStatus)) {
            throw new BusinessException(400, "当前状态不允许取消");
        }

        // 如果是已审批状态，先取消关联的出库单（其内部会释放锁定库存，且条件更新可防止与确认取货竞态）
        // 注意：不能在此处再按申请明细解锁一遍，否则会与出库单取消的解锁重复，导致锁定量被双重释放
        if (ApplyStatus.APPROVED.equals(fromStatus)) {
            boolean outboundCanceled = outboundService.cancelOutboundByApplyId(
                    apply.getId(), "申请人取消申请，自动取消出库单");

            if (!outboundCanceled) {
                // 理论上已审批申请必有待取货出库单；若不存在（数据异常兜底），直接按申请明细释放锁定库存
                LambdaQueryWrapper<ApplyDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(ApplyDetail::getApplyId, apply.getId());
                List<ApplyDetail> details = applyDetailMapper.selectList(detailWrapper);

                for (ApplyDetail detail : details) {
                    inventoryService.unlockInventory(
                            apply.getWarehouseId(),
                            detail.getMaterialId(),
                            detail.getQuantity()
                    );
                    log.info("释放锁定库存: warehouseId={}, materialId={}, quantity={}",
                            apply.getWarehouseId(), detail.getMaterialId(), detail.getQuantity());
                }
            }
        }

        // 条件更新状态（防止与审批/确认取货并发冲突）
        LambdaUpdateWrapper<Apply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Apply::getId, id)
                .eq(Apply::getStatus, fromStatus)
                .set(Apply::getStatus, ApplyStatus.CANCELED);
        if (applyMapper.update(null, updateWrapper) == 0) {
            throw new BusinessException(400, "申请单状态已变更，无法取消");
        }

        log.info("取消申请单: applyNo={}, applicantId={}", apply.getApplyNo(), userId);
    }

    /**
     * 批量填充申请单关联信息（解决N+1查询问题）
     */
    private void fillApplyInfoBatch(List<Apply> applies) {
        if (applies == null || applies.isEmpty()) {
            return;
        }

        // 收集所有需要的ID
        Set<Long> warehouseIds = applies.stream()
                .map(Apply::getWarehouseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> userIds = applies.stream()
                .map(Apply::getApplicantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        applies.stream()
                .map(Apply::getApproverId)
                .filter(id -> id != null)
                .forEach(userIds::add);

        // 批量查询仓库（避免空集合导致SQL错误）
        Map<Long, Warehouse> warehouseMap;
        if (!warehouseIds.isEmpty()) {
            List<Warehouse> warehouses = warehouseMapper.selectBatchIds(warehouseIds);
            warehouseMap = warehouses != null ?
                    warehouses.stream().collect(Collectors.toMap(Warehouse::getId, w -> w)) :
                    Map.of();
        } else {
            warehouseMap = Map.of();
        }

        // 批量查询用户（避免空集合导致SQL错误）
        Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users != null ?
                    users.stream().collect(Collectors.toMap(User::getId, u -> u)) :
                    Map.of();
        } else {
            userMap = Map.of();
        }

        // 填充数据
        for (Apply apply : applies) {
            // 填充仓库名称
            if (apply.getWarehouseId() != null) {
                Warehouse warehouse = warehouseMap.get(apply.getWarehouseId());
                if (warehouse != null) {
                    apply.setWarehouseName(warehouse.getWarehouseName());
                }
            }

            // 填充申请人姓名
            if (apply.getApplicantId() != null) {
                User applicant = userMap.get(apply.getApplicantId());
                if (applicant != null) {
                    apply.setApplicantName(applicant.getRealName());
                }
            }

            // 填充审批人姓名
            if (apply.getApproverId() != null) {
                User approver = userMap.get(apply.getApproverId());
                if (approver != null) {
                    apply.setApproverName(approver.getRealName());
                }
            }
        }
    }

    /**
     * 填充申请单关联信息
     */
    private void fillApplyInfo(Apply apply) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(apply.getWarehouseId());
        if (warehouse != null) {
            apply.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充申请人姓名
        User applicant = userMapper.selectById(apply.getApplicantId());
        if (applicant != null) {
            apply.setApplicantName(applicant.getRealName());
        }

        // 填充审批人姓名
        if (apply.getApproverId() != null) {
            User approver = userMapper.selectById(apply.getApproverId());
            if (approver != null) {
                apply.setApproverName(approver.getRealName());
            }
        }
    }

    /**
     * 生成申请单号（Redis INCR 流水号，无碰撞；Redis 不可用时降级雪花取模）
     */
    private String generateApplyNo(String deptCode) {
        return orderNoGenerator.generate("SQ", deptCode);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new BusinessException(401, "未登录");
    }
}
