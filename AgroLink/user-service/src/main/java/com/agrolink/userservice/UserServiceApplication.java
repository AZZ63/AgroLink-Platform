package com.agrolink.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AgroLink 用户服务 — 应用程序入口
 * <p>
 * 负责启动用户服务模块，扫描 com.agrolink 包下的所有组件。
 */
@SpringBootApplication(scanBasePackages = {"com.agrolink"})
public class UserServiceApplication {
    /**
     * Spring Boot 应用启动入口
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
