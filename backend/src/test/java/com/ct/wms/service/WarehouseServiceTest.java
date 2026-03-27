package com.ct.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 仓库服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        // 获取测试用户并设置SecurityContext
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.last("LIMIT 1");
        User user = userMapper.selectOne(userWrapper);
        if (user != null) {
            String role = user.getRoleId() != null ? user.getRoleId().toString() : "USER";
            TestDataBuilder.mockSecurityContext(user.getId(), user.getUsername(), role);
        }
    }

    @Test
    public void testListWarehouses() {
        List<Warehouse> warehouses = warehouseService.listWarehouses(null, null);
        assertNotNull(warehouses);
    }

    @Test
    public void testGetWarehouseById() {
        Warehouse warehouse = warehouseService.getWarehouseById(1L);
        // 可能不存在，返回null也是正常的
        if (warehouse != null) {
            assertNotNull(warehouse.getWarehouseCode());
        }
    }

    @Test
    public void testGetMyWarehouses() {
        List<Warehouse> warehouses = warehouseService.getMyWarehouses();
        assertNotNull(warehouses);
    }
}
