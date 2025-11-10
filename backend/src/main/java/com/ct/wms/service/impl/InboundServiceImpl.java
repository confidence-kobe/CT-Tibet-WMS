package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
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
import java.util.stream.Collectors;

/**
 * 入库Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;
    private final InboundDetailMapper inboundDetailMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final InventoryService inventoryService;

    @Override
    public Page<Inbound> listInbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                                     Integer inboundType, String startDate, String endDate,
                                     Long operatorId, String keyword) {
        Page<Inbound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Inbound::getWarehouseId, warehouseId);
        }

        if (inboundType != null) {
            wrapper.eq(Inbound::getInboundType, inboundType);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Inbound::getInboundTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(Inbound::getInboundTime, endDate + " 23:59:59");
        }

        if (operatorId != null) {
            wrapper.eq(Inbound::getOperatorId, operatorId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Inbound::getInboundNo, keyword);
        }

        wrapper.orderByDesc(Inbound::getCreateTime);

        Page<Inbound> result = inboundMapper.selectPage(page, wrapper);

        // 填充关联数据
        result.getRecords().forEach(this::fillInboundInfo);

        return result;
    }

    @Override
    public Inbound getInboundById(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BusinessException(404, "入库单不存在");
        }

        // 填充关联数据
        fillInboundInfo(inbound);

        // 查询明细
        LambdaQueryWrapper<InboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundDetail::getInboundId, id);
        List<InboundDetail> details = inboundDetailMapper.selectList(wrapper);

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

        inbound.setDetails(details);

        return inbound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundDTO dto) {
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

        // 生成入库单号: RK_部门编码_YYYYMMDD_流水号
        String inboundNo = generateInboundNo(dept.getDeptCode());

        // 计算总金额
        BigDecimal totalAmount = dto.getDetails().stream()
                .map(detail -> {
                    BigDecimal unitPrice = detail.getUnitPrice() != null ?
                            detail.getUnitPrice() : BigDecimal.ZERO;
                    return unitPrice.multiply(detail.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建入库单
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundNo);
        inbound.setWarehouseId(dto.getWarehouseId());
        inbound.setInboundType(dto.getInboundType());
        inbound.setOperatorId(operatorId);
        inbound.setInboundTime(dto.getInboundTime());
        inbound.setTotalAmount(totalAmount);
        inbound.setRemark(dto.getRemark());

        inboundMapper.insert(inbound);
        log.info("创建入库单: id={}, inboundNo={}", inbound.getId(), inboundNo);

        // 创建入库明细并更新库存
        for (InboundDTO.InboundDetailDTO detailDTO : dto.getDetails()) {
            // 检查物资是否存在
            Material material = materialMapper.selectById(detailDTO.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + detailDTO.getMaterialId());
            }

            // 创建明细
            InboundDetail detail = new InboundDetail();
            detail.setInboundId(inbound.getId());
            detail.setMaterialId(detailDTO.getMaterialId());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());

            BigDecimal amount = detailDTO.getUnitPrice() != null ?
                    detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()) : BigDecimal.ZERO;
            detail.setAmount(amount);
            detail.setRemark(detailDTO.getRemark());

            inboundDetailMapper.insert(detail);

            // 更新库存（增加）
            inventoryService.increaseInventory(
                    dto.getWarehouseId(),
                    detailDTO.getMaterialId(),
                    detailDTO.getQuantity(),
                    inboundNo,
                    inbound.getId(),
                    operatorId
            );

            log.info("入库明细: materialId={}, quantity={}", detailDTO.getMaterialId(), detailDTO.getQuantity());
        }

        log.info("入库单创建成功: inboundNo={}, totalAmount={}", inboundNo, totalAmount);

        return inbound.getId();
    }

    /**
     * 填充入库单关联信息
     */
    private void fillInboundInfo(Inbound inbound) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(inbound.getWarehouseId());
        if (warehouse != null) {
            inbound.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充操作人姓名
        User operator = userMapper.selectById(inbound.getOperatorId());
        if (operator != null) {
            inbound.setOperatorName(operator.getRealName());
        }
    }

    /**
     * 生成入库单号
     */
    private String generateInboundNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RK_" + deptCode + "_" + today + "_";

        // 查询今天最大的流水号
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Inbound::getInboundNo, prefix);
        wrapper.orderByDesc(Inbound::getInboundNo);
        wrapper.last("LIMIT 1");

        Inbound lastInbound = inboundMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastInbound != null) {
            String lastNo = lastInbound.getInboundNo();
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
