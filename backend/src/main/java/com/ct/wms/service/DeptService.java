package com.ct.wms.service;

import com.ct.wms.dto.DeptDTO;
import com.ct.wms.entity.Dept;

import java.util.List;

/**
 * 部门Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface DeptService {

    /**
     * 查询部门树
     *
     * @return 部门树
     */
    List<Dept> listDeptTree();

    /**
     * 查询所有部门（平铺列表）
     *
     * @return 部门列表
     */
    List<Dept> listAllDepts();

    /**
     * 根据ID获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    Dept getDeptById(Long id);

    /**
     * 创建部门
     *
     * @param dto 部门DTO
     * @return 部门ID
     */
    Long createDept(DeptDTO dto);

    /**
     * 更新部门
     *
     * @param id  部门ID
     * @param dto 部门DTO
     */
    void updateDept(Long id, DeptDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);

    /**
     * 检查部门编码是否存在
     *
     * @param deptCode  部门编码
     * @param excludeId 排除的部门ID（更新时使用）
     * @return true-存在 false-不存在
     */
    boolean checkDeptCodeExists(String deptCode, Long excludeId);
}
