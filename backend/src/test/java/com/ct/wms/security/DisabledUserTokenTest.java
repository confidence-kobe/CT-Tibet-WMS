package com.ct.wms.security;

import com.ct.wms.utils.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OWASP A07（W4 自查）：账号被禁用后，手里的令牌必须立即失效，也不能再续期。
 * <ul>
 *   <li>访问：已有保护——过滤器每次请求调 loadUserByUsername，禁用账号在那里抛异常。本测试锁住这个行为，防止以后被改掉</li>
 *   <li>续期：原先有缺口——刷新接口匿名放行、不走上面的检查，禁用账号仍能换到新令牌（新令牌虽然访问不了，但不该发）</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("禁用账号的令牌立即失效")
class DisabledUserTokenTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private JdbcTemplate jdbc;

    private String bearer() {
        return "Bearer " + jwtUtils.generateToken(4L, "employee1");
    }

    @Test
    @DisplayName("启用状态：令牌可以正常访问")
    void enabledUserCanAccess() throws Exception {
        mockMvc.perform(get("/api/auth/user-info").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("禁用后：原令牌访问被拒")
    void disabledUserTokenRejected() throws Exception {
        String token = bearer();
        jdbc.update("UPDATE tb_user SET status = 1 WHERE id = 4");
        mockMvc.perform(get("/api/auth/user-info").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("禁用后：原令牌不能再续期")
    void disabledUserCannotRefresh() throws Exception {
        String token = bearer();
        jdbc.update("UPDATE tb_user SET status = 1 WHERE id = 4");
        mockMvc.perform(post("/api/auth/refresh-token").header("Authorization", token))
                .andExpect(jsonPath("$.code").value(not(200)));
    }
}
