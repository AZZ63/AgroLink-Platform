package com.agrolink.gateway.config;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局认证鉴权过滤器。
 *
 * 作为 Spring Cloud Gateway 的全局过滤器，拦截所有进入网关的请求，
 * 依次完成：白名单放行 -> JWT 令牌校验 -> 用户信息解析 -> 角色权限校验 ->
 * 用户信息透传。确保只有合法请求才能路由到下游微服务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 用于将响应结果序列化为 JSON 格式 */
    private final ObjectMapper objectMapper;

    /**
     * 定义过滤器执行顺序，数值越小优先级越高。
     * 返回 -100 以确保该过滤器在其他过滤器之前执行。
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 核心过滤方法，对每个进入网关的请求执行鉴权逻辑。
     *
     * @param exchange 当前请求/响应的上下文
     * @param chain    过滤器链，用于将请求传递给下一个过滤器
     * @return Mono<Void> 异步结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单路径跳过鉴权（如登录、注册、静态资源等）
        if (AuthConstant.PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        // SSE 通过 token 参数鉴权，无需 Authorization header
        if (path.startsWith("/api/notify/stream")) {
            return chain.filter(exchange);
        }
        // Swagger / API 文档路径放行
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
            || path.startsWith("/user-service/v3/") || path.startsWith("/product-service/v3/")
            || path.startsWith("/order-service/v3/") || path.startsWith("/notify-service/v3/")
            || path.startsWith("/webjars/")) {
            return chain.filter(exchange);
        }

        // 2. 提取 Authorization header，格式必须为 "Bearer <token>"
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未登录");
        }

        // 去除 "Bearer " 前缀，获取原始 JWT 令牌
        String token = authHeader.substring(7);

        // 3. 校验 JWT 签名 & 有效期
        if (!JwtUtil.isTokenValid(token)) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 4. 解析令牌中的用户信息（用户 ID、角色、用户名）
        Long userId;
        String role;
        String username;
        try {
            var claims = JwtUtil.parseToken(token);
            userId = Long.parseLong(claims.getSubject());
            role = claims.get("role", String.class);
            username = claims.get("username", String.class);
        } catch (Exception e) {
            log.error("解析令牌失败，路径: {}", path, e);
            return unauthorized(exchange, "Token 解析失败");
        }

        // 5. 角色权限校验：/api/admin/** 路径下的接口仅允许 ADMIN 角色访问
        if (path.startsWith("/api/admin/") && !"ADMIN".equals(role)) {
            log.warn("权限拒绝：非管理员用户 {} 尝试访问管理端路径: {}", userId, path);
            return forbidden(exchange, "仅管理员可操作");
        }

        // 6. 透传用户信息给下游服务
        //     先清除客户端可能伪造的用户信息头，再设置网关解析后的真实用户信息
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(AuthConstant.USER_ID_HEADER);
                    headers.remove(AuthConstant.USERNAME_HEADER);
                    headers.remove(AuthConstant.ROLE_HEADER);
                    headers.set(AuthConstant.USER_ID_HEADER, String.valueOf(userId));
                    headers.set(AuthConstant.USERNAME_HEADER, username);
                    headers.set(AuthConstant.ROLE_HEADER, role);
                })
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 返回 401 未授权响应。
     *
     * @param exchange 当前请求/响应的上下文
     * @param message  错误提示信息
     * @return Mono<Void> 异步结果
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 返回 403 禁止访问响应。
     *
     * @param exchange 当前请求/响应的上下文
     * @param message  错误提示信息
     * @return Mono<Void> 异步结果
     */
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    /**
     * 构造统一的 JSON 格式错误响应并写回客户端。
     *
     * @param exchange 当前请求/响应的上下文
     * @param status   HTTP 状态码
     * @param message  错误描述信息
     * @return Mono<Void> 异步结果
     */
    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 使用通用 Result 结构封装错误响应
        Result<Void> result = new Result<>(status.value(), message, null);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写出错误响应时发生异常", e);
            return Mono.error(e);
        }
    }
}
