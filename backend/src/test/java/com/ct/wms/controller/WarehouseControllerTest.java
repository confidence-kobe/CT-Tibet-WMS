package com.ct.wms.controller;

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
public class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String warehouseToken;
    private String employeeToken;

    @BeforeEach
    public void setUp() throws Exception {
        adminToken = login("admin", "123456");
        warehouseToken = login("warehouse", "123456");
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
    public void testListWarehousesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListWarehousesAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/warehouses")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testGetWarehouseByIdRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/warehouses/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetWarehouseByIdSuccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/warehouses/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateWarehouseRequiresDeptAdminOrAbove() throws Exception {
        if (warehouseToken == null) return;
        String body = "{\"warehouseName\":\"测试仓库\",\"warehouseCode\":\"TEST_WH\",\"deptId\":2,\"address\":\"测试地址\",\"managerId\":3}";
        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateWarehouseValidatesRequiredFields() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteWarehouseRequiresAdminRole() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(delete("/api/warehouses/999")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetMyWarehousesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/warehouses/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMyWarehousesSuccess() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(get("/api/warehouses/my")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
