package com.ct.wms.mq;

import com.ct.wms.config.RabbitMQConfig;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.dto.WechatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 消息通知生产者
 *
 * 说明：此组件仅在 RabbitTemplate bean 存在时才会加载
 * 如果禁用了 RabbitMQ，此生产者不会被注册，避免启动失败
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送站内通知消息
     *
     * @param message 通知消息
     */
    public void sendNotification(NotificationMessageDTO message) {
        try {
            log.info("发送站内通知消息: receiverId={}, type={}, title={}",
                    message.getReceiverId(), message.getMessageType(), message.getTitle());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "wms.notification.send",
                    message
            );

            log.info("站内通知消息发送成功");
        } catch (Exception e) {
            log.error("发送站内通知消息失败", e);
            throw new RuntimeException("发送站内通知消息失败", e);
        }
    }

    /**
     * 发送微信模板消息
     *
     * @param message 微信消息
     */
    public void sendWechatMessage(WechatMessageDTO message) {
        try {
            log.info("发送微信模板消息: openId={}, templateId={}",
                    message.getOpenId(), message.getTemplateId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.WECHAT_EXCHANGE,
                    "wms.wechat.send",
                    message
            );

            log.info("微信模板消息发送成功");
        } catch (Exception e) {
            log.error("发送微信模板消息失败", e);
            throw new RuntimeException("发送微信模板消息失败", e);
        }
    }
}
