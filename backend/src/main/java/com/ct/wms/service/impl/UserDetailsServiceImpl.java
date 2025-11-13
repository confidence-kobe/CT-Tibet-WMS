package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.common.enums.UserStatus;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            log.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        if (user.getStatus() == UserStatus.DISABLED) {
            log.error("User is disabled: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 查询角色
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            log.error("Role not found for user: {}", username);
            throw new UsernameNotFoundException("用户角色不存在: " + username);
        }

        // 构建UserDetails
        return UserDetailsImpl.build(user, role.getRoleCode());
    }
}
