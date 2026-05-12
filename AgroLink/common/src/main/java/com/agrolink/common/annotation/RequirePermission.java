package com.agrolink.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * <p>
 * 标注在 Controller 方法上，用于声明该接口需要的操作权限。
 * 配合 {@link com.agrolink.userservice.aspect.PermissionAspect} AOP 切面实现自动校验。
 * </p>
 *
 * <pre>{@code
 * @RequirePermission("user:update")
 * @PutMapping("/users/{id}/role")
 * public Result<Void> updateRole(...) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 所需的权限编码，如 "user:update"
     */
    String value();
}
