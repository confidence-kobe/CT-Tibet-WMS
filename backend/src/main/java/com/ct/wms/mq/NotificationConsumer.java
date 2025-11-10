package com.ct.wms.mq;

import com.ct.wms.config.RabbitMQConfig;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消息通知消费者
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final MessageService messageService;

    /**
     * 消费站内通知消息
     *
     * @param notificationMessage 通知消息
     * @param message             MQ消息
     * @param channel             通道
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessageDTO notificationMessage,
                                    Message message,
                                    Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到站内通知消息: receiverId={}, type={}, title={}",
                    notificationMessage.getReceiverId(),
                    notificationMessage.getMessageType(),
                    notificationMessage.getTitle());

            // 保存站内消息
            messageService.sendMessage(
                    notificationMessage.getReceiverId(),
                    notificationMessage.getMessageType(),
                    notificationMessage.getTitle(),
                    notificationMessage.getContent(),
                    notificationMessage.getRelatedId(),
                    notificationMessage.getRelatedType()
            );

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("站内通知消息处理成功");

        } catch (Exception e) {
            log.error("处理站内通知消息失败", e);

            try {
                // 拒绝消息，不重新入队（进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("拒绝消息失败", ioException);
            }
        }
    }
}
