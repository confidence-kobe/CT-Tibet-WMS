package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
}
