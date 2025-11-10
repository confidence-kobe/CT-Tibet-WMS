package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Material;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物资Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
}
