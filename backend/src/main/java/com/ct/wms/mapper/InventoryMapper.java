package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 锁定/解锁库存（使用乐观锁）
     *
     * @param id 库存ID
     * @param version 当前版本号
     * @param lockedQuantity 新的锁定数量
     * @param availableQuantity 新的可用数量
     * @return 更新行数
     */
    int lockOrUnlockInventory(@Param("id") Long id,
                              @Param("version") Integer version,
                              @Param("lockedQuantity") java.math.BigDecimal lockedQuantity,
                              @Param("availableQuantity") java.math.BigDecimal availableQuantity);

    /**
     * 提交库存（将锁定库存转为实际扣减）
     *
     * @param id 库存ID
     * @param version 当前版本号
     * @param quantity 新的总库存
     * @param lockedQuantity 新的锁定数量
     * @param availableQuantity 新的可用数量
     * @return 更新行数
     */
    int commitInventory(@Param("id") Long id,
                        @Param("version") Integer version,
                        @Param("quantity") java.math.BigDecimal quantity,
                        @Param("lockedQuantity") java.math.BigDecimal lockedQuantity,
                        @Param("availableQuantity") java.math.BigDecimal availableQuantity);
}
