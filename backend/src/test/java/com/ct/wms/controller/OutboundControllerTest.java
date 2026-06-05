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
public class OutboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setUp() throws Exception {
        adminToken = login("admin", "123456");
        userToken = login("employee1", "123456");
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
    public void testListOutboundsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/outbounds"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListOutboundsAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/outbounds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListOutboundsEmployeeForbidden() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/outbounds")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListOutboundsPageSizeMaxEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/outbounds?pageSize=500")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetOutboundByIdRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/outbounds/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateDirectOutboundRequiresManagerRole() throws Exception {
        if (userToken == null) return;
        String body = "{\"warehouseId\":1,\"outboundType\":1,\"outboundTime\":\"2026-06-06T10:00:00\",\"details\":[{\"materialId\":1,\"quantity\":1}]}";
        mockMvc.perform(post("/api/outbounds/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
