package com.ct.wms.service.impl;

import com.ct.wms.entity.Dept;
import com.ct.wms.service.DeptService;
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
public class DeptServiceImplTest {

    @Autowired
    private DeptService deptService;

    @Test
    public void testListDeptTree() {
        List<Dept> tree = deptService.listDeptTree();
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testListAllDepts() {
        List<Dept> depts = deptService.listAllDepts();
        assertNotNull(depts);
        // 测试数据中有3个部门
        assertFalse(depts.isEmpty());
    }

    @Test
    public void testGetDeptById() {
        Dept dept = deptService.getDeptById(1L);
        assertNotNull(dept);
        assertEquals("Headquarters", dept.getDeptName());
    }

    @Test
    public void testGetDeptByIdNotFound() {
        assertThrows(BusinessException.class, () -> deptService.getDeptById(9999L));
    }
}
