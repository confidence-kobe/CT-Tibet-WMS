package com.ct.wms.service.impl;

import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * NotificationService 集成测试
 * 验证通知方法在测试环境（无 RabbitMQ）下不抛出异常
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceImplTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    @Test
    public void testNotifyApplySubmitDoesNotThrow() {
        Apply apply = applyMapper.selectById(1L);
        if (apply == null) return;
        assertDoesNotThrow(() -> notificationService.notifyApplySubmit(apply));
    }

    @Test
    public void testNotifyApplyApprovedDoesNotThrow() {
        Apply apply = applyMapper.selectById(1L);
        if (apply == null) return;
        assertDoesNotThrow(() -> notificationService.notifyApplyApproved(apply));
    }

    @Test
    public void testNotifyApplyRejectedDoesNotThrow() {
        Apply apply = applyMapper.selectById(1L);
        if (apply == null) return;
        assertDoesNotThrow(() -> notificationService.notifyApplyRejected(apply));
    }

    @Test
    public void testNotifyOutboundPendingDoesNotThrow() {
        Outbound outbound = outboundMapper.selectById(1L);
        if (outbound == null) return;
        assertDoesNotThrow(() -> notificationService.notifyOutboundPending(outbound));
    }

    @Test
    public void testNotifyWithNullApplyDoesNotThrow() {
        // 传入 null 不应抛出 NPE，应该优雅降级
        assertDoesNotThrow(() -> notificationService.notifyApplySubmit(null));
    }
}
