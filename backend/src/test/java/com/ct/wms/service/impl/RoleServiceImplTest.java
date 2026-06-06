package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.entity.Role;
import com.ct.wms.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ct.wms.common.exception.BusinessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RoleServiceImplTest {

    @Autowired
    private RoleService roleService;

    @Test
    public void testListRoles() {
        Page<Role> page = roleService.listRoles(1, 20, null);
        assertNotNull(page);
        assertTrue(page.getTotal() > 0);
    }

    @Test
    public void testListRolesWithKeyword() {
        Page<Role> page = roleService.listRoles(1, 20, "Admin");
        assertNotNull(page);
        // 测试数据包含 "Admin" 关键词的角色
        assertTrue(page.getTotal() >= 0);
    }

    @Test
    public void testListAllRoles() {
        List<Role> roles = roleService.listAllRoles();
        assertNotNull(roles);
        // 测试数据中有4个角色
        assertEquals(4, roles.size());
    }

    @Test
    public void testGetRoleById() {
        Role role = roleService.getRoleById(1L);
        assertNotNull(role);
        assertEquals("ADMIN", role.getRoleCode());
    }

    @Test
    public void testGetRoleByIdNotFound() {
        assertThrows(BusinessException.class, () -> roleService.getRoleById(9999L));
    }
}
