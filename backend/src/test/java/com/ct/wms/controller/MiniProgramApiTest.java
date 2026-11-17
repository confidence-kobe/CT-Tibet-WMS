package com.ct.wms.controller;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.entity.Apply;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.OutboundMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 小程序 / PC 接口契约测试：领用人下拉权限、PC 审批接口、取消原因传参、登录错误提示
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("小程序与 PC 接口契约测试")
class MiniProgramApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplyMapper applyMapper;
    @Autowired
    private OutboundMapper outboundMapper;

    private String warehouseToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        warehouseToken = login("warehouse");
        employeeToken = login("employee1");
    }

    private String login(String username) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"123456\"}", username);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        int start = response.indexOf("\"token\":\"") + 9;
        return response.substring(start, response.indexOf('"', start));
    }

    private Long createApply() throws Exception {
        String body = "{\"warehouseId\":1,\"applyReason\":\"测试\",\"details\":[{\"materialId\":1,\"quantity\":2}]}";
        MvcResult result = mockMvc.perform(post("/api/applies")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        String data = json.replaceAll(".*\"data\":(\\d+).*", "$1");
        return Long.valueOf(data);
    }

    @Test
    @DisplayName("登录密码错误时提示中文信息")
    void badCredentialsMessage() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"warehouse\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("领用人下拉：仓管可用且只返回本部门，普通员工无权访问")
    void userOptionsPermission() throws Exception {
        mockMvc.perform(get("/api/users/options").header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].deptId", everyItem(is(2))));
        mockMvc.perform(get("/api/users/options").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PC 审批接口：POST /api/applies/approve 带审批意见")
    void pcApproveWithRemark() throws Exception {
        Long applyId = createApply();
        String body = String.format("{\"applyId\":%d,\"approvalResult\":2,\"approvalRemark\":\"本月预算不足\"}", applyId);
        mockMvc.perform(post("/api/applies/approve")
                        .header("Authorization", "Bearer " + warehouseToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        Apply apply = applyMapper.selectById(applyId);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.REJECTED);
        assertThat(apply.getRejectReason()).isEqualTo("本月预算不足");

        // 已处理列表能查到
        mockMvc.perform(get("/api/applies").param("processed", "true")
                        .header("Authorization", "Bearer " + warehouseToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + applyId + ")]").exists());
    }

    @Test
    @DisplayName("取消出库原因：PC 用 URL 参数、小程序用 JSON body 都能保存")
    void cancelReasonFromParamOrBody() throws Exception {
        for (String mode : new String[]{"param", "body"}) {
            Long applyId = createApply();
            mockMvc.perform(post("/api/applies/approve")
                            .header("Authorization", "Bearer " + warehouseToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"applyId\":%d,\"approvalResult\":1}", applyId)))
                    .andExpect(status().isOk());
            Long outboundId = applyMapper.selectById(applyId).getOutboundId();

            if ("param".equals(mode)) {
                mockMvc.perform(post("/api/outbounds/" + outboundId + "/cancel").param("reason", "PC取消")
                                .header("Authorization", "Bearer " + warehouseToken))
                        .andExpect(status().isOk());
                assertThat(outboundMapper.selectById(outboundId).getCancelReason()).isEqualTo("PC取消");
            } else {
                mockMvc.perform(post("/api/outbounds/" + outboundId + "/cancel")
                                .header("Authorization", "Bearer " + warehouseToken)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"小程序取消\"}"))
                        .andExpect(status().isOk());
                assertThat(outboundMapper.selectById(outboundId).getCancelReason()).isEqualTo("小程序取消");
            }
        }
    }
}
