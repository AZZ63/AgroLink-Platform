package com.agrolink.common.constant;

/**
 * 用户角色常量接口。
 * <p>
 * 定义系统中支持的用户角色：农户（FARMER）、采购商（MERCHANT）、管理员（ADMIN）。
 * 用于注册、登录验证及接口权限控制。
 * </p>
 */
public interface UserRole {
    /** 农户 */
    String FARMER = "FARMER";
    /** 采购商 / 商家 */
    String MERCHANT = "MERCHANT";
    /** 系统管理员 */
    String ADMIN = "ADMIN";
}
