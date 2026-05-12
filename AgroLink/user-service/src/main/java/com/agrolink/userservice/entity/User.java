package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 映射数据库表 {@code users}，存储用户账号信息。
 */
@Data
@TableName("users")
public class User {
    /** 用户主键 ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（MD5 加盐加密后） */
    private String password;

    /** 用户角色：FARMER（农户）或 MERCHANT（商家） */
    private String role;

    /** 手机号 */
    private String phone;

    /** 用户状态：1 启用，0 禁用（默认 1） */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
