package com.ct.wms.service;

import com.ct.wms.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
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
        assertEquals("admin", user.getUsername());
    }

    @Test
    public void testGetUserByUsername() {
        User user = userService.getUserByUsername("admin");
        assertNotNull(user);
    }

    @Test
    public void testGetUserByPhone() {
        User user = userService.getUserByPhone("13800000000");
        assertNotNull(user);
    }

    @Test
    public void testCheckUsernameExists() {
        assertTrue(userService.checkUsernameExists("admin"));
        assertFalse(userService.checkUsernameExists("notexist123456"));
    }

    @Test
    public void testCheckPhoneExists() {
        assertTrue(userService.checkPhoneExists("13800000000"));
        assertFalse(userService.checkPhoneExists("13999999999"));
    }
}
