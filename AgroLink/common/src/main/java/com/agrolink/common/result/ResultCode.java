package com.agrolink.common.result;

/**
 * API 响应状态码常量接口。
 * <p>
 * 定义系统中使用的 HTTP 风格状态码，
 * 与 {@link Result} 配合使用，统一接口响应语义。
 * </p>
 */
public interface ResultCode {

    /** 请求成功 */
    Integer SUCCESS = 200;

    /** 服务器内部错误 */
    Integer FAIL = 500;

    /** 未授权，需要登录或令牌无效 */
    Integer UNAUTHORIZED = 401;

    /** 无权限访问，当前角色不支持该操作 */
    Integer FORBIDDEN = 403;
}
