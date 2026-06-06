package com.ct.wms.service.impl;

import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.LoginRequest;
import com.ct.wms.service.AuthService;
import com.ct.wms.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceImplTest {

    @Autowired
    private AuthService authService;

    private LoginRequest buildRequest(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    @Test
    public void testLoginSuccess() {
        LoginVO vo = authService.login(buildRequest("admin", "123456"));
        assertNotNull(vo);
        assertNotNull(vo.getToken());
        assertEquals("Bearer", vo.getTokenType());
        assertNotNull(vo.getUser());
        assertEquals("admin", vo.getUser().getUsername());
        assertEquals("ADMIN", vo.getUser().getRoleCode());
    }

    @Test
    public void testLoginSuccessForEmployee() {
        LoginVO vo = authService.login(buildRequest("employee1", "123456"));
        assertNotNull(vo);
        assertEquals("USER", vo.getUser().getRoleCode());
    }

    @Test
    public void testLoginWrongPassword() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildRequest("admin", "wrongpassword")));
    }

    @Test
    public void testLoginNonExistentUser() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildRequest("nobody", "123456")));
    }

    @Test
    public void testLoginEmptyUsername() {
        assertThrows(Exception.class,
                () -> authService.login(buildRequest("", "123456")));
    }

    @Test
    public void testLogoutClearsSecurityContext() {
        LoginVO vo = authService.login(buildRequest("admin", "123456"));
        assertNotNull(vo.getToken());

        // logout 不应抛出异常（Redis 为 null 时静默跳过黑名单写入）
        assertDoesNotThrow(() -> authService.logout(vo.getToken()));

        // SecurityContext 应已清空
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testLogoutWithNullToken() {
        assertDoesNotThrow(() -> authService.logout(null));
    }

    @Test
    public void testRefreshToken() {
        LoginVO vo = authService.login(buildRequest("admin", "123456"));
        // refreshToken 返回一个有效的新 token（同一秒内 iat 相同时值可能相同，属正常）
        String newToken = authService.refreshToken(vo.getToken());
        assertNotNull(newToken);
        assertFalse(newToken.isEmpty());
    }

    @Test
    public void testGetCurrentUserInfoAfterLogin() {
        authService.login(buildRequest("warehouse", "123456"));
        Object info = authService.getCurrentUserInfo();
        assertNotNull(info);
    }
}
