package com.agrolink.gateway.config;

import com.agrolink.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局网关异常处理器。
 * <p>
 * 实现 ErrorWebExceptionHandler 接口，捕获网关层所有未处理的异常，
 * 将其转换为统一的 JSON 格式错误响应返回给客户端，避免返回原始的
 * Whitelabel Error Page 或堆栈信息。
 * </p>
 */
@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    /** 用于将错误信息序列化为 JSON */
    private final ObjectMapper objectMapper;

    /**
     * 处理网关层抛出的异常，构造统一的错误响应。
     *
     * @param exchange 当前请求/响应的上下文
     * @param ex       捕获到的异常对象
     * @return Mono<Void> 异步结果
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已经提交，则直接传播异常，避免重复写入
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应内容类型为 JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 默认返回 500 内部服务器错误
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "服务器内部错误";

        // 如果是 HTTP 状态码异常（如 404、405），则提取其状态码和原因
        if (ex instanceof ResponseStatusException statusException) {
            status = HttpStatus.resolve(statusException.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            message = statusException.getReason();
        }

        // 如果错误消息为空，则使用 HTTP 状态码的默认短语
        if (message == null || message.isBlank()) {
            message = status.getReasonPhrase();
        }

        log.error("网关异常: {} {}", status.value(), message, ex);

        // 构造统一的 API 响应格式
        Result<Void> result = new Result<>(status.value(), message, null);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            response.setStatusCode(status);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写出错误响应时发生异常", e);
            return Mono.error(ex);
        }
    }
}
