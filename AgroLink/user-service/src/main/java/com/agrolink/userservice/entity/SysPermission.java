package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统权限实体类
 * <p>
 * 映射数据库表 sys_permission，定义系统中的细粒度操作权限，
 * 通过 sys_role_permission 与角色关联。
 */
@Data
@TableName("sys_permission")
public class SysPermission {
    /** 权限主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限名称（如"用户查询"） */
    private String permissionName;

    /** 权限编码（如 user:list），用于代码中标识 */
    private String permissionCode;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
