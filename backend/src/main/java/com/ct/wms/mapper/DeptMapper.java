package com.ct.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ct.wms.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
