package com.agrolink.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用 API 响应结果包装类。
 * <p>
 * 封装所有控制层接口的返回格式，包含状态码（code）、消息（message）和数据（data）。
 * 提供静态工厂方法 {@link #success()}、{@link #success(Object)}、{@link #fail(String)}
 * 用于快速构造常见响应。
 * </p>
 *
 * @param <T> 响应数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码，200 表示成功，500 表示服务器错误，401 表示未授权等 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 返回成功响应，携带数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应（状态码 200）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 返回成功响应，不携带数据。
     *
     * @param <T> 数据类型
     * @return 成功响应（状态码 200，data 为 null）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 返回失败响应。
     *
     * @param message 错误描述
     * @param <T>     数据类型
     * @return 失败响应（状态码 500）
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 判断请求是否成功。
     *
     * @return 如果状态码不为 null 且等于 200 则返回 true
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
