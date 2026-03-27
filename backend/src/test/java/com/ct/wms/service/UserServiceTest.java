package com.ct.wms.service;

import com.ct.wms.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testListUsers() {
        var page = userService.listUsers(1, 10, null, null, null, null);
        assertNotNull(page);
    }

    @Test
    public void testGetUserById() {
        User user = userService.getUserById(1L);
        assertNotNull(user);
    }

    @Test
    public void testCheckUsernameExists() {
        // 测试用户名存在检查
        assertTrue(userService.checkUsernameExists("admin", null));
        assertFalse(userService.checkUsernameExists("notexist123456", null));
    }
}
