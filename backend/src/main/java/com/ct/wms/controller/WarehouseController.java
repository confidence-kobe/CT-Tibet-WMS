package com.ct.wms.controller;

import com.ct.wms.common.api.Result;
import com.ct.wms.dto.WarehouseDTO;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "仓库管理", description = "仓库的增删改查接口")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @Operation(summary = "查询仓库列表", description = "支持按部门、状态筛选")
    public Result<List<Warehouse>> listWarehouses(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("查询仓库列表: deptId={}, status={}", deptId, status);
        List<Warehouse> warehouses = warehouseService.listWarehouses(deptId, status);
        return Result.success(warehouses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询仓库详情", description = "根据ID查询仓库详细信息")
    public Result<Warehouse> getWarehouseById(
            @Parameter(description = "仓库ID") @PathVariable Long id) {
        log.info("查询仓库详情: id={}", id);
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return Result.success(warehouse);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "创建仓库", description = "新增仓库信息")
    public Result<Long> createWarehouse(@Validated @RequestBody WarehouseDTO dto) {
        log.info("创建仓库: dto={}", dto);
        Long warehouseId = warehouseService.createWarehouse(dto);
        return Result.success(warehouseId, "创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "更新仓库", description = "修改仓库信息")
    public Result<Void> updateWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id,
            @Validated @RequestBody WarehouseDTO dto) {
        log.info("更新仓库: id={}, dto={}", id, dto);
        warehouseService.updateWarehouse(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除仓库", description = "删除仓库")
    public Result<Void> deleteWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id) {
        log.info("删除仓库: id={}", id);
        warehouseService.deleteWarehouse(id);
        return Result.success("删除成功");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "启用/停用仓库", description = "修改仓库状态")
    public Result<Void> updateStatus(
            @Parameter(description = "仓库ID") @PathVariable Long id,
            @Parameter(description = "状态(0-启用 1-禁用)") @RequestParam Integer status) {
        log.info("更新仓库状态: id={}, status={}", id, status);
        warehouseService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
