package com.agrolink.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO。
 * <p>
 * 封装用户登录时提交的用户名和密码，
 * 使用 Jakarta Validation 注解进行非空校验。
 * </p>
 */
@Data
public class LoginRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
