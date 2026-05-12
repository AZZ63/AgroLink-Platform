package com.agrolink.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AgroLink 网关服务主启动类。
 *
 * 作为整个微服务架构的流量入口，负责请求路由、负载均衡、
 * 鉴权过滤、跨域处理等网关职责。
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * 网关服务启动入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
