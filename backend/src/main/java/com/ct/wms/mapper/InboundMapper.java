package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Inbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface InboundMapper extends BaseMapper<Inbound> {
}
