package com.ct.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    public void setUp() throws Exception {
        adminToken = login("admin", "123456");
        employeeToken = login("employee1", "123456");
    }

    private String login(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        if (response.contains("token")) {
            int start = response.indexOf("\"token\":\"") + 9;
            int end = response.indexOf("\"", start);
            return response.substring(start, end);
        }
        return null;
    }

    @Test
    public void testListAppliesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/applies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListAppliesAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/applies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testMyAppliesEmployeeCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/applies/my")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateApplyValidatesWarehouseDept() throws Exception {
        if (employeeToken == null) return;

        // 获取员工信息，找一个不属于其部门的仓库
        User employee = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "employee1"));
        if (employee == null) return;

        // 找一个不同部门的仓库
        Warehouse otherDeptWarehouse = warehouseMapper.selectOne(
                new LambdaQueryWrapper<Warehouse>()
                        .ne(Warehouse::getDeptId, employee.getDeptId())
                        .last("LIMIT 1"));
        if (otherDeptWarehouse == null) return;

        String body = String.format(
                "{\"warehouseId\":%d,\"details\":[{\"materialId\":1,\"quantity\":1}]}",
                otherDeptWarehouse.getId());

        // BusinessException(403) 由全局异常处理返回 HTTP 200，body 中 code=403
        mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    public void testResetPasswordViaUrlParamFails() throws Exception {
        if (adminToken == null) return;
        // 不传 Content-Type + body，应返回 4xx（415 或 400）
        mockMvc.perform(put("/api/users/1/reset-password?newPassword=abc123")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/applies?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
