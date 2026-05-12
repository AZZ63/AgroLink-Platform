package com.agrolink.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用通用配置类
 * 注册 RestTemplate Bean，用于服务间 HTTP 调用（如调用产品服务、用户服务、通知服务）
 */
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
