package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        // 填充关联数据
        result.getRecords().forEach(this::fillOutboundInfo);

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

        // 检查库存是否充足
        for (OutboundDTO.OutboundDetailDTO detailDTO : dto.getDetails()) {
            Material material = materialMapper.selectById(detailDTO.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + detailDTO.getMaterialId());
            }

            if (!inventoryService.checkInventory(dto.getWarehouseId(), detailDTO.getMaterialId(), detailDTO.getQuantity())) {
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
        outbound.setOutboundType(dto.getOutboundType());
        outbound.setSource(OutboundSource.DIRECT.getValue());
        outbound.setStatus(OutboundStatus.COMPLETED.getValue());
        outbound.setOperatorId(operatorId);
        outbound.setReceiverId(dto.getReceiverId());
        outbound.setOutboundTime(dto.getOutboundTime());
        outbound.setTotalAmount(totalAmount);
        outbound.setRemark(dto.getRemark());

        outboundMapper.insert(outbound);
        log.info("创建直接出库单: id={}, outboundNo={}", outbound.getId(), outboundNo);

        // 创建出库明细并扣减库存
        for (OutboundDTO.OutboundDetailDTO detailDTO : dto.getDetails()) {
            // 创建明细
            OutboundDetail detail = new OutboundDetail();
            detail.setOutboundId(outbound.getId());
            detail.setMaterialId(detailDTO.getMaterialId());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());

            BigDecimal amount = detailDTO.getUnitPrice() != null ?
                    detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()) : BigDecimal.ZERO;
            detail.setAmount(amount);
            detail.setRemark(detailDTO.getRemark());

            outboundDetailMapper.insert(detail);

            // 扣减库存
            inventoryService.decreaseInventory(
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

        // 检查库存是否充足
        for (ApplyDetail applyDetail : applyDetails) {
            Material material = materialMapper.selectById(applyDetail.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + applyDetail.getMaterialId());
            }

            if (!inventoryService.checkInventory(warehouseId, applyDetail.getMaterialId(), applyDetail.getQuantity())) {
                throw new BusinessException(1001, "库存不足: " + material.getMaterialName());
            }
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
        outbound.setOutboundType(1); // 领用
        outbound.setSource(OutboundSource.FROM_APPLY.getValue());
        outbound.setStatus(OutboundStatus.PENDING_PICKUP.getValue());
        outbound.setOperatorId(operatorId);
        outbound.setReceiverId(receiverId);
        outbound.setApplyId(applyId);
        outbound.setOutboundTime(apply.getApplyTime());
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
        if (!OutboundStatus.PENDING_PICKUP.getValue().equals(outbound.getStatus())) {
            throw new BusinessException(400, "出库单状态不正确，当前状态: " + outbound.getStatus());
        }

        // 查询出库明细
        LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDetail::getOutboundId, id);
        List<OutboundDetail> details = outboundDetailMapper.selectList(wrapper);

        // 扣减库存
        Long operatorId = getCurrentUserId();
        for (OutboundDetail detail : details) {
            inventoryService.decreaseInventory(
                    outbound.getWarehouseId(),
                    detail.getMaterialId(),
                    detail.getQuantity(),
                    outbound.getOutboundNo(),
                    outbound.getId(),
                    operatorId
            );

            log.info("确认出库，扣减库存: materialId={}, quantity={}", detail.getMaterialId(), detail.getQuantity());
        }

        // 更新出库单状态
        outbound.setStatus(OutboundStatus.COMPLETED.getValue());
        outboundMapper.updateById(outbound);

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
        if (!OutboundStatus.PENDING_PICKUP.getValue().equals(outbound.getStatus())) {
            throw new BusinessException(400, "出库单状态不正确，只有待取货状态才能取消");
        }

        // 更新出库单状态
        outbound.setStatus(OutboundStatus.CANCELED.getValue());
        outbound.setRemark(outbound.getRemark() + " [取消原因: " + reason + "]");
        outboundMapper.updateById(outbound);

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
     * 生成出库单号
     */
    private String generateOutboundNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CK_" + deptCode + "_" + today + "_";

        // 查询今天最大的流水号
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Outbound::getOutboundNo, prefix);
        wrapper.orderByDesc(Outbound::getOutboundNo);
        wrapper.last("LIMIT 1");

        Outbound lastOutbound = outboundMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastOutbound != null) {
            String lastNo = lastOutbound.getOutboundNo();
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
