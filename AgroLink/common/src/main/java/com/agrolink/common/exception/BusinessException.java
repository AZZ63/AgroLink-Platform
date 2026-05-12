package com.agrolink.common.exception;

import lombok.Getter;

/**
 * 业务异常类。
 * <p>
 * 继承 {@link RuntimeException}，用于在业务逻辑层抛出可区分的异常，
 * 配合 {@link GlobalExceptionHandler} 统一返回错误响应。
 * 包含错误码（code）和错误描述（message）。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，默认 500（服务器内部错误） */
    private final Integer code;

    /**
     * 构造业务异常，错误码默认为 500。
     *
     * @param message 错误描述
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常，可指定自定义错误码。
     *
     * @param code    错误码
     * @param message 错误描述
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
