package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.dto.InboundStatisticsDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.dto.OutboundStatisticsDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final InboundDetailMapper inboundDetailMapper;
    private final OutboundMapper outboundMapper;
    private final OutboundDetailMapper outboundDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryService inventoryService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    @Override
    public InboundStatisticsDTO getInboundStatistics(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        // 默认日期范围：最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取入库统计数据: startDate={}, endDate={}, warehouseId={}", startDate, endDate, warehouseId);

        InboundStatisticsDTO dto = new InboundStatisticsDTO();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 构建查询条件
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Inbound::getInboundTime, startDateTime)
               .le(Inbound::getInboundTime, endDateTime);
        if (warehouseId != null) {
            wrapper.eq(Inbound::getWarehouseId, warehouseId);
        }

        // 查询入库单列表
        List<Inbound> inboundList = inboundMapper.selectList(wrapper);

        // 1. 计算总次数、总数量、总金额
        dto.setTotalCount(inboundList.size());

        BigDecimal totalAmount = inboundList.stream()
                .map(inbound -> inbound.getTotalAmount() != null ? inbound.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(totalAmount);

        // 计算总数量（需要查询明细）
        List<Long> inboundIds = inboundList.stream().map(Inbound::getId).collect(Collectors.toList());
        BigDecimal totalQuantity = BigDecimal.ZERO;
        if (!inboundIds.isEmpty()) {
            LambdaQueryWrapper<InboundDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(InboundDetail::getInboundId, inboundIds);
            List<InboundDetail> details = inboundDetailMapper.selectList(detailWrapper);
            totalQuantity = details.stream()
                    .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        dto.setTotalQuantity(totalQuantity);

        // 2. 计算日均入库数量
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal avgDaily = daysBetween > 0
                ? totalQuantity.divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        dto.setAvgDaily(avgDaily);

        // 3. 生成趋势图数据
        dto.setTrendData(generateInboundTrendData(inboundList, startDate, endDate));

        // 4. 按仓库统计
        dto.setWarehouseData(generateWarehouseData(inboundList));

        // 5. 物资分类占比
        dto.setCategoryData(generateInboundCategoryData(inboundIds));

        // 6. 每日明细表格
        dto.setDailyData(generateInboundDailyData(inboundList, startDate, endDate));

        log.info("入库统计数据生成完成: totalCount={}, totalQuantity={}, totalAmount={}",
                dto.getTotalCount(), dto.getTotalQuantity(), dto.getTotalAmount());

        return dto;
    }

    @Override
    public OutboundStatisticsDTO getOutboundStatistics(LocalDate startDate, LocalDate endDate,
                                                       Long warehouseId, Integer outboundType) {
        // 默认日期范围：最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取出库统计数据: startDate={}, endDate={}, warehouseId={}, outboundType={}",
                startDate, endDate, warehouseId, outboundType);

        OutboundStatisticsDTO dto = new OutboundStatisticsDTO();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 构建查询条件
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Outbound::getOutboundTime, startDateTime)
               .le(Outbound::getOutboundTime, endDateTime)
               .isNotNull(Outbound::getOutboundTime); // 只统计已出库的

        if (warehouseId != null) {
            wrapper.eq(Outbound::getWarehouseId, warehouseId);
        }
        if (outboundType != null) {
            wrapper.eq(Outbound::getSource, outboundType);
        }

        // 查询出库单列表
        List<Outbound> outboundList = outboundMapper.selectList(wrapper);

        // 1. 计算总次数、总数量、总金额
        dto.setTotalCount(outboundList.size());

        BigDecimal totalAmount = outboundList.stream()
                .map(outbound -> outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(totalAmount);

        // 计算总数量（需要查询明细）
        List<Long> outboundIds = outboundList.stream().map(Outbound::getId).collect(Collectors.toList());
        BigDecimal totalQuantity = BigDecimal.ZERO;
        if (!outboundIds.isEmpty()) {
            LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(OutboundDetail::getOutboundId, outboundIds);
            List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);
            totalQuantity = details.stream()
                    .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        dto.setTotalQuantity(totalQuantity);

        // 2. 计算日均出库数量
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal avgDaily = daysBetween > 0
                ? totalQuantity.divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        dto.setAvgDaily(avgDaily);

        // 3. 生成趋势图数据
        dto.setTrendData(generateOutboundTrendData(outboundList, startDate, endDate));

        // 4. 按仓库统计
        dto.setWarehouseData(generateOutboundWarehouseData(outboundList));

        // 5. 物资分类占比
        dto.setCategoryData(generateOutboundCategoryData(outboundIds));

        // 6. 出库类型占比
        dto.setTypeData(generateOutboundTypeData(outboundList));

        // 7. 每日明细表格
        dto.setDailyData(generateOutboundDailyData(outboundList, startDate, endDate));

        log.info("出库统计数据生成完成: totalCount={}, totalQuantity={}, totalAmount={}",
                dto.getTotalCount(), dto.getTotalQuantity(), dto.getTotalAmount());

        return dto;
    }

    @Override
    public InventoryStatisticsDTO getInventoryStatistics(Long warehouseId) {
        log.info("获取库存统计数据: warehouseId={}", warehouseId);

        InventoryStatisticsDTO dto = new InventoryStatisticsDTO();

        // 构建查询条件
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Inventory::getQuantity, BigDecimal.ZERO); // 只统计有库存的
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }

        List<Inventory> inventoryList = inventoryMapper.selectList(wrapper);

        // 1. 物资种类数（去重）
        Set<Long> materialIds = inventoryList.stream()
                .map(Inventory::getMaterialId)
                .collect(Collectors.toSet());
        dto.setMaterialCount(materialIds.size());

        // 2. 库存总值
        BigDecimal totalValue = BigDecimal.ZERO;
        int warningCount = 0;

        for (Inventory inventory : inventoryList) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getPrice() != null) {
                BigDecimal value = material.getPrice().multiply(inventory.getQuantity());
                totalValue = totalValue.add(value);

                // 统计预警数量
                if (material.getMinStock() != null &&
                    inventory.getQuantity().compareTo(material.getMinStock()) < 0) {
                    warningCount++;
                }
            }
        }
        dto.setTotalValue(totalValue);
        dto.setWarningCount(warningCount);

        // 3. 计算库存周转率（次/月）- 简化版：最近30天出库量 / 平均库存
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        BigDecimal turnoverRate = calculateTurnoverRate(startDate, endDate, warehouseId);
        dto.setTurnoverRate(turnoverRate);

        // 4. 各仓库库存金额
        dto.setWarehouseData(generateInventoryWarehouseData(inventoryList));

        // 5. 物资分类占比
        dto.setCategoryData(generateInventoryCategoryData(inventoryList));

        // 6. 预警趋势数据（最近7天）
        dto.setWarningTrendData(generateWarningTrendData(warehouseId));

        // 7. Top 10库存占用
        dto.setTopStocks(generateTopStocks(inventoryList));

        log.info("库存统计数据生成完成: materialCount={}, totalValue={}, warningCount={}",
                dto.getMaterialCount(), dto.getTotalValue(), dto.getWarningCount());

        return dto;
    }

    // ============================= 辅助方法 =============================

    /**
     * 生成入库趋势图数据
     */
    private InboundStatisticsDTO.TrendData generateInboundTrendData(
            List<Inbound> inboundList, LocalDate startDate, LocalDate endDate) {

        InboundStatisticsDTO.TrendData trendData = new InboundStatisticsDTO.TrendData();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> quantities = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        // 按日期分组
        Map<LocalDate, List<Inbound>> groupedByDate = inboundList.stream()
                .collect(Collectors.groupingBy(inbound ->
                    inbound.getInboundTime().toLocalDate()));

        // 生成完整日期序列
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current.format(DATE_FORMATTER));

            List<Inbound> dayInbounds = groupedByDate.getOrDefault(current, Collections.emptyList());

            // 计算当日数量和金额
            BigDecimal dayAmount = dayInbounds.stream()
                    .map(inbound -> inbound.getTotalAmount() != null ? inbound.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount);

            // 查询当日明细数量
            List<Long> dayInboundIds = dayInbounds.stream().map(Inbound::getId).collect(Collectors.toList());
            BigDecimal dayQuantity = BigDecimal.ZERO;
            if (!dayInboundIds.isEmpty()) {
                LambdaQueryWrapper<InboundDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.in(InboundDetail::getInboundId, dayInboundIds);
                List<InboundDetail> details = inboundDetailMapper.selectList(detailWrapper);
                dayQuantity = details.stream()
                        .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            quantities.add(dayQuantity);

            current = current.plusDays(1);
        }

        trendData.setDates(dates);
        trendData.setQuantities(quantities);
        trendData.setAmounts(amounts);
        return trendData;
    }

    /**
     * 生成入库按仓库统计数据
     */
    private List<InboundStatisticsDTO.ChartData> generateWarehouseData(List<Inbound> inboundList) {
        Map<Long, BigDecimal> warehouseAmountMap = inboundList.stream()
                .collect(Collectors.groupingBy(
                    Inbound::getWarehouseId,
                    Collectors.reducing(BigDecimal.ZERO,
                        inbound -> inbound.getTotalAmount() != null ? inbound.getTotalAmount() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));

        List<InboundStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : warehouseAmountMap.entrySet()) {
            Warehouse warehouse = warehouseMapper.selectById(entry.getKey());
            if (warehouse != null) {
                InboundStatisticsDTO.ChartData chartData = new InboundStatisticsDTO.ChartData();
                chartData.setName(warehouse.getWarehouseName());
                chartData.setValue(entry.getValue());
                chartDataList.add(chartData);
            }
        }

        // 按金额降序排序
        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成入库物资分类占比数据
     */
    private List<InboundStatisticsDTO.ChartData> generateInboundCategoryData(List<Long> inboundIds) {
        if (inboundIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<InboundDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(InboundDetail::getInboundId, inboundIds);
        List<InboundDetail> details = inboundDetailMapper.selectList(detailWrapper);

        // 按物资ID分组，计算每个物资的金额
        Map<Long, BigDecimal> materialAmountMap = details.stream()
                .collect(Collectors.groupingBy(
                    InboundDetail::getMaterialId,
                    Collectors.reducing(BigDecimal.ZERO,
                        detail -> detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));

        // 按物资分类汇总
        Map<String, BigDecimal> categoryAmountMap = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : materialAmountMap.entrySet()) {
            Material material = materialMapper.selectById(entry.getKey());
            if (material != null && material.getCategory() != null) {
                categoryAmountMap.merge(material.getCategory(), entry.getValue(), BigDecimal::add);
            }
        }

        List<InboundStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryAmountMap.entrySet()) {
            InboundStatisticsDTO.ChartData chartData = new InboundStatisticsDTO.ChartData();
            chartData.setName(entry.getKey());
            chartData.setValue(entry.getValue());
            chartDataList.add(chartData);
        }

        // 按金额降序排序
        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成入库每日明细数据
     */
    private List<InboundStatisticsDTO.DailyData> generateInboundDailyData(
            List<Inbound> inboundList, LocalDate startDate, LocalDate endDate) {

        List<InboundStatisticsDTO.DailyData> dailyDataList = new ArrayList<>();

        // 按日期分组
        Map<LocalDate, List<Inbound>> groupedByDate = inboundList.stream()
                .collect(Collectors.groupingBy(inbound ->
                    inbound.getInboundTime().toLocalDate()));

        // 生成完整日期序列
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Inbound> dayInbounds = groupedByDate.getOrDefault(current, Collections.emptyList());

            InboundStatisticsDTO.DailyData dailyData = new InboundStatisticsDTO.DailyData();
            dailyData.setDate(current.format(DATE_FORMATTER));
            dailyData.setCount(dayInbounds.size());

            BigDecimal dayAmount = dayInbounds.stream()
                    .map(inbound -> inbound.getTotalAmount() != null ? inbound.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyData.setAmount(dayAmount);

            // 查询当日明细数量
            List<Long> dayInboundIds = dayInbounds.stream().map(Inbound::getId).collect(Collectors.toList());
            BigDecimal dayQuantity = BigDecimal.ZERO;
            if (!dayInboundIds.isEmpty()) {
                LambdaQueryWrapper<InboundDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.in(InboundDetail::getInboundId, dayInboundIds);
                List<InboundDetail> details = inboundDetailMapper.selectList(detailWrapper);
                dayQuantity = details.stream()
                        .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            dailyData.setQuantity(dayQuantity);

            dailyDataList.add(dailyData);
            current = current.plusDays(1);
        }

        return dailyDataList;
    }

    /**
     * 生成出库趋势图数据
     */
    private OutboundStatisticsDTO.TrendData generateOutboundTrendData(
            List<Outbound> outboundList, LocalDate startDate, LocalDate endDate) {

        OutboundStatisticsDTO.TrendData trendData = new OutboundStatisticsDTO.TrendData();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> quantities = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        // 按日期分组
        Map<LocalDate, List<Outbound>> groupedByDate = outboundList.stream()
                .collect(Collectors.groupingBy(outbound ->
                    outbound.getOutboundTime().toLocalDate()));

        // 生成完整日期序列
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current.format(DATE_FORMATTER));

            List<Outbound> dayOutbounds = groupedByDate.getOrDefault(current, Collections.emptyList());

            // 计算当日金额
            BigDecimal dayAmount = dayOutbounds.stream()
                    .map(outbound -> outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount);

            // 查询当日明细数量
            List<Long> dayOutboundIds = dayOutbounds.stream().map(Outbound::getId).collect(Collectors.toList());
            BigDecimal dayQuantity = BigDecimal.ZERO;
            if (!dayOutboundIds.isEmpty()) {
                LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.in(OutboundDetail::getOutboundId, dayOutboundIds);
                List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);
                dayQuantity = details.stream()
                        .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            quantities.add(dayQuantity);

            current = current.plusDays(1);
        }

        trendData.setDates(dates);
        trendData.setQuantities(quantities);
        trendData.setAmounts(amounts);
        return trendData;
    }

    /**
     * 生成出库按仓库统计数据
     */
    private List<OutboundStatisticsDTO.ChartData> generateOutboundWarehouseData(List<Outbound> outboundList) {
        Map<Long, BigDecimal> warehouseAmountMap = outboundList.stream()
                .collect(Collectors.groupingBy(
                    Outbound::getWarehouseId,
                    Collectors.reducing(BigDecimal.ZERO,
                        outbound -> outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));

        List<OutboundStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : warehouseAmountMap.entrySet()) {
            Warehouse warehouse = warehouseMapper.selectById(entry.getKey());
            if (warehouse != null) {
                OutboundStatisticsDTO.ChartData chartData = new OutboundStatisticsDTO.ChartData();
                chartData.setName(warehouse.getWarehouseName());
                chartData.setValue(entry.getValue());
                chartDataList.add(chartData);
            }
        }

        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成出库物资分类占比数据
     */
    private List<OutboundStatisticsDTO.ChartData> generateOutboundCategoryData(List<Long> outboundIds) {
        if (outboundIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OutboundDetail::getOutboundId, outboundIds);
        List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);

        // 按物资ID分组，计算每个物资的数量
        Map<Long, BigDecimal> materialQuantityMap = details.stream()
                .collect(Collectors.groupingBy(
                    OutboundDetail::getMaterialId,
                    Collectors.reducing(BigDecimal.ZERO,
                        detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));

        // 按物资分类汇总
        Map<String, BigDecimal> categoryQuantityMap = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : materialQuantityMap.entrySet()) {
            Material material = materialMapper.selectById(entry.getKey());
            if (material != null && material.getCategory() != null) {
                categoryQuantityMap.merge(material.getCategory(), entry.getValue(), BigDecimal::add);
            }
        }

        List<OutboundStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryQuantityMap.entrySet()) {
            OutboundStatisticsDTO.ChartData chartData = new OutboundStatisticsDTO.ChartData();
            chartData.setName(entry.getKey());
            chartData.setValue(entry.getValue());
            chartDataList.add(chartData);
        }

        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成出库类型占比数据
     */
    private List<OutboundStatisticsDTO.ChartData> generateOutboundTypeData(List<Outbound> outboundList) {
        Map<String, BigDecimal> typeAmountMap = new HashMap<>();

        for (Outbound outbound : outboundList) {
            String typeName = outbound.getSource() == OutboundSource.DIRECT ? "直接出库" : "申请出库";
            BigDecimal amount = outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO;
            typeAmountMap.merge(typeName, amount, BigDecimal::add);
        }

        List<OutboundStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : typeAmountMap.entrySet()) {
            OutboundStatisticsDTO.ChartData chartData = new OutboundStatisticsDTO.ChartData();
            chartData.setName(entry.getKey());
            chartData.setValue(entry.getValue());
            chartDataList.add(chartData);
        }

        return chartDataList;
    }

    /**
     * 生成出库每日明细数据
     */
    private List<OutboundStatisticsDTO.DailyData> generateOutboundDailyData(
            List<Outbound> outboundList, LocalDate startDate, LocalDate endDate) {

        List<OutboundStatisticsDTO.DailyData> dailyDataList = new ArrayList<>();

        // 按日期分组
        Map<LocalDate, List<Outbound>> groupedByDate = outboundList.stream()
                .collect(Collectors.groupingBy(outbound ->
                    outbound.getOutboundTime().toLocalDate()));

        // 生成完整日期序列
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Outbound> dayOutbounds = groupedByDate.getOrDefault(current, Collections.emptyList());

            OutboundStatisticsDTO.DailyData dailyData = new OutboundStatisticsDTO.DailyData();
            dailyData.setDate(current.format(DATE_FORMATTER));
            dailyData.setCount(dayOutbounds.size());

            BigDecimal dayAmount = dayOutbounds.stream()
                    .map(outbound -> outbound.getTotalAmount() != null ? outbound.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyData.setAmount(dayAmount);

            // 查询当日明细数量
            List<Long> dayOutboundIds = dayOutbounds.stream().map(Outbound::getId).collect(Collectors.toList());
            BigDecimal dayQuantity = BigDecimal.ZERO;
            if (!dayOutboundIds.isEmpty()) {
                LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.in(OutboundDetail::getOutboundId, dayOutboundIds);
                List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);
                dayQuantity = details.stream()
                        .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            dailyData.setQuantity(dayQuantity);

            dailyDataList.add(dailyData);
            current = current.plusDays(1);
        }

        return dailyDataList;
    }

    /**
     * 计算库存周转率（次/月）
     * 简化版：最近N天出库量 / 平均库存
     */
    private BigDecimal calculateTurnoverRate(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        // 计算期间出库总量
        LambdaQueryWrapper<Outbound> outboundWrapper = new LambdaQueryWrapper<>();
        outboundWrapper.ge(Outbound::getOutboundTime, startDate.atStartOfDay())
                       .le(Outbound::getOutboundTime, endDate.atTime(23, 59, 59))
                       .isNotNull(Outbound::getOutboundTime);
        if (warehouseId != null) {
            outboundWrapper.eq(Outbound::getWarehouseId, warehouseId);
        }

        List<Outbound> outbounds = outboundMapper.selectList(outboundWrapper);
        List<Long> outboundIds = outbounds.stream().map(Outbound::getId).collect(Collectors.toList());

        BigDecimal totalOutboundQuantity = BigDecimal.ZERO;
        if (!outboundIds.isEmpty()) {
            LambdaQueryWrapper<OutboundDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(OutboundDetail::getOutboundId, outboundIds);
            List<OutboundDetail> details = outboundDetailMapper.selectList(detailWrapper);
            totalOutboundQuantity = details.stream()
                    .map(detail -> detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 计算平均库存
        LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
        inventoryWrapper.gt(Inventory::getQuantity, BigDecimal.ZERO);
        if (warehouseId != null) {
            inventoryWrapper.eq(Inventory::getWarehouseId, warehouseId);
        }
        List<Inventory> inventories = inventoryMapper.selectList(inventoryWrapper);

        BigDecimal totalInventory = inventories.stream()
                .map(Inventory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 周转率 = 出库量 / 平均库存 * (30天 / 统计天数)
        if (totalInventory.compareTo(BigDecimal.ZERO) > 0) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            BigDecimal ratio = totalOutboundQuantity.divide(totalInventory, 4, RoundingMode.HALF_UP);
            // 转换为月度周转率
            BigDecimal monthlyRatio = ratio.multiply(BigDecimal.valueOf(30))
                    .divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP);
            return monthlyRatio;
        }

        return BigDecimal.ZERO;
    }

    /**
     * 生成库存按仓库统计数据
     */
    private List<InventoryStatisticsDTO.ChartData> generateInventoryWarehouseData(List<Inventory> inventoryList) {
        Map<Long, BigDecimal> warehouseValueMap = new HashMap<>();

        for (Inventory inventory : inventoryList) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getPrice() != null) {
                BigDecimal value = material.getPrice().multiply(inventory.getQuantity());
                warehouseValueMap.merge(inventory.getWarehouseId(), value, BigDecimal::add);
            }
        }

        List<InventoryStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : warehouseValueMap.entrySet()) {
            Warehouse warehouse = warehouseMapper.selectById(entry.getKey());
            if (warehouse != null) {
                InventoryStatisticsDTO.ChartData chartData = new InventoryStatisticsDTO.ChartData();
                chartData.setName(warehouse.getWarehouseName());
                chartData.setValue(entry.getValue());
                chartDataList.add(chartData);
            }
        }

        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成库存物资分类占比数据
     */
    private List<InventoryStatisticsDTO.ChartData> generateInventoryCategoryData(List<Inventory> inventoryList) {
        Map<String, BigDecimal> categoryValueMap = new HashMap<>();

        for (Inventory inventory : inventoryList) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getCategory() != null && material.getPrice() != null) {
                BigDecimal value = material.getPrice().multiply(inventory.getQuantity());
                categoryValueMap.merge(material.getCategory(), value, BigDecimal::add);
            }
        }

        List<InventoryStatisticsDTO.ChartData> chartDataList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryValueMap.entrySet()) {
            InventoryStatisticsDTO.ChartData chartData = new InventoryStatisticsDTO.ChartData();
            chartData.setName(entry.getKey());
            chartData.setValue(entry.getValue());
            chartDataList.add(chartData);
        }

        chartDataList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return chartDataList;
    }

    /**
     * 生成预警趋势数据（最近7天）
     */
    private InventoryStatisticsDTO.WarningTrendData generateWarningTrendData(Long warehouseId) {
        InventoryStatisticsDTO.WarningTrendData trendData = new InventoryStatisticsDTO.WarningTrendData();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 简化版：假设每天预警数量相同（实际应该从历史日志获取）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        // 查询当前预警数量
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Inventory::getQuantity, BigDecimal.ZERO);
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }
        List<Inventory> inventoryList = inventoryMapper.selectList(wrapper);

        int currentWarningCount = 0;
        for (Inventory inventory : inventoryList) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getMinStock() != null &&
                inventory.getQuantity().compareTo(material.getMinStock()) < 0) {
                currentWarningCount++;
            }
        }

        // 生成7天数据（简化版：模拟趋势）
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current.format(DATE_FORMATTER));
            // 简化处理：最后一天为实际值，之前的天数模拟递减
            long daysFromEnd = java.time.temporal.ChronoUnit.DAYS.between(current, endDate);
            int dayCount = Math.max(0, currentWarningCount - (int)daysFromEnd);
            counts.add(dayCount);
            current = current.plusDays(1);
        }

        trendData.setDates(dates);
        trendData.setCounts(counts);
        return trendData;
    }

    /**
     * 生成Top 10库存占用数据
     */
    private List<InventoryStatisticsDTO.TopStockData> generateTopStocks(List<Inventory> inventoryList) {
        List<InventoryStatisticsDTO.TopStockData> topStockList = new ArrayList<>();

        for (Inventory inventory : inventoryList) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getPrice() != null) {
                InventoryStatisticsDTO.TopStockData topStock = new InventoryStatisticsDTO.TopStockData();
                topStock.setMaterialName(material.getMaterialName());
                topStock.setStock(inventory.getQuantity());
                topStock.setValue(material.getPrice().multiply(inventory.getQuantity()));
                topStockList.add(topStock);
            }
        }

        // 按库存金额降序排序，取前10
        topStockList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return topStockList.stream().limit(10).collect(Collectors.toList());
    }
}
