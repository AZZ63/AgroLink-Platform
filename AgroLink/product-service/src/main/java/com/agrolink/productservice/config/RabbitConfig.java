package com.agrolink.productservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置类
 * <p>
 * 配置订单相关的 Topic 交换机、库存恢复队列及绑定关系，
 * 用于在订单取消时恢复产品库存（将产品状态重新置为"上架"）。
 * 同时配置 Jackson JSON 消息转换器，确保消息以 JSON 格式序列化/反序列化。
 */
@Configuration
public class RabbitConfig {

    /** 订单业务的 Topic 交换机名称 */
    public static final String ORDER_EXCHANGE = "agrolink.order.topic";

    /** 库存恢复队列名称——订单取消后恢复产品库存 */
    public static final String INVENTORY_QUEUE = "agrolink.order.inventory.queue";

    /** 订单取消事件的路由键 */
    public static final String RK_ORDER_CANCELLED = "order.cancelled";

    /**
     * 创建订单 Topic 交换机
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    /**
     * 创建库存恢复队列（持久化队列）
     */
    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_QUEUE).build();
    }

    /**
     * 将库存恢复队列绑定到订单交换机，监听订单取消事件
     */
    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder.bind(inventoryQueue()).to(orderExchange()).with(RK_ORDER_CANCELLED);
    }

    /**
     * 配置 Jackson JSON 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate，使用 JSON 消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
