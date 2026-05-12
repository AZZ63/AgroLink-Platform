package com.agrolink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO。
 * <p>
 * 登录成功时返回给客户端的信息，
 * 包含访问令牌、刷新令牌以及用户基本信息。
 * </p>
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** 访问令牌（JWT），用于接口鉴权，有效期较短（默认 30 分钟） */
    private String token;

    /** 刷新令牌，用于在访问令牌过期后获取新的令牌，有效期较长（默认 7 天） */
    private String refreshToken;

    /** 用户名 */
    private String username;

    /** 用户角色（FARMER / MERCHANT / ADMIN） */
    private String role;

    /** 用户 ID */
    private Long userId;
}
