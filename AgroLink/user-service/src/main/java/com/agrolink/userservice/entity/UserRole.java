package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色关联实体类
 * <p>
 * 映射数据库表 {@code user_role}，建立用户与角色之间的多对多映射关系。
 */
@Data
@TableName("user_role")
public class UserRole {

    /** 用户 ID */
    private Long userId;

    /** 角色 ID */
    private Long roleId;
}
