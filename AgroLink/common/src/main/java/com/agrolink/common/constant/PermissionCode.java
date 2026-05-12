package com.agrolink.common.constant;

/**
 * 权限编码常量类
 * <p>
 * 定义系统中所有操作权限的编码常量，统一管理避免硬编码。
 */
public interface PermissionCode {

    // ========== 用户管理 ==========
    /** 用户查询 */
    String USER_LIST = "user:list";
    /** 用户修改 */
    String USER_UPDATE = "user:update";
    /** 用户删除 */
    String USER_DELETE = "user:delete";

    // ========== 角色管理 ==========
    /** 角色查询 */
    String ROLE_LIST = "role:list";
    /** 角色新增 */
    String ROLE_CREATE = "role:create";
    /** 角色编辑 */
    String ROLE_UPDATE = "role:update";
    /** 角色删除 */
    String ROLE_DELETE = "role:delete";
    /** 角色分配权限 */
    String ROLE_ASSIGN = "role:assign";

    // ========== 菜单管理 ==========
    /** 菜单查询 */
    String MENU_LIST = "menu:list";
    /** 菜单新增 */
    String MENU_CREATE = "menu:create";
    /** 菜单编辑 */
    String MENU_UPDATE = "menu:update";
    /** 菜单删除 */
    String MENU_DELETE = "menu:delete";

    // ========== 商品管理 ==========
    /** 商品审核 */
    String PRODUCT_AUDIT = "product:audit";
    /** 商品删除 */
    String PRODUCT_DELETE = "product:delete";
}
