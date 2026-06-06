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
public class RoleControllerTest {

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
    public void testListRolesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListRolesRequiresAdminRole() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListRolesAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListAllRolesEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/roles/all")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListAllRolesAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/roles/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateRoleRequiresAdminRole() throws Exception {
        if (warehouseToken == null) return;
        String body = "{\"roleName\":\"TestRole\",\"roleCode\":\"TEST\",\"roleLevel\":5}";
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteRoleRequiresAdminRole() throws Exception {
        if (warehouseToken == null) return;
        mockMvc.perform(delete("/api/roles/999")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRolesPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/roles?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
