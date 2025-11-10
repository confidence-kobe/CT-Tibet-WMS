package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.RoleDTO;
import com.ct.wms.entity.Role;
import com.ct.wms.service.RoleService;
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
 * 角色管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色的增删改查接口")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询角色列表", description = "支持关键词搜索")
    public Result<PageResult<Role>> listRoles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询角色列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);

        Page<Role> page = roleService.listRoles(pageNum, pageSize, keyword);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有角色", description = "不分页，返回所有启用的角色")
    public Result<List<Role>> listAllRoles() {
        log.info("查询所有角色");
        List<Role> roles = roleService.listAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询角色详情", description = "根据ID查询角色详细信息")
    public Result<Role> getRoleById(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        log.info("查询角色详情: id={}", id);
        Role role = roleService.getRoleById(id);
        return Result.success(role);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建角色", description = "管理员创建新角色")
    public Result<Long> createRole(@Validated @RequestBody RoleDTO dto) {
        log.info("创建角色: dto={}", dto);
        Long roleId = roleService.createRole(dto);
        return Result.success(roleId, "创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新角色", description = "管理员更新角色信息")
    public Result<Void> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Validated @RequestBody RoleDTO dto) {
        log.info("更新角色: id={}, dto={}", id, dto);
        roleService.updateRole(id, dto);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除角色", description = "管理员删除角色")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        log.info("删除角色: id={}", id);
        roleService.deleteRole(id);
        return Result.success(null, "删除成功");
    }
}
