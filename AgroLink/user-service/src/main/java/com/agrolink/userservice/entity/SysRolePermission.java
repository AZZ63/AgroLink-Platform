package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-权限关联实体类
 * <p>
 * 映射数据库表 sys_role_permission，建立角色与权限的多对多映射。
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {
    /** 角色 ID */
    private Long roleId;

    /** 权限 ID */
    private Long permissionId;
}
