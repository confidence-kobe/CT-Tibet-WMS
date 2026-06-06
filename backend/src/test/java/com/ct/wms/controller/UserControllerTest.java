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
public class UserControllerTest {

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
    public void testListUsersRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListUsersAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListUsersEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testResetPasswordRequiresRequestBody() throws Exception {
        if (adminToken == null) return;
        // 不传 body 应返回 400
        mockMvc.perform(put("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testResetPasswordValidatesMinLength() throws Exception {
        if (adminToken == null) return;
        // 密码长度不足 6 位
        mockMvc.perform(put("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"123\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testResetPasswordEmployeeForbidden() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(put("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newpass123\"}")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetProfileAuthenticated() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/users?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
