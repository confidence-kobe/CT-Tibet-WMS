package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
