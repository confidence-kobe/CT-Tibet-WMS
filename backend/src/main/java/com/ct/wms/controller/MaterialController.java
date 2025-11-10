package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.MaterialDTO;
import com.ct.wms.dto.MaterialQueryDTO;
import com.ct.wms.entity.Material;
import com.ct.wms.service.MaterialService;
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
 * 物资管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "物资管理", description = "物资的增删改查接口")
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    @Operation(summary = "分页查询物资列表", description = "支持按类别、状态、关键词筛选")
    public Result<PageResult<Material>> listMaterials(MaterialQueryDTO queryDTO) {
        log.info("查询物资列表: queryDTO={}", queryDTO);
        Page<Material> page = materialService.listMaterials(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询物资详情", description = "根据ID查询物资详细信息")
    public Result<Material> getMaterialById(
            @Parameter(description = "物资ID") @PathVariable Long id) {
        log.info("查询物资详情: id={}", id);
        Material material = materialService.getMaterialById(id);
        return Result.success(material);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "创建物资", description = "新增物资信息")
    public Result<Long> createMaterial(@Validated @RequestBody MaterialDTO dto) {
        log.info("创建物资: dto={}", dto);
        Long materialId = materialService.createMaterial(dto);
        return Result.success(materialId, "创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "更新物资", description = "修改物资信息")
    public Result<Void> updateMaterial(
            @Parameter(description = "物资ID") @PathVariable Long id,
            @Validated @RequestBody MaterialDTO dto) {
        log.info("更新物资: id={}, dto={}", id, dto);
        materialService.updateMaterial(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "删除物资", description = "软删除物资（有库存的物资不能删除）")
    public Result<Void> deleteMaterial(
            @Parameter(description = "物资ID") @PathVariable Long id) {
        log.info("删除物资: id={}", id);
        materialService.deleteMaterial(id);
        return Result.success("删除成功");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "启用/停用物资", description = "修改物资状态")
    public Result<Void> updateStatus(
            @Parameter(description = "物资ID") @PathVariable Long id,
            @Parameter(description = "状态(0-启用 1-停用)") @RequestParam Integer status) {
        log.info("更新物资状态: id={}, status={}", id, status);
        materialService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @GetMapping("/categories")
    @Operation(summary = "获取物资类别列表", description = "获取所有物资类别（去重）")
    public Result<List<String>> getCategories() {
        log.info("查询物资类别列表");
        List<String> categories = materialService.getCategories();
        return Result.success(categories);
    }
}
