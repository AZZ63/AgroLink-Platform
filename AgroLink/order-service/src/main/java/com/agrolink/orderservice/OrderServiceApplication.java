package com.agrolink.orderservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AgroLink 订单服务 - 启动类
 * 负责订单的创建、支付、退款等核心业务流程
 */
@SpringBootApplication(scanBasePackages = {"com.agrolink.orderservice", "com.agrolink.common"})
@EnableRabbit // 启用 RabbitMQ 消息队列支持（用于订单超时取消、通知等）
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
