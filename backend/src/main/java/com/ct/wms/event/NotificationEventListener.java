package com.ct.wms.event;

import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.mq.NotificationProducer;
import com.ct.wms.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 业务通知事件监听器
 * <p>
 * 在业务事务提交后发送通知：优先走 RabbitMQ 异步链路（站内信 + 微信模板消息），
 * RabbitMQ 未启用时降级为直接写站内信（不发微信）。
 * 通知发送失败仅记录日志，不影响已提交的业务结果。
 *
 * @author CT Development Team
 * @since 2026-08-04
 */
@Slf4j
@Component
public class NotificationEventListener {

    // 可选依赖：如果 RabbitMQ 未启用，此字段为 null
    @Autowired(required = false)
    private NotificationProducer notificationProducer;

    @Autowired
    private MessageService messageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotification(NotificationEvent event) {
        NotificationMessageDTO message = event.getMessage();
        if (message == null || message.getReceiverId() == null) {
            return;
        }

        try {
            if (notificationProducer != null) {
                notificationProducer.sendNotification(message);
            } else {
                // 降级模式：直接落库站内信
                messageService.sendMessage(
                        message.getReceiverId(),
                        message.getMessageType(),
                        message.getTitle(),
                        message.getContent(),
                        message.getRelatedId(),
                        message.getRelatedType()
                );
            }
            log.info("业务通知已发送: receiverId={}, title={}", message.getReceiverId(), message.getTitle());
        } catch (Exception e) {
            log.error("发送业务通知失败: receiverId={}, title={}", message.getReceiverId(), message.getTitle(), e);
        }
    }
}
