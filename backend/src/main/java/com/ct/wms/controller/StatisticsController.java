package com.ct.wms.controller;

import com.ct.wms.common.api.Result;
import com.ct.wms.dto.InboundStatisticsDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.dto.OutboundStatisticsDTO;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 统计报表Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计报表", description = "数据统计、报表接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘统计数据", description = "返回首页仪表盘的各项统计数据")
    public Result<DashboardStatsVO> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        DashboardStatsVO stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    @GetMapping("/inbound")
    @Operation(summary = "获取入库统计数据", description = "获取指定时间范围和仓库的入库统计数据，包含趋势图、分类占比等")
    public Result<InboundStatisticsDTO> getInboundStatistics(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd，默认最近30天）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @Parameter(description = "结束日期（格式：yyyy-MM-dd，默认今天）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            @Parameter(description = "仓库ID（可选，不传则统计所有仓库）")
            @RequestParam(required = false)
            Long warehouseId) {

        log.info("获取入库统计数据: startDate={}, endDate={}, warehouseId={}", startDate, endDate, warehouseId);
        InboundStatisticsDTO statistics = statisticsService.getInboundStatistics(startDate, endDate, warehouseId);
        return Result.success(statistics);
    }

    @GetMapping("/outbound")
    @Operation(summary = "获取出库统计数据", description = "获取指定时间范围和仓库的出库统计数据，包含趋势图、分类占比、类型占比等")
    public Result<OutboundStatisticsDTO> getOutboundStatistics(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd，默认最近30天）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @Parameter(description = "结束日期（格式：yyyy-MM-dd，默认今天）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            @Parameter(description = "仓库ID（可选，不传则统计所有仓库）")
            @RequestParam(required = false)
            Long warehouseId,

            @Parameter(description = "出库类型（可选，1-直接出库，2-申请出库）")
            @RequestParam(required = false)
            Integer outboundType) {

        log.info("获取出库统计数据: startDate={}, endDate={}, warehouseId={}, outboundType={}",
                startDate, endDate, warehouseId, outboundType);
        OutboundStatisticsDTO statistics = statisticsService.getOutboundStatistics(
                startDate, endDate, warehouseId, outboundType);
        return Result.success(statistics);
    }

    @GetMapping("/inventory")
    @Operation(summary = "获取库存统计数据", description = "获取库存统计数据，包含库存总值、预警数量、周转率、Top 10等")
    public Result<InventoryStatisticsDTO> getInventoryStatistics(
            @Parameter(description = "仓库ID（可选，不传则统计所有仓库）")
            @RequestParam(required = false)
            Long warehouseId) {

        log.info("获取库存统计数据: warehouseId={}", warehouseId);
        InventoryStatisticsDTO statistics = statisticsService.getInventoryStatistics(warehouseId);
        return Result.success(statistics);
    }
}
