package com.ct.wms.mq;

import com.ct.wms.config.RabbitMQConfig;
import com.ct.wms.dto.WechatMessageDTO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 微信消息消费者
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatConsumer {

    /**
     * 消费微信模板消息
     * <p>
     * 注意: 需要配置微信小程序的AppID、AppSecret等信息
     * 这里提供基础框架，实际发送需要对接微信API
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

            // TODO: 调用微信API发送模板消息
            // 1. 获取access_token
            // 2. 调用微信模板消息接口
            // String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            // RestTemplate restTemplate = new RestTemplate();
            // String result = restTemplate.postForObject(url, wechatMessage, String.class);

            log.warn("微信模板消息功能暂未实现，需要配置微信小程序信息");

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("微信模板消息处理完成");

        } catch (Exception e) {
            log.error("处理微信模板消息失败", e);

            try {
                // 拒绝消息，不重新入队（进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("拒绝消息失败", ioException);
            }
        }
    }
}
