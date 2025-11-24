package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.ChangePasswordRequest;
import com.ct.wms.dto.UpdateProfileRequest;
import com.ct.wms.dto.UserDTO;
import com.ct.wms.entity.User;
import com.ct.wms.service.UserService;
import com.ct.wms.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户的增删改查接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "分页查询用户列表", description = "支持多条件筛选")
    public Result<PageResult<User>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询用户列表: pageNum={}, pageSize={}, deptId={}, roleId={}",
                pageNum, pageSize, deptId, roleId);

        Page<User> page = userService.listUsers(pageNum, pageSize, deptId, roleId, status, keyword);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详细信息")
    public Result<User> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("查询用户详情: id={}", id);
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public Result<Long> createUser(@Validated @RequestBody UserDTO dto) {
        log.info("创建用户: dto={}", dto);
        Long userId = userService.createUser(dto);
        return Result.success(userId, "创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Validated @RequestBody UserDTO dto) {
        log.info("更新用户: id={}, dto={}", id, dto);
        userService.updateUser(id, dto);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "管理员删除用户（软删除）")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("删除用户: id={}", id);
        userService.deleteUser(id);
        return Result.success(null, "删除成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态: 0-启用 1-禁用") @RequestParam Integer status) {
        log.info("更新用户状态: id={}, status={}", id, status);
        userService.updateUserStatus(id, status);
        return Result.success(null, "更新成功");
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        log.info("重置用户密码: id={}", id);
        userService.resetPassword(id, newPassword);
        return Result.success(null, "重置成功");
    }

    // ========== 个人中心相关接口 ==========

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public Result<UserProfileVO> getProfile() {
        log.info("获取个人信息");
        UserProfileVO profile = userService.getCurrentUserProfile();
        return Result.success(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "更新当前用户的个人信息（真实姓名、手机号、邮箱）")
    public Result<Void> updateProfile(@Validated @RequestBody UpdateProfileRequest request) {
        log.info("更新个人信息: request={}", request);
        userService.updateCurrentUserProfile(request);
        return Result.success(null, "更新成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        log.info("修改密码");
        userService.changeCurrentUserPassword(request);
        return Result.success(null, "修改成功");
    }
}
