package com.agrolink.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域资源共享（CORS）配置。
 *
 * 配置网关允许的前端来源域名、请求头和方法。基于 WebFlux 响应式
 * 编程模型（CorsWebFilter），适配 Spring Cloud Gateway 的非阻塞架构。
 */
@Configuration
public class CorsConfig {

    /**
     * 注册跨域过滤器 Bean。
     * <p>
     * 允许来自本地开发服务器（5173 为 Vite 默认端口，3000 为 React 默认端口）
     * 的跨域请求，放行所有自定义请求头和 HTTP 方法，并支持携带凭证（Cookie）。
     * </p>
     *
     * @return 跨域 Web 过滤器
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端开发服务器地址
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        // 允许所有自定义请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");
        // 允许跨域请求携带凭证（Cookie、Authorization 头等）
        config.setAllowCredentials(true);

        // 将配置注册到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
