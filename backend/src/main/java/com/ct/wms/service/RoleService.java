package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.RoleDTO;
import com.ct.wms.entity.Role;

import java.util.List;

/**
 * 角色Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    Page<Role> listRoles(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 查询所有角色（不分页）
     *
     * @return 角色列表
     */
    List<Role> listAllRoles();

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    Role getRoleById(Long id);

    /**
     * 创建角色
     *
     * @param dto 角色DTO
     * @return 角色ID
     */
    Long createRole(RoleDTO dto);

    /**
     * 更新角色
     *
     * @param id  角色ID
     * @param dto 角色DTO
     */
    void updateRole(Long id, RoleDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode  角色编码
     * @param excludeId 排除的角色ID（更新时使用）
     * @return true-存在 false-不存在
     */
    boolean checkRoleCodeExists(String roleCode, Long excludeId);
}
