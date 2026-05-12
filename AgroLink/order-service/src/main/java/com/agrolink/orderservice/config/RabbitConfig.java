package com.agrolink.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置类
 * 配置订单服务的交换机、队列、绑定关系以及消息转换器
 * 用于处理订单超时取消、状态变更通知等异步事件
 */
@Configuration
public class RabbitConfig {

    // ========== 交换机名称 ==========
    /** 订单主题交换机：用于发布订单相关事件 */
    public static final String ORDER_EXCHANGE = "agrolink.order.topic";
    /** 死信交换机：用于处理超时订单 */
    public static final String DLX_EXCHANGE = "agrolink.order.dlx";

    // ========== 队列名称 ==========
    /** 延迟队列：订单创建后发送消息到此队列，TTL 到期后转入死信队列触发超时取消 */
    public static final String DELAY_QUEUE = "agrolink.order.delay.queue";
    /** 取消队列：死信队列的消费者，处理订单超时取消 */
    public static final String CANCEL_QUEUE = "agrolink.order.cancel.queue";
    /** 通知队列：用于订单取消时的通知推送 */
    public static final String NOTIFY_QUEUE = "agrolink.order.notify.queue";
    /** 库存队列：用于订单取消时通知库存服务恢复库存 */
    public static final String INVENTORY_QUEUE = "agrolink.order.inventory.queue";

    // ========== 路由键 ==========
    /** 订单创建路由键 */
    public static final String RK_ORDER_CREATED = "order.created";
    /** 订单超时路由键（从死信路由） */
    public static final String RK_ORDER_TIMEOUT = "order.timeout";
    /** 订单取消路由键 */
    public static final String RK_ORDER_CANCELLED = "order.cancelled";
    /** 订单支付路由键 */
    public static final String RK_ORDER_PAID = "order.paid";

    // ========== 交换机配置 ==========

    /** 订单主题交换机，持久化 */
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).durable(true).build();
    }

    /** 死信交换机，持久化 */
    @Bean
    public TopicExchange dlxExchange() {
        return ExchangeBuilder.topicExchange(DLX_EXCHANGE).durable(true).build();
    }

    // ========== 队列配置 ==========

    /**
     * 延迟队列
     * 消息 TTL 为 30 分钟，到期后转入死信交换机处理超时取消
     */
    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
            .withArgument("x-message-ttl", 30 * 60 * 1000) // 30 分钟
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)    // 死信交换机
            .withArgument("x-dead-letter-routing-key", RK_ORDER_TIMEOUT) // 死信路由键
            .build();
    }

    /** 取消队列：接收超时订单消息并处理取消 */
    @Bean
    public Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE).build();
    }

    /** 通知队列：接收订单取消通知消息 */
    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(NOTIFY_QUEUE).build();
    }

    /** 库存队列：接收订单取消消息，通知库存服务恢复库存 */
    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_QUEUE).build();
    }

    // ========== 绑定关系配置 ==========

    /** 延迟队列绑定到订单交换机，路由键为 order.created */
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(orderExchange()).with(RK_ORDER_CREATED);
    }

    /** 取消队列绑定到死信交换机，路由键为 order.timeout */
    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue()).to(dlxExchange()).with(RK_ORDER_TIMEOUT);
    }

    /** 通知队列绑定到订单交换机，路由键为 order.cancelled */
    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue()).to(orderExchange()).with(RK_ORDER_CANCELLED);
    }

    /** 库存队列绑定到订单交换机，路由键为 order.cancelled */
    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder.bind(inventoryQueue()).to(orderExchange()).with(RK_ORDER_CANCELLED);
    }

    // ========== 消息转换器 ==========

    /** 使用 Jackson JSON 格式进行消息序列化 */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** 配置 RabbitTemplate，设置消息转换器及发送确认回调 */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // 消息发送确认回调：发送失败时打印错误日志
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack && cause != null) {
                System.err.println("[RabbitMQ] 消息发送失败: " + cause);
            }
        });
        return template;
    }
}
