package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.security.DataScopeHelper;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.utils.IdGenerator;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    private final IdGenerator idGenerator;
    private final DataScopeHelper dataScopeHelper;

    @Override
    public Page<Inbound> listInbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                                     Integer inboundType, String startDate, String endDate,
                                     Long operatorId, String keyword) {
        Page<Inbound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();

        // 部门数据隔离：系统管理员查看全部，其他角色只能查看本部门仓库
        dataScopeHelper.resolveWarehouseScope(warehouseId).applyTo(wrapper, Inbound::getWarehouseId);

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

        // 填充关联数据（防御性检查，避免空指针）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillInboundInfo);
            fillInboundDetailsBatch(result.getRecords());
        }

        return result;
    }

    @Override
    public Inbound getInboundById(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BusinessException(404, "入库单不存在");
        }

        // 部门数据隔离：只能查看本部门仓库的单据
        dataScopeHelper.checkWarehouseAccess(inbound.getWarehouseId());

        // 填充关联数据和明细
        fillInboundInfo(inbound);
        fillInboundDetailsBatch(Collections.singletonList(inbound));

        return inbound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundDTO dto) {
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
        inbound.setOperatorName(operator != null ? operator.getRealName() : null);
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
            detail.setMaterialName(material.getMaterialName());
            detail.setMaterialCode(material.getMaterialCode());
            detail.setSpec(material.getSpec());
            detail.setUnit(material.getUnit());
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
     * 批量填充单据明细及物资信息（列表页用于展示"XX等N项"）
     */
    private void fillInboundDetailsBatch(List<Inbound> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> ids = records.stream().map(Inbound::getId).collect(Collectors.toList());
        Map<Long, List<InboundDetail>> detailsById = inboundDetailMapper.selectList(
                        new LambdaQueryWrapper<InboundDetail>().in(InboundDetail::getInboundId, ids))
                .stream()
                .collect(Collectors.groupingBy(InboundDetail::getInboundId));

        Set<Long> materialIds = detailsById.values().stream()
                .flatMap(List::stream)
                .map(InboundDetail::getMaterialId)
                .collect(Collectors.toSet());
        Map<Long, Material> materialMap = materialIds.isEmpty() ? Map.of()
                : materialMapper.selectBatchIds(materialIds).stream()
                        .collect(Collectors.toMap(Material::getId, Function.identity()));

        for (Inbound record : records) {
            List<InboundDetail> details = detailsById.getOrDefault(record.getId(), Collections.emptyList());
            for (InboundDetail detail : details) {
                Material material = materialMap.get(detail.getMaterialId());
                if (material != null) {
                    detail.setMaterialName(material.getMaterialName());
                    detail.setMaterialCode(material.getMaterialCode());
                    detail.setSpec(material.getSpec());
                    detail.setUnit(material.getUnit());
                }
            }
            record.setDetails(details);
        }
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
     * 生成入库单号（线程安全）
     * 注意：实际生产环境建议使用分布式ID生成器或数据库序列
     */
    private String generateInboundNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RK_" + deptCode + "_" + today + "_";
        // 使用雪花算法生成的ID作为流水号
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
