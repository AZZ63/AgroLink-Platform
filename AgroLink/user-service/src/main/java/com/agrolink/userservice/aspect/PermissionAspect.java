package com.agrolink.userservice.aspect;

import com.agrolink.common.annotation.RequirePermission;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.userservice.entity.SysPermission;
import com.agrolink.userservice.entity.SysRolePermission;
import com.agrolink.userservice.repository.SysPermissionRepository;
import com.agrolink.userservice.repository.SysRolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限校验 AOP 切面
 * <p>
 * 拦截所有标注了 @RequirePermission 的方法，
 * 从请求头获取用户角色，查询角色拥有的权限列表，
 * 判断是否包含注解指定的权限编码。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final SysPermissionRepository sysPermissionRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;

    @Before("@annotation(com.agrolink.common.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取注解中的权限编码
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission annotation = signature.getMethod().getAnnotation(RequirePermission.class);
        if (annotation == null) return;

        String requiredPermission = annotation.value();

        // 从请求头获取用户角色
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(401, "未认证");
        }
        String role = attrs.getRequest().getHeader(AuthConstant.ROLE_HEADER);
        if (role == null) {
            throw new BusinessException(401, "未认证");
        }

        // ADMIN 放行所有权限
        if ("ADMIN".equals(role)) return;

        // 查询角色拥有的权限编码列表
        List<SysRolePermission> rps = sysRolePermissionRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, getRoleId(role)));
        if (rps.isEmpty()) {
            throw new BusinessException(403, "无操作权限");
        }

        List<Long> permissionIds = rps.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<SysPermission> permissions = sysPermissionRepository.selectBatchIds(permissionIds);
        boolean hasPermission = permissions.stream()
                .anyMatch(p -> p.getPermissionCode().equals(requiredPermission));

        if (!hasPermission) {
            throw new BusinessException(403, "无操作权限");
        }
    }

    private Long getRoleId(String roleCode) {
        if ("ADMIN".equals(roleCode)) return 3L;
        if ("FARMER".equals(roleCode)) return 1L;
        if ("MERCHANT".equals(roleCode)) return 2L;
        return 0L;
    }
}
