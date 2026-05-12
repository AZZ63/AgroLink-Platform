package com.agrolink.userservice.config;

import com.agrolink.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Token 黑名单过滤器
 * <p>
 * 拦截所有请求，检查请求头中的 Bearer Token 是否已被加入 Redis 黑名单。
 * 若命中黑名单则直接返回 401，阻止请求继续向下传递。
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class TokenBlacklistFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** Redis 中黑名单的 key 前缀 */
    private static final String BLACKLIST_PREFIX = "agrolink:token:blacklist:";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 只拦截带 Authorization header 的请求
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String blacklistKey = BLACKLIST_PREFIX + token;
            // 检查该 token 是否存在于 Redis 黑名单中
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write(objectMapper.writeValueAsString(new Result<>(401, "Token 已失效，请重新登录", null)));
                return;
            }
        }

        // 未命中黑名单，继续执行过滤器链
        chain.doFilter(request, response);
    }
}
