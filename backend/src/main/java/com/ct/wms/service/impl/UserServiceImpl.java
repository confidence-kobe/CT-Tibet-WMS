package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.UserStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.UserDTO;
import com.ct.wms.entity.Dept;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.DeptMapper;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> listUsers(Integer pageNum, Integer pageSize, Long deptId,
                                Long roleId, Integer status, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (deptId != null) {
            wrapper.eq(User::getDeptId, deptId);
        }

        if (roleId != null) {
            wrapper.eq(User::getRoleId, roleId);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);

        // 填充关联数据
        result.getRecords().forEach(this::fillUserInfo);

        return result;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 填充关联数据
        fillUserInfo(user);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO dto) {
        // 检查用户名是否存在
        if (checkUsernameExists(dto.getUsername(), null)) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查部门是否存在
        Dept dept = deptMapper.selectById(dto.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 检查密码
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setDeptId(dto.getDeptId());
        user.setRoleId(dto.getRoleId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatus.ENABLED);
        user.setRemark(dto.getRemark());

        userMapper.insert(user);

        log.info("创建用户: id={}, username={}", user.getId(), user.getUsername());

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserDTO dto) {
        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查用户名是否重复
        if (!user.getUsername().equals(dto.getUsername())) {
            if (checkUsernameExists(dto.getUsername(), id)) {
                throw new BusinessException(400, "用户名已存在");
            }
        }

        // 检查部门是否存在
        Dept dept = deptMapper.selectById(dto.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 更新用户
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setDeptId(dto.getDeptId());
        user.setRoleId(dto.getRoleId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRemark(dto.getRemark());

        // 如果提供了密码，则更新密码
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userMapper.updateById(user);

        log.info("更新用户: id={}, username={}", id, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 软删除
        userMapper.deleteById(id);

        log.info("删除用户: id={}, username={}", id, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新状态
        // 将Integer状态值转换为UserStatus枚举
        if (status != null) {
            for (UserStatus us : UserStatus.values()) {
                if (us.getCode().equals(status)) {
                    user.setStatus(us);
                    break;
                }
            }
        }
        userMapper.updateById(user);

        log.info("更新用户状态: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查新密码
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(400, "新密码不能为空");
        }

        // 重置密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("重置用户密码: id={}, username={}", id, user.getUsername());
    }

    @Override
    public boolean checkUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);

        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }

        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 填充用户关联信息
     */
    private void fillUserInfo(User user) {
        // 填充部门名称
        Dept dept = deptMapper.selectById(user.getDeptId());
        if (dept != null) {
            user.setDeptName(dept.getDeptName());
        }

        // 填充角色名称
        Role role = roleMapper.selectById(user.getRoleId());
        if (role != null) {
            user.setRoleName(role.getRoleName());
        }
    }
}
