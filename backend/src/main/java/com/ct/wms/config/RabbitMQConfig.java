package com.ct.wms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 *
 * 说明：此配置类仅在 RabbitMQ 启用时才会加载
 * - ConditionalOnClass: 当 RabbitTemplate 类存在时才加载（确保依赖存在）
 * - ConditionalOnProperty: 当未禁用 RabbitMQ 自动配置时才加载
 *
 * 如果在开发环境禁用了 RabbitMQ（方案A），此配置类不会被加载，避免启动失败
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(
    prefix = "spring.rabbitmq",
    name = "host",
    matchIfMissing = false
)
public class RabbitMQConfig {

    // =============== 消息通知相关 ===============

    /**
     * 消息通知交换机
     */
    public static final String NOTIFICATION_EXCHANGE = "wms.notification.exchange";

    /**
     * 消息通知队列
     */
    public static final String NOTIFICATION_QUEUE = "wms.notification.queue";

    /**
     * 消息通知路由键
     */
    public static final String NOTIFICATION_ROUTING_KEY = "wms.notification.#";

    // =============== 微信通知相关 ===============

    /**
     * 微信通知交换机
     */
    public static final String WECHAT_EXCHANGE = "wms.wechat.exchange";

    /**
     * 微信通知队列
     */
    public static final String WECHAT_QUEUE = "wms.wechat.queue";

    /**
     * 微信通知路由键
     */
    public static final String WECHAT_ROUTING_KEY = "wms.wechat.#";

    // =============== 死信队列相关 ===============

    /**
     * 死信交换机
     */
    public static final String DLX_EXCHANGE = "wms.dlx.exchange";

    /**
     * 死信队列
     */
    public static final String DLX_QUEUE = "wms.dlx.queue";

    /**
     * 死信路由键
     */
    public static final String DLX_ROUTING_KEY = "wms.dlx.#";

    /**
     * 消息转换器（JSON）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // 开启消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("消息发送失败: " + cause);
            }
        });

        // 开启消息返回回调（消息无法路由时触发）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage());
        });

        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        return factory;
    }

    // =============== 消息通知队列配置 ===============

    /**
     * 消息通知交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder
                .topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 消息通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "notification.failed")
                .build();
    }

    /**
     * 消息通知绑定
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // =============== 微信通知队列配置 ===============

    /**
     * 微信通知交换机
     */
    @Bean
    public TopicExchange wechatExchange() {
        return ExchangeBuilder
                .topicExchange(WECHAT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 微信通知队列
     */
    @Bean
    public Queue wechatQueue() {
        return QueueBuilder
                .durable(WECHAT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "wechat.failed")
                .build();
    }

    /**
     * 微信通知绑定
     */
    @Bean
    public Binding wechatBinding(Queue wechatQueue, TopicExchange wechatExchange) {
        return BindingBuilder
                .bind(wechatQueue)
                .to(wechatExchange)
                .with(WECHAT_ROUTING_KEY);
    }

    // =============== 死信队列配置 ===============

    /**
     * 死信交换机
     */
    @Bean
    public TopicExchange dlxExchange() {
        return ExchangeBuilder
                .topicExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder
                .durable(DLX_QUEUE)
                .build();
    }

    /**
     * 死信绑定
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, TopicExchange dlxExchange) {
        return BindingBuilder
                .bind(dlxQueue)
                .to(dlxExchange)
                .with(DLX_ROUTING_KEY);
    }
}
