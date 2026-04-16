package com.ct.wms.mq;

import com.ct.wms.config.RabbitMQConfig;
import com.ct.wms.dto.WechatMessageDTO;
import com.ct.wms.service.WechatApiService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 微信消息消费者
 *
 * <p>说明：此组件仅在 RabbitTemplate bean 存在时才会加载。
 * 如果禁用了 RabbitMQ，此消费者不会被注册，避免启动失败。
 *
 * <p>消费策略：
 * <ul>
 *   <li>发送成功 → {@code basicAck}</li>
 *   <li>发送失败(业务返回错误) → {@code basicNack(requeue=false)} 进入死信队列</li>
 *   <li>监听器层异常 → {@code basicNack(requeue=false)} 进入死信队列</li>
 * </ul>
 * 进入 DLQ 的消息由后台任务或人工介入处理，避免无效重试风暴。
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class WechatConsumer {

    private final WechatApiService wechatApiService;

    /**
     * 消费微信模板消息
     *
     * @param wechatMessage 微信消息
     * @param message       MQ消息
     * @param channel       通道
     */
    @RabbitListener(queues = RabbitMQConfig.WECHAT_QUEUE)
    public void handleWechatMessage(WechatMessageDTO wechatMessage,
                                     Message message,
                                     Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到微信模板消息: openId={}, templateId={}",
                    wechatMessage.getOpenId(), wechatMessage.getTemplateId());

            boolean success = wechatApiService.sendSubscribeMessage(wechatMessage);

            if (success) {
                channel.basicAck(deliveryTag, false);
                log.info("微信模板消息处理完成: openId={}", wechatMessage.getOpenId());
            } else {
                // 业务失败(参数缺失 / 微信返回错误 / 未启用) 进入 DLQ，不无限重试
                channel.basicNack(deliveryTag, false, false);
                log.warn("微信模板消息发送失败，已丢入死信队列: openId={}", wechatMessage.getOpenId());
            }
        } catch (Exception e) {
            log.error("处理微信模板消息异常", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("拒绝消息失败", ioException);
            }
        }
    }
}
