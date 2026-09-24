package com.ct.wms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.constant.RoleCode;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限辅助类
 * <p>
 * 统一计算当前登录用户可访问的数据范围：
 * 系统管理员可查看全部部门；其他角色只能查看本部门仓库的数据。
 * 用户与角色信息每次从数据库读取，避免依赖请求上下文中可能缺失的字段。
 *
 * @author CT Development Team
 */
@Component
@RequiredArgsConstructor
public class DataScopeHelper {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final WarehouseMapper warehouseMapper;

    /**
     * 获取当前登录用户（从数据库读取）
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BusinessException(401, "用户未登录");
        }
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    /**
     * 获取当前用户的角色编码（从数据库读取）
     */
    public String getCurrentRoleCode() {
        return getRoleCode(getCurrentUser());
    }

    /**
     * 当前用户是否为系统管理员
     */
    public boolean isAdmin() {
        return RoleCode.is(getCurrentRoleCode(), RoleCode.ADMIN);
    }

    /**
     * 计算当前用户可查看的仓库范围
     */
    public WarehouseScope currentWarehouseScope() {
        User user = getCurrentUser();
        if (RoleCode.is(getRoleCode(user), RoleCode.ADMIN)) {
            return WarehouseScope.all();
        }
        if (user.getDeptId() == null) {
            return WarehouseScope.of(null);
        }
        List<Long> warehouseIds = warehouseMapper.selectList(
                        new LambdaQueryWrapper<Warehouse>()
                                .select(Warehouse::getId)
                                .eq(Warehouse::getDeptId, user.getDeptId()))
                .stream()
                .map(Warehouse::getId)
                .collect(Collectors.toList());
        return WarehouseScope.of(warehouseIds);
    }

    /**
     * 解析查询范围：指定了仓库时校验访问权限并只查该仓库，否则使用当前用户的全部可见仓库
     *
     * @param warehouseId 前端传入的仓库ID（可选）
     * @throws BusinessException 403 无权访问该仓库
     */
    public WarehouseScope resolveWarehouseScope(Long warehouseId) {
        WarehouseScope scope = currentWarehouseScope();
        if (warehouseId == null) {
            return scope;
        }
        checkWarehouseAccess(scope, warehouseId);
        return WarehouseScope.of(Collections.singletonList(warehouseId));
    }

    /**
     * 校验当前用户是否可以访问指定仓库
     *
     * @throws BusinessException 403 无权访问该仓库
     */
    public void checkWarehouseAccess(Long warehouseId) {
        checkWarehouseAccess(currentWarehouseScope(), warehouseId);
    }

    private void checkWarehouseAccess(WarehouseScope scope, Long warehouseId) {
        if (!scope.contains(warehouseId)) {
            throw new BusinessException(403, "无权访问其他部门的仓库数据");
        }
    }

    private String getRoleCode(User user) {
        if (user.getRoleId() == null) {
            return null;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role == null ? null : role.getRoleCode();
    }
}
