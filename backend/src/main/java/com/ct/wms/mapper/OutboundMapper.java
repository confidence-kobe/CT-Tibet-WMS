package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Outbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface OutboundMapper extends BaseMapper<Outbound> {
}
