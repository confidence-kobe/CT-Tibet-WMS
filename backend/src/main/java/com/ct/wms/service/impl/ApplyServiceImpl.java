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
import com.ct.wms.service.OutboundService;
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
    private final OutboundService outboundService;

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

        // 填充关联数据
        result.getRecords().forEach(this::fillApplyInfo);

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

        // 填充关联数据
        result.getRecords().forEach(this::fillApplyInfo);

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
        wrapper.in(Apply::getWarehouseId, warehouses.stream()
                .map(Warehouse::getId)
                .toArray());
        wrapper.eq(Apply::getStatus, ApplyStatus.PENDING.getValue());
        wrapper.orderByAsc(Apply::getApplyTime);

        Page<Apply> result = applyMapper.selectPage(page, wrapper);

        // 填充关联数据
        result.getRecords().forEach(this::fillApplyInfo);

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

        // 检查仓库是否存在
        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
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
        apply.setApplyTime(dto.getApplyTime());
        apply.setStatus(ApplyStatus.PENDING.getValue());
        apply.setApplyReason(dto.getApplyReason());

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
        if (!ApplyStatus.PENDING.getValue().equals(apply.getStatus())) {
            throw new BusinessException(400, "申请单状态不正确，当前状态: " + apply.getStatus());
        }

        // 检查权限：仓管员只能审批自己管理的仓库的申请
        Warehouse warehouse = warehouseMapper.selectById(apply.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        if (!warehouse.getManagerId().equals(approverId)) {
            throw new BusinessException(403, "无权审批此申请单");
        }

        // 更新申请单
        apply.setApproverId(approverId);
        apply.setApprovalTime(LocalDateTime.now());
        apply.setApprovalRemark(dto.getApprovalRemark());

        if (dto.getApprovalResult() == 1) {
            // 审批通过
            apply.setStatus(ApplyStatus.APPROVED.getValue());
            applyMapper.updateById(apply);

            log.info("审批通过: applyNo={}, approverId={}", apply.getApplyNo(), approverId);

            // 自动创建出库单（状态为待取货，不扣减库存）
            Long outboundId = outboundService.createOutboundFromApply(
                    apply.getId(),
                    apply.getWarehouseId(),
                    apply.getApplicantId(),
                    approverId
            );

            log.info("自动创建出库单: applyId={}, outboundId={}", apply.getId(), outboundId);

        } else if (dto.getApprovalResult() == 2) {
            // 审批拒绝
            apply.setStatus(ApplyStatus.REJECTED.getValue());
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

        // 检查状态：只能取消待审批的申请
        if (!ApplyStatus.PENDING.getValue().equals(apply.getStatus())) {
            throw new BusinessException(400, "只能取消待审批的申请单");
        }

        // 更新状态
        apply.setStatus(ApplyStatus.CANCELED.getValue());
        applyMapper.updateById(apply);

        log.info("取消申请单: applyNo={}, applicantId={}", apply.getApplyNo(), userId);
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
     * 生成申请单号
     */
    private String generateApplyNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SQ_" + deptCode + "_" + today + "_";

        // 查询今天最大的流水号
        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Apply::getApplyNo, prefix);
        wrapper.orderByDesc(Apply::getApplyNo);
        wrapper.last("LIMIT 1");

        Apply lastApply = applyMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastApply != null) {
            String lastNo = lastApply.getApplyNo();
            String lastSeq = lastNo.substring(lastNo.lastIndexOf("_") + 1);
            sequence = Integer.parseInt(lastSeq) + 1;
        }

        return prefix + String.format("%04d", sequence);
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
