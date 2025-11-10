package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
