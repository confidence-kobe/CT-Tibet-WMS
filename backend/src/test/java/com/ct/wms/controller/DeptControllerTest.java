package com.ct.wms.controller;

import com.ct.wms.dto.DeptDTO;
import com.ct.wms.service.DeptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 部门Controller测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DeptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeptService deptService;

    /**
     * 测试获取部门列表（需要认证）
     */
    @Test
    public void testListDeptsRequiresAuth() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/depts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }

    /**
     * 测试健康检查
     */
    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/auth/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
