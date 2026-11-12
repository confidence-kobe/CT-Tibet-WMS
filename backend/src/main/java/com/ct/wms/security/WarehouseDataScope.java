package com.ct.wms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库数据权限（部门隔离）
 *
 * <ul>
 *   <li>系统管理员：可访问所有仓库</li>
 *   <li>其他已登录用户：只能访问本部门的仓库</li>
 *   <li>没有登录上下文（定时任务等系统内部调用）：不限制</li>
 * </ul>
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@Component
@RequiredArgsConstructor
public class WarehouseDataScope {

    private final UserMapper userMapper;
    private final WarehouseMapper warehouseMapper;

    /**
     * 当前用户被限制在哪个部门
     *
     * @return null 表示不限制；否则为用户所在部门ID（用户没有部门时返回 -1，匹配不到任何仓库）
     */
    public Long getRestrictedDeptId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // 定时任务等系统内部调用
            return null;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return null;
        }
        if (!(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return -1L;
        }

        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        User user = userMapper.selectById(userId);
        return user != null && user.getDeptId() != null ? user.getDeptId() : -1L;
    }

    /**
     * 当前用户可访问的仓库ID
     *
     * @return null 表示不限制；否则为可访问的仓库ID列表（可能为空）
     */
    public List<Long> getAccessibleWarehouseIds() {
        Long deptId = getRestrictedDeptId();
        if (deptId == null) {
            return null;
        }
        if (deptId < 0) {
            return Collections.emptyList();
        }
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>()
                        .select(Warehouse::getId)
                        .eq(Warehouse::getDeptId, deptId))
                .stream()
                .map(Warehouse::getId)
                .collect(Collectors.toList());
    }

    /**
     * 给查询条件加上仓库范围限制
     *
     * @param wrapper 查询条件
     * @param column  仓库ID字段
     */
    public <T> void restrict(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column) {
        List<Long> warehouseIds = getAccessibleWarehouseIds();
        if (warehouseIds == null) {
            return;
        }
        if (warehouseIds.isEmpty()) {
            // 没有任何可访问的仓库：查询结果为空
            wrapper.apply("1 = 0");
        } else {
            wrapper.in(column, warehouseIds);
        }
    }

    /**
     * 当前用户能否访问该仓库
     */
    public boolean canAccess(Long warehouseId) {
        Long deptId = getRestrictedDeptId();
        if (deptId == null) {
            return true;
        }
        Warehouse warehouse = warehouseId == null ? null : warehouseMapper.selectById(warehouseId);
        return warehouse != null && deptId.equals(warehouse.getDeptId());
    }

    /**
     * 校验当前用户能否访问该仓库，不能则抛出 403
     *
     * @param warehouseId 仓库ID
     * @param message     无权时的提示
     */
    public void checkAccess(Long warehouseId, String message) {
        if (!canAccess(warehouseId)) {
            throw new BusinessException(403, message);
        }
    }
}
