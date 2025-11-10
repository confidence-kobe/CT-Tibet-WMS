package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 统计Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MaterialMapper materialMapper;
    private final WarehouseMapper warehouseMapper;
    private final ApplyMapper applyMapper;
    private final InboundMapper inboundMapper;
    private final OutboundMapper outboundMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryService inventoryService;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 1. 总物资种类数
        stats.setTotalMaterials(materialMapper.selectCount(null));

        // 2. 总仓库数
        stats.setTotalWarehouses(warehouseMapper.selectCount(null));

        // 3. 低库存预警数
        List<Inventory> lowStockAlerts = inventoryService.listLowStockAlerts(null);
        stats.setLowStockAlerts((long) lowStockAlerts.size());

        // 4. 待审批申请数
        LambdaQueryWrapper<Apply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(Apply::getStatus, ApplyStatus.PENDING.getValue());
        stats.setPendingApplies(applyMapper.selectCount(applyWrapper));

        // 5. 本月入库统计
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        LambdaQueryWrapper<Inbound> inboundWrapper = new LambdaQueryWrapper<>();
        inboundWrapper.ge(Inbound::getInboundTime, monthStart);
        inboundWrapper.le(Inbound::getInboundTime, monthEnd);

        List<Inbound> monthInbounds = inboundMapper.selectList(inboundWrapper);
        stats.setMonthInboundCount((long) monthInbounds.size());

        BigDecimal monthInboundAmount = monthInbounds.stream()
                .map(inbound -> inbound.getTotalAmount() != null ? inbound.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthInboundAmount(monthInboundAmount);

        // 6. 本月出库统计
        LambdaQueryWrapper<Outbound> outboundWrapper = new LambdaQueryWrapper<>();
        outboundWrapper.ge(Outbound::getOutboundTime, monthStart);
        outboundWrapper.le(Outbound::getOutboundTime, monthEnd);

        List<Outbound> monthOutbounds = outboundMapper.selectList(outboundWrapper);
        stats.setMonthOutboundCount((long) monthOutbounds.size());

        BigDecimal monthOutboundAmount = monthOutbounds.stream()
                .map(outbound -> outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthOutboundAmount(monthOutboundAmount);

        // 7. 库存总价值（简化计算：数量 * 物资单价）
        // 这里需要关联物资表获取价格，实际应该通过SQL优化
        List<Inventory> allInventories = inventoryMapper.selectList(null);
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Inventory inventory : allInventories) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getPrice() != null) {
                BigDecimal value = material.getPrice().multiply(inventory.getQuantity());
                totalValue = totalValue.add(value);
            }
        }
        stats.setTotalInventoryValue(totalValue);

        log.info("获取仪表盘统计数据: {}", stats);

        return stats;
    }
}
