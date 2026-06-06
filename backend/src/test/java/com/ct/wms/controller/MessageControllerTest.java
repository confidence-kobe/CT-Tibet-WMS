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
public class MessageControllerTest {

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
    public void testListMessagesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListMessagesAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/messages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testMyMessagesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/messages/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testMyMessagesEmployeeCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/messages/my")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testUnreadCountRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/messages/unread-count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnreadCountAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/messages/unread-count")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testReadAllMessagesRequiresAuth() throws Exception {
        mockMvc.perform(put("/api/messages/read-all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testReadAllMessagesSuccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(put("/api/messages/read-all")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testMessagesPageSizeLimitEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/messages?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
