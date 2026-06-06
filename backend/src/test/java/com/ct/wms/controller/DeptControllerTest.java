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
public class DeptControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void testListDeptsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/depts/tree"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeptTreeAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/depts/tree")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListAllDeptsAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/depts/all")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testGetDeptByIdRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/depts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDeptByIdSuccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/depts/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateDeptRequiresAdminRole() throws Exception {
        if (employeeToken == null) return;
        String body = "{\"deptName\":\"测试部门\",\"deptCode\":\"TEST\",\"parentId\":0}";
        mockMvc.perform(post("/api/depts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateDeptValidatesRequiredFields() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/depts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteDeptRequiresAdminRole() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(delete("/api/depts/999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
}
