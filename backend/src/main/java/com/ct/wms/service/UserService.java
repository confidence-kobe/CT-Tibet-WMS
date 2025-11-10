package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.UserDTO;
import com.ct.wms.entity.User;

/**
 * 用户Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param deptId   部门ID（可选）
     * @param roleId   角色ID（可选）
     * @param status   状态（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    Page<User> listUsers(Integer pageNum, Integer pageSize, Long deptId,
                         Long roleId, Integer status, String keyword);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    User getUserById(Long id);

    /**
     * 创建用户
     *
     * @param dto 用户DTO
     * @return 用户ID
     */
    Long createUser(UserDTO dto);

    /**
     * 更新用户
     *
     * @param id  用户ID
     * @param dto 用户DTO
     */
    void updateUser(Long id, UserDTO dto);

    /**
     * 删除用户（软删除）
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 启用/禁用用户
     *
     * @param id     用户ID
     * @param status 状态: 0-启用 1-禁用
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 重置密码
     *
     * @param id          用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（更新时使用）
     * @return true-存在 false-不存在
     */
    boolean checkUsernameExists(String username, Long excludeId);
}
