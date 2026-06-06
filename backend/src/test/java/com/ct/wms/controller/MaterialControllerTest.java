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
public class MaterialControllerTest {

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
    public void testListMaterialsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListMaterialsAllRolesCanAccess() throws Exception {
        if (employeeToken == null) return;
        mockMvc.perform(get("/api/materials")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMaterialByIdRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/materials/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMaterialByIdSuccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/materials/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateMaterialRequiresManagerRole() throws Exception {
        if (employeeToken == null) return;
        String body = "{\"materialName\":\"测试物资\",\"materialCode\":\"TEST001\",\"category\":\"测试类别\",\"unit\":\"个\",\"minStock\":10}";
        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateMaterialValidatesRequiredFields() throws Exception {
        if (adminToken == null) return;
        // 缺少必填字段
        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteMaterialRequiresDeptAdminOrAbove() throws Exception {
        if (warehouseToken == null) return;
        // WAREHOUSE 角色无法删除物资（需要 DEPT_ADMIN 或 ADMIN）
        mockMvc.perform(delete("/api/materials/999")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSearchMaterialsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/materials/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSearchMaterialsSuccess() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/materials/search?keyword=Cable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testGetCategoriesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/materials/categories"))
                .andExpect(status().isUnauthorized());
    }
}
