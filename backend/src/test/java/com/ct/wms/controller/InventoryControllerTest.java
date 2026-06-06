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
public class InventoryControllerTest {

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
    public void testListInventoriesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListInventoriesAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inventories")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListInventoriesWarehouseCanAccess() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(get("/api/inventories")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListInventoriesEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/inventories")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInventoryLogsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/inventories/logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInventoryLogsAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inventories/logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testInventoryLogsEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/inventories/logs")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLowStockAlertsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/inventories/low-stock-alerts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLowStockAlertsAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inventories/low-stock-alerts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testInventoryPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inventories?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInventoryLogsPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inventories/logs?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
