package com.ct.wms.controller;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.entity.Apply;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.InventoryMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.schedule.ApplyTimeoutTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 业务流程复查中发现问题的回归测试
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("业务流程复查回归测试")
class BusinessFlowReviewTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplyMapper applyMapper;
    @Autowired
    private OutboundMapper outboundMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private ApplyTimeoutTask applyTimeoutTask;

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

    private Long createAndApprove(int quantity) throws Exception {
        String body = "{\"warehouseId\":1,\"applyReason\":\"测试\",\"details\":[{\"materialId\":1,\"quantity\":" + quantity + "}]}";
        String json = mockMvc.perform(post("/api/applies")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Long applyId = Long.valueOf(json.replaceAll(".*\"data\":(\\d+).*", "$1"));
        mockMvc.perform(post("/api/applies/approve")
                        .header("Authorization", "Bearer " + warehouseToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"applyId\":%d,\"approvalResult\":1}", applyId)))
                .andExpect(status().isOk());
        return applyId;
    }

    private BigDecimal locked() {
        return inventoryMapper.selectById(1L).getLockedQuantity();
    }

    @Test
    @DisplayName("员工取消已审批申请：只解锁一次，不影响其他申请的锁定库存")
    void cancelApprovedApplyUnlocksOnce() throws Exception {
        BigDecimal before = locked();
        Long other = createAndApprove(3);   // 另一张已审批、待领取的申请
        Long mine = createAndApprove(2);
        assertThat(locked()).isEqualByComparingTo(before.add(new BigDecimal("5")));

        mockMvc.perform(post("/api/applies/" + mine + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        // 只释放自己的 2 个，另一张单的 3 个仍然锁定
        assertThat(locked()).isEqualByComparingTo(before.add(new BigDecimal("3")));
        Apply apply = applyMapper.selectById(mine);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.CANCELED);
        assertThat(apply.getCancelReason()).isNotBlank();
        assertThat(apply.getCancelTime()).isNotNull();
        assertThat(outboundMapper.selectById(apply.getOutboundId()).getStatus()).isEqualTo(OutboundStatus.CANCELED);
        assertThat(applyMapper.selectById(other).getStatus()).isEqualTo(ApplyStatus.APPROVED);
    }

    @Test
    @DisplayName("员工取消唯一一张已审批申请也能成功（以前会报锁定数量不足）")
    void cancelOnlyApprovedApply() throws Exception {
        BigDecimal before = locked();
        Long mine = createAndApprove(2);
        mockMvc.perform(post("/api/applies/" + mine + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertThat(locked()).isEqualByComparingTo(before);
    }

    @Test
    @DisplayName("快速出库：领用人不是本部门员工时拒绝，库存不变")
    void directOutboundRejectsReceiverFromOtherDept() throws Exception {
        BigDecimal qty = inventoryMapper.selectById(1L).getQuantity();
        String body = "{\"warehouseId\":1,\"outboundType\":1,\"outboundTime\":\"2026-06-06T10:00:00\",\"receiverId\":5,"
                + "\"details\":[{\"materialId\":1,\"quantity\":1}]}";
        mockMvc.perform(post("/api/outbounds/direct")
                        .header("Authorization", "Bearer " + warehouseToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.message").value("领用人不属于该仓库所在部门"));
        assertThat(inventoryMapper.selectById(1L).getQuantity()).isEqualByComparingTo(qty);
    }

    @Test
    @DisplayName("24 小时审批提醒发给仓库的仓管员（待审批时还没有审批人）")
    void reminderGoesToWarehouseManager() {
        Apply apply = new Apply();
        apply.setWarehouseId(1L);
        assertThat(applyTimeoutTask.resolveReminderReceiver(apply)).isEqualTo(3L);
        apply.setWarehouseId(2L);
        assertThat(applyTimeoutTask.resolveReminderReceiver(apply)).isNull();
    }
}
