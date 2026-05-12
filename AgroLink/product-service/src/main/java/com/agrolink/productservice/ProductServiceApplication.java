package com.agrolink.productservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AgroLink 产品服务启动类
 * <p>
 * 负责产品管理、收藏、评价、文件上传等功能。
 * 启动时自动扫描 com.agrolink 包下的所有组件，并启用定时任务和 RabbitMQ 消息处理。
 */
@SpringBootApplication(scanBasePackages = {"com.agrolink"})
@EnableScheduling       // 启用 Spring 定时任务（用于浏览量定时同步到数据库）
@EnableRabbit           // 启用 RabbitMQ 消息监听（用于处理订单取消后的库存恢复）
public class ProductServiceApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
