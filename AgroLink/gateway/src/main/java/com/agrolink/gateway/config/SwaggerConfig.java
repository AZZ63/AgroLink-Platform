package com.agrolink.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Swagger UI 路由——重定向到 user-service 的 Swagger 文档页
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerRedirect() {
        return RouterFunctions.route(
            GET("/swagger-ui.html"),
            req -> ServerResponse.status(302)
                .header("Location", "http://localhost:8081/swagger-ui/index.html")
                .build()
        );
    }
}
