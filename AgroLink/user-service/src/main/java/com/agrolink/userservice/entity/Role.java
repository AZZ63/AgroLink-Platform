package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色实体类
 * <p>
 * 映射数据库表 {@code role}，存储系统角色定义（如 FARMER、MERCHANT、ADMIN）。
 */
@Data
@TableName("role")
public class Role {
    /** 角色主键 ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码（如 FARMER、MERCHANT、ADMIN） */
    private String roleCode;

    /** 角色名称（中文描述） */
    private String roleName;
}
