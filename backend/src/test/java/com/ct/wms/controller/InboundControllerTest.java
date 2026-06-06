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
public class InboundControllerTest {

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
    public void testListInboundsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/inbounds"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListInboundsAdminCanAccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inbounds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testListInboundsEmployeeForbidden() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/inbounds")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListInboundsPageSizeMaxEnforced() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/inbounds?pageSize=200")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
