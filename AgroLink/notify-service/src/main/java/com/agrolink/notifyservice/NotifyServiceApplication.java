package com.agrolink.notifyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AgroLink 通知服务 —— 启动入口
 *
 * <p>负责通知的发送、查询、已读管理等核心功能，
 * 扫描本模块以及公共模块（common）下的所有 Spring Bean。</p>
 */
@SpringBootApplication(scanBasePackages = {"com.agrolink.notifyservice", "com.agrolink.common"})
public class NotifyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyServiceApplication.class, args);
    }
}
