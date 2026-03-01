package com.ct.wms.mapper;

import com.ct.wms.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectAll() {
        List<User> users = userMapper.selectList(null);
        assertNotNull(users);
    }

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(1L);
        assertNotNull(user);
    }

    @Test
    public void testSelectByUsername() {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        User user = userMapper.selectOne(wrapper);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }
}
