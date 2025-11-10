package com.ct.wms.controller;

import com.ct.wms.common.api.Result;
import com.ct.wms.dto.DeptDTO;
import com.ct.wms.entity.Dept;
import com.ct.wms.service.DeptService;
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
 * 部门管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门的增删改查接口")
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/tree")
    @Operation(summary = "查询部门树", description = "返回树形结构的部门列表")
    public Result<List<Dept>> listDeptTree() {
        log.info("查询部门树");
        List<Dept> tree = deptService.listDeptTree();
        return Result.success(tree);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有部门", description = "返回平铺列表的所有部门")
    public Result<List<Dept>> listAllDepts() {
        log.info("查询所有部门");
        List<Dept> depts = deptService.listAllDepts();
        return Result.success(depts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询部门详情", description = "根据ID查询部门详细信息")
    public Result<Dept> getDeptById(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        log.info("查询部门详情: id={}", id);
        Dept dept = deptService.getDeptById(id);
        return Result.success(dept);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建部门", description = "管理员创建新部门")
    public Result<Long> createDept(@Validated @RequestBody DeptDTO dto) {
        log.info("创建部门: dto={}", dto);
        Long deptId = deptService.createDept(dto);
        return Result.success(deptId, "创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新部门", description = "管理员更新部门信息")
    public Result<Void> updateDept(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Validated @RequestBody DeptDTO dto) {
        log.info("更新部门: id={}, dto={}", id, dto);
        deptService.updateDept(id, dto);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除部门", description = "管理员删除部门")
    public Result<Void> deleteDept(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        log.info("删除部门: id={}", id);
        deptService.deleteDept(id);
        return Result.success(null, "删除成功");
    }
}
