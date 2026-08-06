package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.common.enums.OutboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.*;
import com.ct.wms.event.NotificationEvent;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 出库Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundServiceImpl implements OutboundService {

    private final OutboundMapper outboundMapper;
    private final OutboundDetailMapper outboundDetailMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final ApplyMapper applyMapper;
    private final ApplyDetailMapper applyDetailMapper;
    private final InventoryService inventoryService;
    private final OrderNoGenerator orderNoGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Outbound> listOutbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                                        Integer outboundType, Integer status, String startDate,
                                        String endDate, Long operatorId, Long receiverId, String keyword) {
        Page<Outbound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Outbound::getWarehouseId, warehouseId);
        }

        if (outboundType != null) {
            wrapper.eq(Outbound::getOutboundType, outboundType);
        }

        if (status != null) {
            wrapper.eq(Outbound::getStatus, status);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Outbound::getOutboundTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(Outbound::getOutboundTime, endDate + " 23:59:59");
        }

        if (operatorId != null) {
            wrapper.eq(Outbound::getOperatorId, operatorId);
        }

        if (receiverId != null) {
            wrapper.eq(Outbound::getReceiverId, receiverId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Outbound::getOutboundNo, keyword);
        }

        wrapper.orderByDesc(Outbound::getCreateTime);

        Page<Outbound> result = outboundMapper.selectPage(page, wrapper);

        // 填充关联数据（防御性检查，避免空指针）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillOutboundInfo);
        }

        return result;
    }

    @Override
    public Outbound getOutboundById(Long id) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BusinessException(404, "出库单不存在");
        }

        // 填充关联数据
        fillOutboundInfo(outbound);

        // 查询明细
        LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDetail::getOutboundId, id);
        List<OutboundDetail> details = outboundDetailMapper.selectList(wrapper);

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

        outbound.setDetails(details);

        return outbound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutboundDirect(OutboundDTO dto) {
        // 获取当前用户
        Long operatorId = getCurrentUserId();
        User operator = userMapper.selectById(operatorId);

        // 检查仓库是否存在
        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        // 查询部门信息
        Dept dept = deptMapper.selectById(warehouse.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 使用 lockInventory 原子性锁定库存（含行锁+乐观锁重试），消除 checkInventory 与 decreaseInventory 之间的竞态
        // 任一明细库存不足即抛异常，本方法整体在一个事务内，已锁定的部分随事务回滚自动释放
        for (OutboundDTO.OutboundDetailDTO detailDTO : dto.getDetails()) {
            Material material = materialMapper.selectById(detailDTO.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + detailDTO.getMaterialId());
            }
            boolean locked = inventoryService.lockInventory(dto.getWarehouseId(),
                    detailDTO.getMaterialId(), detailDTO.getQuantity());
            if (!locked) {
                throw new BusinessException(1001, "库存不足: " + material.getMaterialName());
            }
        }

        // 生成出库单号: CK_部门编码_YYYYMMDD_流水号
        String outboundNo = generateOutboundNo(dept.getDeptCode());

        // 计算总金额
        BigDecimal totalAmount = dto.getDetails().stream()
                .map(detail -> {
                    BigDecimal unitPrice = detail.getUnitPrice() != null ?
                            detail.getUnitPrice() : BigDecimal.ZERO;
                    return unitPrice.multiply(detail.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建出库单
        Outbound outbound = new Outbound();
        outbound.setOutboundNo(outboundNo);
        outbound.setWarehouseId(dto.getWarehouseId());
        // 将Integer类型转换为OutboundType枚举
        if (dto.getOutboundType() != null) {
            for (OutboundType ot : OutboundType.values()) {
                if (ot.getCode().equals(dto.getOutboundType())) {
                    outbound.setOutboundType(ot);
                    break;
                }
            }
        }
        outbound.setSource(OutboundSource.DIRECT);
        outbound.setStatus(OutboundStatus.COMPLETED);
        outbound.setOperatorId(operatorId);
        outbound.setOperatorName(operator != null ? operator.getRealName() : null);

        // 获取领用人信息
        if (dto.getReceiverId() != null) {
            User receiver = userMapper.selectById(dto.getReceiverId());
            outbound.setReceiverId(dto.getReceiverId());
            outbound.setReceiverName(receiver != null ? receiver.getRealName() : null);
        }

        outbound.setOutboundTime(dto.getOutboundTime());
        outbound.setTotalAmount(totalAmount);
        outbound.setRemark(dto.getRemark());

        outboundMapper.insert(outbound);
        log.info("创建直接出库单: id={}, outboundNo={}", outbound.getId(), outboundNo);

        // 创建出库明细并扣减库存
        for (OutboundDTO.OutboundDetailDTO detailDTO : dto.getDetails()) {
            // 查询物资信息
            Material material = materialMapper.selectById(detailDTO.getMaterialId());

            // 创建明细
            OutboundDetail detail = new OutboundDetail();
            detail.setOutboundId(outbound.getId());
            detail.setMaterialId(detailDTO.getMaterialId());
            detail.setMaterialName(material != null ? material.getMaterialName() : null);
            detail.setMaterialCode(material != null ? material.getMaterialCode() : null);
            detail.setSpec(material != null ? material.getSpec() : null);
            detail.setUnit(material != null ? material.getUnit() : null);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());

            BigDecimal amount = detailDTO.getUnitPrice() != null ?
                    detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()) : BigDecimal.ZERO;
            detail.setAmount(amount);
            detail.setRemark(detailDTO.getRemark());

            outboundDetailMapper.insert(detail);

            // 提交库存（将锁定量转为实际扣减，原子操作）
            inventoryService.commitInventory(
                    dto.getWarehouseId(),
                    detailDTO.getMaterialId(),
                    detailDTO.getQuantity(),
                    outboundNo,
                    outbound.getId(),
                    operatorId
            );

            log.info("出库明细: materialId={}, quantity={}", detailDTO.getMaterialId(), detailDTO.getQuantity());
        }

        log.info("直接出库完成: outboundNo={}, totalAmount={}", outboundNo, totalAmount);

        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutboundFromApply(Long applyId, Long warehouseId, Long receiverId, Long operatorId) {
        // 查询申请单
        Apply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException(404, "申请单不存在");
        }

        // 查询申请明细
        LambdaQueryWrapper<ApplyDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplyDetail::getApplyId, applyId);
        List<ApplyDetail> applyDetails = applyDetailMapper.selectList(wrapper);

        if (applyDetails.isEmpty()) {
            throw new BusinessException(400, "申请单明细为空");
        }

        // 查询操作员信息
        User operator = userMapper.selectById(operatorId);

        // 检查仓库是否存在
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        // 查询部门信息
        Dept dept = deptMapper.selectById(warehouse.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 生成出库单号
        String outboundNo = generateOutboundNo(dept.getDeptCode());

        // 计算总金额
        BigDecimal totalAmount = applyDetails.stream()
                .map(detail -> {
                    Material material = materialMapper.selectById(detail.getMaterialId());
                    BigDecimal unitPrice = material != null && material.getPrice() != null ?
                            material.getPrice() : BigDecimal.ZERO;
                    return unitPrice.multiply(detail.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建出库单（状态为待取货，不扣减库存）
        Outbound outbound = new Outbound();
        outbound.setOutboundNo(outboundNo);
        outbound.setWarehouseId(warehouseId);
        outbound.setOutboundType(OutboundType.RECEIVE); // 领用
        outbound.setSource(OutboundSource.FROM_APPLY);
        outbound.setStatus(OutboundStatus.PENDING_PICKUP);
        outbound.setOperatorId(operatorId);
        outbound.setOperatorName(operator != null ? operator.getRealName() : null);

        // 获取领用人信息
        if (receiverId != null) {
            User receiver = userMapper.selectById(receiverId);
            outbound.setReceiverId(receiverId);
            outbound.setReceiverName(receiver != null ? receiver.getRealName() : null);
        }

        outbound.setApplyId(applyId);
        // 取货超时(7天)从审批通过时刻起算，不能用申请时间，否则审批耗时会挤占取货窗口
        outbound.setOutboundTime(LocalDateTime.now());
        outbound.setTotalAmount(totalAmount);
        outbound.setRemark("来自申请单: " + apply.getApplyNo());

        outboundMapper.insert(outbound);
        log.info("从申请单创建出库单: id={}, outboundNo={}, applyId={}", outbound.getId(), outboundNo, applyId);

        // 创建出库明细（不扣减库存）
        for (ApplyDetail applyDetail : applyDetails) {
            Material material = materialMapper.selectById(applyDetail.getMaterialId());
            BigDecimal unitPrice = material != null && material.getPrice() != null ?
                    material.getPrice() : BigDecimal.ZERO;

            OutboundDetail detail = new OutboundDetail();
            detail.setOutboundId(outbound.getId());
            detail.setMaterialId(applyDetail.getMaterialId());
            detail.setMaterialName(applyDetail.getMaterialName());
            detail.setMaterialCode(applyDetail.getMaterialCode());
            detail.setSpec(applyDetail.getSpec());
            detail.setUnit(applyDetail.getUnit());
            detail.setQuantity(applyDetail.getQuantity());
            detail.setUnitPrice(unitPrice);
            detail.setAmount(unitPrice.multiply(applyDetail.getQuantity()));
            detail.setRemark(applyDetail.getRemark());

            outboundDetailMapper.insert(detail);

            log.info("出库明细: materialId={}, quantity={}", applyDetail.getMaterialId(), applyDetail.getQuantity());
        }

        log.info("从申请单创建出库单完成: outboundNo={}, status=待取货", outboundNo);

        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutbound(Long id) {
        // 查询出库单
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BusinessException(404, "出库单不存在");
        }

        // 检查状态
        if (!OutboundStatus.PENDING_PICKUP.equals(outbound.getStatus())) {
            throw new BusinessException(400, "出库单状态不正确，当前状态: " + outbound.getStatus());
        }

        // 条件更新抢占状态，防止并发重复确认或与取消操作竞态
        LambdaUpdateWrapper<Outbound> claimWrapper = new LambdaUpdateWrapper<>();
        claimWrapper.eq(Outbound::getId, id)
                .eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
                .set(Outbound::getStatus, OutboundStatus.COMPLETED);
        if (outboundMapper.update(null, claimWrapper) == 0) {
            throw new BusinessException(400, "出库单已被处理，请刷新后重试");
        }

        // 查询出库明细
        LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDetail::getOutboundId, id);
        List<OutboundDetail> details = outboundDetailMapper.selectList(wrapper);

        // 将锁定库存转为实际扣减
        Long operatorId = getCurrentUserId();
        for (OutboundDetail detail : details) {
            inventoryService.commitInventory(
                    outbound.getWarehouseId(),
                    detail.getMaterialId(),
                    detail.getQuantity(),
                    outbound.getOutboundNo(),
                    outbound.getId(),
                    operatorId
            );

            log.info("确认出库，锁定转扣减: materialId={}, quantity={}", detail.getMaterialId(), detail.getQuantity());
        }

        // 更新申请单状态为已完成（条件更新，仅从已审批状态迁移）
        if (outbound.getApplyId() != null) {
            LambdaUpdateWrapper<Apply> applyWrapper = new LambdaUpdateWrapper<>();
            applyWrapper.eq(Apply::getId, outbound.getApplyId())
                    .eq(Apply::getStatus, ApplyStatus.APPROVED)
                    .set(Apply::getStatus, ApplyStatus.COMPLETED);
            int updated = applyMapper.update(null, applyWrapper);
            if (updated == 0) {
                log.warn("确认出库时申请单状态非已审批，未更新: applyId={}", outbound.getApplyId());
            }
        }

        // 通知领用人取货完成（事务提交后发送）
        if (outbound.getReceiverId() != null) {
            eventPublisher.publishEvent(new NotificationEvent(NotificationMessageDTO.builder()
                    .receiverId(outbound.getReceiverId())
                    .messageType(MessageType.SYSTEM.getValue())
                    .title("出库单取货完成")
                    .content(String.format("出库单 %s 已确认取货，物资出库完成。", outbound.getOutboundNo()))
                    .relatedId(outbound.getId())
                    .relatedType(2)
                    .sendWechat(false)
                    .build()));
        }

        log.info("确认出库完成: outboundNo={}", outbound.getOutboundNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutbound(Long id, String reason) {
        // 查询出库单
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BusinessException(404, "出库单不存在");
        }

        // 检查状态（只有待取货状态才能取消）
        if (!OutboundStatus.PENDING_PICKUP.equals(outbound.getStatus())) {
            throw new BusinessException(400, "出库单状态不正确，只有待取货状态才能取消");
        }

        // 条件更新抢占状态，防止与确认取货操作竞态
        String newRemark = (outbound.getRemark() != null ? outbound.getRemark() : "") + " [取消原因: " + reason + "]";
        LambdaUpdateWrapper<Outbound> claimWrapper = new LambdaUpdateWrapper<>();
        claimWrapper.eq(Outbound::getId, id)
                .eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
                .set(Outbound::getStatus, OutboundStatus.CANCELED)
                .set(Outbound::getRemark, newRemark);
        if (outboundMapper.update(null, claimWrapper) == 0) {
            throw new BusinessException(400, "出库单已被处理，请刷新后重试");
        }

        // 查询出库明细并释放锁定库存
        LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDetail::getOutboundId, id);
        List<OutboundDetail> details = outboundDetailMapper.selectList(wrapper);

        for (OutboundDetail detail : details) {
            inventoryService.unlockInventory(
                    outbound.getWarehouseId(),
                    detail.getMaterialId(),
                    detail.getQuantity()
            );
            log.info("取消出库单，释放锁定库存: materialId={}, quantity={}", detail.getMaterialId(), detail.getQuantity());
        }

        // 更新申请单状态为已取消（条件更新，仅从已审批状态迁移）
        if (outbound.getApplyId() != null) {
            LambdaUpdateWrapper<Apply> applyWrapper = new LambdaUpdateWrapper<>();
            applyWrapper.eq(Apply::getId, outbound.getApplyId())
                    .eq(Apply::getStatus, ApplyStatus.APPROVED)
                    .set(Apply::getStatus, ApplyStatus.CANCELED);
            applyMapper.update(null, applyWrapper);
        }

        log.info("取消出库单: outboundNo={}, reason={}", outbound.getOutboundNo(), reason);
    }

    /**
     * 填充出库单关联信息
     */
    private void fillOutboundInfo(Outbound outbound) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(outbound.getWarehouseId());
        if (warehouse != null) {
            outbound.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充操作人姓名
        User operator = userMapper.selectById(outbound.getOperatorId());
        if (operator != null) {
            outbound.setOperatorName(operator.getRealName());
        }

        // 填充领用人姓名
        if (outbound.getReceiverId() != null) {
            User receiver = userMapper.selectById(outbound.getReceiverId());
            if (receiver != null) {
                outbound.setReceiverName(receiver.getRealName());
            }
        }
    }

    /**
     * 生成出库单号（Redis INCR 流水号，无碰撞；Redis 不可用时降级雪花取模）
     */
    private String generateOutboundNo(String deptCode) {
        return orderNoGenerator.generate("CK", deptCode);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOutboundByApplyId(Long applyId, String reason) {
        // 根据申请单ID查询关联的出库单
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Outbound::getApplyId, applyId)
                .eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP);

        Outbound outbound = outboundMapper.selectOne(wrapper);
        if (outbound == null) {
            log.info("没有找到待取货状态的关联出库单，applyId={}", applyId);
            return false;
        }

        // 条件更新抢占状态，防止与确认取货操作竞态
        String newRemark = (outbound.getRemark() != null ? outbound.getRemark() : "") + " [取消原因: " + reason + "]";
        LambdaUpdateWrapper<Outbound> claimWrapper = new LambdaUpdateWrapper<>();
        claimWrapper.eq(Outbound::getId, outbound.getId())
                .eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
                .set(Outbound::getStatus, OutboundStatus.CANCELED)
                .set(Outbound::getRemark, newRemark);
        if (outboundMapper.update(null, claimWrapper) == 0) {
            throw new BusinessException(400, "关联出库单已被处理，无法取消");
        }

        // 查询出库明细并释放锁定库存
        LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OutboundDetail::getOutboundId, outbound.getId());
        List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);

        for (OutboundDetail detail : details) {
            inventoryService.unlockInventory(
                    outbound.getWarehouseId(),
                    detail.getMaterialId(),
                    detail.getQuantity()
            );
            log.info("取消关联出库单，释放锁定库存: materialId={}, quantity={}", detail.getMaterialId(), detail.getQuantity());
        }

        log.info("取消关联出库单成功: outboundId={}, applyId={}", outbound.getId(), applyId);
        return true;
    }
}
