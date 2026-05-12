package com.agrolink.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册请求 DTO。
 * <p>
 * 封装用户注册时提交的信息，包含用户名、密码、角色和手机号。
 * 使用 Jakarta Validation 注解进行参数校验。
 * </p>
 */
@Data
public class RegisterRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 角色：FARMER（农户）或 MERCHANT（采购商） */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 手机号，需符合中国大陆手机号格式（1 开头，第二位 3-9，共 11 位） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
