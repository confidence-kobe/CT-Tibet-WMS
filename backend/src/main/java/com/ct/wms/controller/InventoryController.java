package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "库存查询、库存流水、库存预警接口")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "分页查询库存列表", description = "支持多条件筛选")
    public Result<PageResult<Inventory>> listInventories(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "物资ID") @RequestParam(required = false) Long materialId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询库存列表: pageNum={}, pageSize={}, warehouseId={}, materialId={}",
                pageNum, pageSize, warehouseId, materialId);

        Page<Inventory> page = inventoryService.listInventories(pageNum, pageSize, warehouseId,
                materialId, keyword);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/logs")
    @Operation(summary = "分页查询库存流水", description = "支持多条件筛选")
    public Result<PageResult<InventoryLog>> listInventoryLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "物资ID") @RequestParam(required = false) Long materialId,
            @Parameter(description = "变动类型") @RequestParam(required = false) Integer changeType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {

        log.info("查询库存流水: pageNum={}, pageSize={}, warehouseId={}, materialId={}, changeType={}",
                pageNum, pageSize, warehouseId, materialId, changeType);

        Page<InventoryLog> page = inventoryService.listInventoryLogs(pageNum, pageSize, warehouseId,
                materialId, changeType, startDate, endDate);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/low-stock-alerts")
    @Operation(summary = "查询低库存预警列表", description = "查询库存低于最低库存的物资")
    public Result<List<Inventory>> listLowStockAlerts(
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId) {

        log.info("查询低库存预警: warehouseId={}", warehouseId);

        List<Inventory> alerts = inventoryService.listLowStockAlerts(warehouseId);

        return Result.success(alerts);
    }
}
