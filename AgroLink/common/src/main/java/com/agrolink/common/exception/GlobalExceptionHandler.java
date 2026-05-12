package com.agrolink.common.exception;

import com.agrolink.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>
 * 通过 {@link RestControllerAdvice} 统一拦截控制器层抛出的异常，
 * 将其转换为统一的 {@link Result} 格式返回给客户端。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（BusinessException）。
     * <p>
     * 将业务异常中的错误码和错误信息原样返回给前端。
     * </p>
     *
     * @param e 业务异常
     * @return 包含错误码和错误信息的统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return new Result<>(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    /**
     * 处理未知的系统异常。
     * <p>
     * 作为兜底处理，捕获所有未被特定异常处理器处理的异常，
     * 返回通用的"服务器内部错误"提示，并记录完整堆栈日志。
     * </p>
     *
     * @param e 系统异常
     * @return 统一错误响应（状态码 500）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("服务器内部错误");
    }
}
