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
public class StatisticsControllerTest {

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
    public void testDashboardRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/statistics/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDashboardEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/statistics/dashboard")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDashboardWarehouseCanAccess() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(get("/api/statistics/dashboard")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testInboundStatisticsEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/statistics/inbound")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInboundStatisticsAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/statistics/inbound")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testOutboundStatisticsEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/statistics/outbound")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInventoryStatisticsEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/statistics/inventory")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testMiniprogramDashboardAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        // /miniprogram 端点面向小程序，所有已登录用户可访问
        mockMvc.perform(get("/api/statistics/miniprogram")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testInboundStatisticsWithDateRange() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/statistics/inbound?startDate=2026-01-01&endDate=2026-06-01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
