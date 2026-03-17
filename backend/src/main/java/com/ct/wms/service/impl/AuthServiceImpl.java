package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.LoginRequest;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.service.AuthService;
import com.ct.wms.utils.JwtUtils;
import com.ct.wms.utils.RedisUtils;
import com.ct.wms.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final int MAX_LOGIN_FAIL = 5;
    private static final long LOGIN_LOCK_SECONDS = 15 * 60L; // 15分钟

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtUtils jwtUtils;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // 可选注入：测试环境或Redis不可用时为null
    @Autowired(required = false)
    private RedisUtils redisUtils;

    @Override
    public LoginVO login(LoginRequest request) {
        String username = request.getUsername();

        // 检查登录失败次数（防暴力破解）
        if (redisUtils != null) {
            Object failCount = redisUtils.get(LOGIN_FAIL_PREFIX + username);
            if (failCount != null && ((Integer) failCount) >= MAX_LOGIN_FAIL) {
                // 抛出 BadCredentialsException，由 GlobalExceptionHandler 处理返回 HTTP 401
                throw new BadCredentialsException("登录失败次数过多，账号已被锁定15分钟，请稍后重试");
            }
        }

        // 认证用户
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
        } catch (AuthenticationException e) {
            // 记录失败次数，然后重新抛出让 GlobalExceptionHandler 处理（返回 HTTP 401）
            recordLoginFail(username);
            throw e;
        }

        // 设置到Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 查询用户完整信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
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

        // 登录成功：清除失败计数、更新登录时间和IP
        clearLoginFail(username);
        updateLoginInfo(user.getId());

        // 构建响应
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

        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(userInfo)
                .build();
    }

    @Override
    public void logout(String token) {
        // 将token加入Redis黑名单，TTL为token剩余有效期
        if (token != null && !token.isEmpty() && redisUtils != null) {
            long remainingSeconds = jwtUtils.getExpirationInSeconds(token);
            if (remainingSeconds > 0) {
                redisUtils.set(TOKEN_BLACKLIST_PREFIX + token, "true", remainingSeconds);
                log.info("Token已加入黑名单，剩余有效期: {}秒", remainingSeconds);
            }
        }
        // 清除Security上下文
        SecurityContextHolder.clearContext();
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        String username = authentication.getName();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BusinessException(401, "用户角色不存在");
        }

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

        Map<String, Object> result = new HashMap<>();
        result.put("user", userInfo);
        result.put("roles", Arrays.asList(role.getRoleCode()));
        result.put("permissions", Collections.emptyList());

        return result;
    }

    // ==================== 私有方法 ====================

    private void recordLoginFail(String username) {
        if (redisUtils == null) {
            return;
        }
        String key = LOGIN_FAIL_PREFIX + username;
        Object current = redisUtils.get(key);
        if (current == null) {
            redisUtils.set(key, 1, LOGIN_LOCK_SECONDS);
        } else {
            redisUtils.incr(key, 1);
            // 重置TTL
            redisUtils.expire(key, LOGIN_LOCK_SECONDS);
        }
    }

    private void clearLoginFail(String username) {
        if (redisUtils != null) {
            redisUtils.delete(LOGIN_FAIL_PREFIX + username);
        }
    }

    private void updateLoginInfo(Long userId) {
        try {
            String ip = null;
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ip = req.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.getRemoteAddr();
                }
                // 取第一个IP（X-Forwarded-For可能包含多个）
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
            }

            LambdaUpdateWrapper<User> update = new LambdaUpdateWrapper<>();
            update.eq(User::getId, userId)
                  .set(User::getLastLoginTime, LocalDateTime.now())
                  .set(User::getLastLoginIp, ip);
            userMapper.update(null, update);
        } catch (Exception e) {
            log.warn("更新登录信息失败: userId={}", userId, e);
        }
    }
}
