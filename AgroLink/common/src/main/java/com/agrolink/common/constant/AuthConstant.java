package com.agrolink.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 认证相关常量接口。
 * <p>
 * 定义 Gateway 透传给下游服务的请求头名称，
 * 以及无需登录即可访问的白名单路径列表。
 * </p>
 */
public interface AuthConstant {

    /** Gateway 透传给下游服务的请求头名称 —— 用户 ID */
    String USER_ID_HEADER = "X-User-Id";

    /** Gateway 透传给下游服务的请求头名称 —— 用户名 */
    String USERNAME_HEADER = "X-Username";

    /** Gateway 透传给下游服务的请求头名称 —— 用户角色 */
    String ROLE_HEADER = "X-Role";

    /** 白名单路径列表（无需登录即可访问的公开接口） */
    List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/user/login",      // 用户登录
            "/api/user/register",   // 用户注册
            "/api/user/refresh",    // 刷新令牌
            "/api/user/setup",      // 管理员初始化
            "/api/notify/stream"    // SSE 实时通知（通过 token 参数鉴权）
    );
}
