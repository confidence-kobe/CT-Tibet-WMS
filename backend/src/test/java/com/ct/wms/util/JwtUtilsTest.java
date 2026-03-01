package com.ct.wms.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void testGenerateToken() {
        Long userId = 1L;
        String username = "admin";
        
        String token = jwtUtils.generateToken(userId, username);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    public void testGetUsernameFromToken() {
        Long userId = 1L;
        String username = "admin";
        
        String token = jwtUtils.generateToken(userId, username);
        String tokenUsername = jwtUtils.getUsernameFromToken(token);
        
        assertEquals(username, tokenUsername);
    }

    @Test
    public void testGetUserIdFromToken() {
        Long userId = 1L;
        String username = "admin";
        
        String token = jwtUtils.generateToken(userId, username);
        Long tokenUserId = jwtUtils.getUserIdFromToken(token);
        
        assertEquals(userId, tokenUserId);
    }

    @Test
    public void testIsTokenExpired() {
        String token = jwtUtils.generateToken(1L, "testuser");
        
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    public void testValidateToken() {
        Long userId = 1L;
        String username = "admin";
        
        String token = jwtUtils.generateToken(userId, username);
        
        assertTrue(jwtUtils.validateToken(token, username));
        assertFalse(jwtUtils.validateToken(token, "wronguser"));
    }

    @Test
    public void testRefreshToken() {
        String oldToken = jwtUtils.generateToken(1L, "admin");
        
        String newToken = jwtUtils.refreshToken(oldToken);
        
        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);
    }
}
