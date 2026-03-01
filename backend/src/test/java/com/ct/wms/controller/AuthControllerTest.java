package com.ct.wms.controller;

import com.ct.wms.dto.LoginRequest;
import com.ct.wms.service.AuthService;
import com.ct.wms.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证Controller测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    /**
     * 测试登录成功
     */
    @Test
    public void testLoginSuccess() throws Exception {
        String requestBody = "{\"username\":\"admin\",\"password\":\"123456\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    /**
     * 测试登录失败 - 用户名错误
     */
    @Test
    public void testLoginFailureWrongUsername() throws Exception {
        String requestBody = "{\"username\":\"wronguser\",\"password\":\"123456\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试登录失败 - 密码错误
     */
    @Test
    public void testLoginFailureWrongPassword() throws Exception {
        String requestBody = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试登录失败 - 用户名为空
     */
    @Test
    public void testLoginFailureEmptyUsername() throws Exception {
        String requestBody = "{\"username\":\"\",\"password\":\"123456\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试登录失败 - 密码为空
     */
    @Test
    public void testLoginFailureEmptyPassword() throws Exception {
        String requestBody = "{\"username\":\"admin\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试健康检查接口
     */
    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(post("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("OK"));
    }
}
