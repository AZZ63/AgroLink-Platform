package com.agrolink.common.dto;

import lombok.Data;

/**
 * 用户信息 DTO。
 * <p>
 * 封装用户的基本信息，用于在接口响应中返回用户数据（不包含密码等敏感字段）。
 * </p>
 */
@Data
public class UserInfo {

    /** 用户 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 用户角色：FARMER（农户）/ MERCHANT（采购商）/ ADMIN（管理员） */
    private String role;

    /** 手机号 */
    private String phone;
}
