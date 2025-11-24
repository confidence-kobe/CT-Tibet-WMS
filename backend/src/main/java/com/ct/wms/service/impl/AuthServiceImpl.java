package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.LoginRequest;
import com.ct.wms.entity.Dept;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.service.AuthService;
import com.ct.wms.utils.JwtUtils;
import com.ct.wms.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginVO login(LoginRequest request) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 设置到Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 查询用户完整信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 查询角色信息
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BusinessException(401, "用户角色不存在");
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 更新最后登录时间和IP
        // TODO: 更新登录信息

        // 构建响应
        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .deptId(user.getDeptId())
                .deptName(user.getDeptName()) // 需要关联查询
                .roleId(user.getRoleId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .avatar(user.getAvatar())
                .build();

        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(userInfo)
                .build();
    }

    @Override
    public void logout() {
        // 清除Security上下文
        SecurityContextHolder.clearContext();
        // TODO: 将token加入黑名单(Redis)
    }

    @Override
    public String refreshToken(String oldToken) {
        if (jwtUtils.isTokenExpired(oldToken)) {
            throw new BusinessException(401, "Token已过期");
        }

        return jwtUtils.refreshToken(oldToken);
    }

    @Override
    public Object getCurrentUserInfo() {
        // 从SecurityContext获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        // 获取用户名
        String username = authentication.getName();

        // 查询用户完整信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 查询角色信息
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BusinessException(401, "用户角色不存在");
        }

        // 构建用户信息
        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .deptId(user.getDeptId())
                .deptName(user.getDeptName())
                .roleId(user.getRoleId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .avatar(user.getAvatar())
                .build();

        // 构建返回结果（Map格式：{user, roles, permissions}）
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("user", userInfo);
        result.put("roles", java.util.Arrays.asList(role.getRoleCode())); // 角色码列表
        result.put("permissions", java.util.Collections.emptyList()); // 权限列表（暂时为空）

        return result;
    }
}
