package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 库存流水Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {

    /**
     * 统计指定日期低库存记录数（基于当天最后一次变动后的数量）
     * 即：当天有过变动，且变动后数量低于物资最低库存的(仓库,物资)组合数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM (" +
            "  SELECT il.warehouse_id, il.material_id" +
            "  FROM tb_inventory_log il" +
            "  INNER JOIN (" +
            "    SELECT warehouse_id, material_id, MAX(id) AS max_id" +
            "    FROM tb_inventory_log" +
            "    WHERE DATE(create_time) = #{date}" +
            "    AND deleted = 0" +
            "    <if test='warehouseId != null'>AND warehouse_id = #{warehouseId}</if>" +
            "    GROUP BY warehouse_id, material_id" +
            "  ) latest ON il.id = latest.max_id" +
            "  INNER JOIN tb_material m ON il.material_id = m.id AND m.deleted = 0" +
            "  WHERE il.after_quantity &lt; m.min_stock" +
            "  AND m.min_stock IS NOT NULL" +
            ") t" +
            "</script>")
    int countLowStockOnDate(@Param("date") LocalDate date, @Param("warehouseId") Long warehouseId);
}
