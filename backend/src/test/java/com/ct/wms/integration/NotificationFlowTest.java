package com.ct.wms.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Message;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.MessageMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.schedule.OutboundTimeoutTask;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.service.impl.NotificationServiceImpl;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 站内消息通知流程测试
 * <p>
 * 通知在业务事务提交后才写入。测试方法整体在一个会回滚的事务中运行，
 * 因此用 {@link #deliverAfterCommitCallbacks()} 手动触发"提交后"回调，消息写入后仍随测试回滚。
 * 测试数据：部门2 的仓库1 由 warehouse(3) 管理，部门管理员 dept_admin(2)，员工 employee1(4)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("站内消息通知")
class NotificationFlowTest {

    private static final long WAREHOUSE_MANAGER_ID = 3L;
    private static final long DEPT_ADMIN_ID = 2L;
    private static final long EMPLOYEE_ID = 4L;
    private static final long DEPT2_WAREHOUSE_ID = 1L;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ApplyService applyService;
    @Autowired
    private OutboundService outboundService;
    @Autowired
    private ApplyMapper applyMapper;
    @Autowired
    private OutboundMapper outboundMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private OutboundTimeoutTask outboundTimeoutTask;

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private void loginAs(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    /** 已触发过的回调（事务同步只能整体清除，这里按对象去重，避免重复投递） */
    private final Set<TransactionSynchronization> delivered =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * 触发通知服务注册的"事务提交后"回调（模拟业务事务提交）；
     * 不清除事务同步，以免影响 Spring 自身的连接/会话管理
     */
    private void deliverAfterCommitCallbacks() {
        String notifierClass = NotificationServiceImpl.class.getName();
        TransactionSynchronizationManager.getSynchronizations().stream()
                .filter(s -> s.getClass().getName().startsWith(notifierClass))
                .filter(delivered::add)
                .forEach(TransactionSynchronization::afterCommit);
    }

    private List<Message> messagesOf(long userId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByAsc(Message::getId));
    }

    private Long createApply(long materialId, String quantity) {
        loginAs("employee1");
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(DEPT2_WAREHOUSE_ID);
        dto.setApplyReason("拉萨小区光缆施工");
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(materialId);
        detail.setQuantity(new BigDecimal(quantity));
        dto.setDetails(List.of(detail));
        return applyService.createApply(dto);
    }

    private void approve(Long applyId, int result, String remark) {
        loginAs("warehouse");
        ApprovalDTO dto = new ApprovalDTO();
        dto.setApplyId(applyId);
        dto.setApprovalResult(result);
        dto.setApprovalRemark(remark);
        applyService.approveApply(dto);
    }

    @Test
    @DisplayName("消息在事务提交后才写入（业务回滚不会留下消息）")
    void messagesAreWrittenOnlyAfterCommit() {
        createApply(1L, "2");
        assertThat(messagesOf(WAREHOUSE_MANAGER_ID)).isEmpty();

        deliverAfterCommitCallbacks();
        assertThat(messagesOf(WAREHOUSE_MANAGER_ID)).hasSize(1);
    }

    @Test
    @DisplayName("提交申请：通知仓库管理员和本部门的部门管理员，不通知申请人")
    void applySubmitNotifiesApprovers() {
        Long applyId = createApply(1L, "2");
        deliverAfterCommitCallbacks();

        for (long approver : new long[]{WAREHOUSE_MANAGER_ID, DEPT_ADMIN_ID}) {
            List<Message> messages = messagesOf(approver);
            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getType()).isEqualTo(MessageType.APPLY_SUBMIT);
            assertThat(messages.get(0).getRelatedId()).isEqualTo(applyId);
            assertThat(messages.get(0).getRelatedType()).isEqualTo(3);
            assertThat(messages.get(0).getContent()).contains("Emp Zhang").contains("拉萨小区光缆施工");
        }
        assertThat(messagesOf(EMPLOYEE_ID)).isEmpty();

        // 读取申请时带回申请原因（列表页"申请原因"列）
        assertThat(applyService.getApplyById(applyId).getApplyReason()).isEqualTo("拉萨小区光缆施工");
    }

    @Test
    @DisplayName("审批通过：通知申请人领取仓库和审批意见；领取后再通知")
    void approvalAndPickupNotifyApplicant() {
        Long applyId = createApply(1L, "2");
        approve(applyId, 1, "同意，请尽快领取");
        deliverAfterCommitCallbacks();

        List<Message> messages = messagesOf(EMPLOYEE_ID);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getType()).isEqualTo(MessageType.APPLY_APPROVED);
        assertThat(messages.get(0).getContent()).contains("Network Warehouse").contains("同意，请尽快领取");

        Apply apply = applyMapper.selectById(applyId);
        outboundService.confirmOutbound(apply.getOutboundId());
        deliverAfterCommitCallbacks();

        messages = messagesOf(EMPLOYEE_ID);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(1).getTitle()).isEqualTo("物资已领取");
        assertThat(messages.get(1).getRelatedId()).isEqualTo(applyId);
    }

    @Test
    @DisplayName("审批拒绝：通知申请人拒绝原因")
    void rejectionNotifiesApplicantWithReason() {
        Long applyId = createApply(1L, "2");
        approve(applyId, 2, "本月配额已用完");
        deliverAfterCommitCallbacks();

        List<Message> messages = messagesOf(EMPLOYEE_ID);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getType()).isEqualTo(MessageType.APPLY_REJECTED);
        assertThat(messages.get(0).getContent()).contains("本月配额已用完");
    }

    @Test
    @DisplayName("仓管取消出库单：通知领用人取消原因")
    void cancelOutboundNotifiesReceiver() {
        Long applyId = createApply(1L, "2");
        approve(applyId, 1, null);
        deliverAfterCommitCallbacks();

        Apply apply = applyMapper.selectById(applyId);
        outboundService.cancelOutbound(apply.getOutboundId(), "物资已调拨其他项目");
        deliverAfterCommitCallbacks();

        List<Message> messages = messagesOf(EMPLOYEE_ID);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(1).getTitle()).isEqualTo("出库单已取消");
        assertThat(messages.get(1).getContent()).contains("物资已调拨其他项目");
    }

    @Test
    @DisplayName("出库使库存跌破最低库存时预警一次，继续出库不再重复预警")
    void lowStockAlertOnlyWhenCrossingThreshold() {
        loginAs("warehouse");
        // 物资10：库存20，最低库存10
        outboundService.createOutboundDirect(directOutbound(10L, "15"));
        deliverAfterCommitCallbacks();

        List<Message> managerMessages = messagesOf(WAREHOUSE_MANAGER_ID);
        assertThat(managerMessages).extracting(Message::getType).containsExactly(MessageType.STOCK_ALERT);
        assertThat(managerMessages.get(0).getContent()).contains("Enterprise Router").contains("5").contains("10");
        assertThat(messagesOf(DEPT_ADMIN_ID)).extracting(Message::getType).containsExactly(MessageType.STOCK_ALERT);

        outboundService.createOutboundDirect(directOutbound(10L, "1"));
        deliverAfterCommitCallbacks();
        assertThat(messagesOf(WAREHOUSE_MANAGER_ID)).hasSize(1);
    }

    @Test
    @DisplayName("超过7天未领取的出库单会被自动取消并通知领用人")
    void pickupTimeoutCancelsAndNotifies() {
        Long applyId = createApply(1L, "2");
        approve(applyId, 1, null);
        deliverAfterCommitCallbacks();
        Long outboundId = applyMapper.selectById(applyId).getOutboundId();

        // 待领取的出库单没有出库时间，超时按创建时间计算
        outboundMapper.update(null, new LambdaUpdateWrapper<Outbound>()
                .eq(Outbound::getId, outboundId)
                .set(Outbound::getCreateTime, LocalDateTime.now().minusDays(8)));

        outboundTimeoutTask.cancelTimeoutOutbound();
        deliverAfterCommitCallbacks();

        assertThat(outboundMapper.selectById(outboundId).getStatus()).isEqualTo(OutboundStatus.CANCELED);
        assertThat(messagesOf(EMPLOYEE_ID)).extracting(Message::getTitle).contains("出库单已取消");
    }

    private OutboundDTO directOutbound(long materialId, String quantity) {
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(DEPT2_WAREHOUSE_ID);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(EMPLOYEE_ID);
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(materialId);
        detail.setQuantity(new BigDecimal(quantity));
        dto.setDetails(List.of(detail));
        return dto;
    }
}
