package com.ct.wms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.Collections;
import java.util.List;

/**
 * 仓库数据范围
 * <p>
 * 表示当前用户可以查看的仓库集合，用于实现部门级数据隔离：
 * <ul>
 *     <li>{@link #all()}：不限制（系统管理员查看全部仓库）</li>
 *     <li>{@link #of(List)}：仅限指定仓库（空列表表示无任何可见仓库）</li>
 * </ul>
 *
 * @author CT Development Team
 */
public final class WarehouseScope {

    private static final WarehouseScope ALL = new WarehouseScope(null);

    /** null 表示不限制 */
    private final List<Long> warehouseIds;

    private WarehouseScope(List<Long> warehouseIds) {
        this.warehouseIds = warehouseIds;
    }

    public static WarehouseScope all() {
        return ALL;
    }

    public static WarehouseScope of(List<Long> warehouseIds) {
        return new WarehouseScope(warehouseIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(warehouseIds));
    }

    public boolean isAll() {
        return warehouseIds == null;
    }

    public boolean contains(Long warehouseId) {
        return isAll() || (warehouseId != null && warehouseIds.contains(warehouseId));
    }

    /**
     * 将数据范围作为查询条件追加到 wrapper 上
     *
     * @param wrapper 查询条件
     * @param column  仓库ID字段
     */
    public <T> LambdaQueryWrapper<T> applyTo(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column) {
        if (isAll()) {
            return wrapper;
        }
        if (warehouseIds.isEmpty()) {
            // 无可见仓库：返回空结果
            wrapper.apply("1 = 0");
        } else {
            wrapper.in(column, warehouseIds);
        }
        return wrapper;
    }
}
