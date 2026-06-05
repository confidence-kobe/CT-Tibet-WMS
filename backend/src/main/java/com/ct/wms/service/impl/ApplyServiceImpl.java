package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final IdGenerator idGenerator;

    @Override
    public Page<Apply> listApplies(Integer pageNum, Integer pageSize, Long warehouseId,
                                   Integer status, String startDate, String endDate,
                                   Long applicantId, Long approverId, String keyword) {
        Page<Apply> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();

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

        // 更新申请单
        apply.setApproverId(approverId);
        apply.setApprovalTime(LocalDateTime.now());
        apply.setApprovalRemark(dto.getApprovalRemark());

        if (dto.getApprovalResult() == 1) {
            // 审批通过 - 先锁定库存再创建出库单
            // 查询申请明细
            LambdaQueryWrapper<ApplyDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(ApplyDetail::getApplyId, apply.getId());
            List<ApplyDetail> applyDetails = applyDetailMapper.selectList(detailWrapper);

            // 锁定库存（使用乐观锁重试机制）
            for (int i = 0; i < applyDetails.size(); i++) {
                ApplyDetail detail = applyDetails.get(i);
                boolean locked = inventoryService.lockInventory(
                        apply.getWarehouseId(),
                        detail.getMaterialId(),
                        detail.getQuantity()
                );
                if (!locked) {
                    // 库存不足，释放已锁定的库存并抛出异常
                    for (int j = 0; j < i; j++) {
                        inventoryService.unlockInventory(
                                apply.getWarehouseId(),
                                applyDetails.get(j).getMaterialId(),
                                applyDetails.get(j).getQuantity()
                        );
                    }
                    Material material = materialMapper.selectById(detail.getMaterialId());
                    throw new BusinessException(1001, "库存不足: " + (material != null ? material.getMaterialName() : "物资ID:" + detail.getMaterialId()));
                }
            }

            // 库存锁定成功，更新申请状态
            apply.setStatus(ApplyStatus.APPROVED);
            applyMapper.updateById(apply);

            log.info("审批通过: applyNo={}, approverId={}", apply.getApplyNo(), approverId);

            // 自动创建出库单（状态为待取货，库存已预扣）
            Long outboundId = outboundService.createOutboundFromApply(
                    apply.getId(),
                    apply.getWarehouseId(),
                    apply.getApplicantId(),
                    approverId
            );

            log.info("自动创建出库单: applyId={}, outboundId={}", apply.getId(), outboundId);

        } else if (dto.getApprovalResult() == 2) {
            // 审批拒绝
            apply.setStatus(ApplyStatus.REJECTED);
            applyMapper.updateById(apply);

            log.info("审批拒绝: applyNo={}, approverId={}, reason={}",
                    apply.getApplyNo(), approverId, dto.getApprovalRemark());

        } else {
            throw new BusinessException(400, "审批结果无效");
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
        if (!ApplyStatus.PENDING.equals(apply.getStatus()) && !ApplyStatus.APPROVED.equals(apply.getStatus())) {
            throw new BusinessException(400, "当前状态不允许取消");
        }

        // 如果是已审批状态，需要释放锁定的库存，并取消关联的出库单
        if (ApplyStatus.APPROVED.equals(apply.getStatus())) {
            // 查询申请明细并释放锁定库存
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

            // 同步取消关联的出库单
            outboundService.cancelOutboundByApplyId(apply.getId(), "申请人取消申请，自动取消出库单");
        }

        // 更新状态
        apply.setStatus(ApplyStatus.CANCELED);
        applyMapper.updateById(apply);

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
     * 生成申请单号（使用分布式ID生成器，高性能高并发）
     */
    private String generateApplyNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SQ_" + deptCode + "_" + today + "_";
        // 使用雪花算法生成的ID作为流水号，确保唯一性
        long sequence = idGenerator.nextId() % 100000;
        return prefix + String.format("%05d", sequence);
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
