package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统菜单实体类
 * <p>
 * 映射数据库表 sys_menu，构建管理后台的树形菜单结构，
 * 支持多级菜单、路由映射、权限标识绑定。
 */
@Data
@TableName("sys_menu")
public class SysMenu {
    /** 菜单主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单 ID，顶级菜单为 0 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 前端路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 权限标识（如 user:list） */
    private String permissionCode;

    /** 排序号，值越小越靠前 */
    private Integer sortOrder;

    /** 是否可见：1 显示，0 隐藏 */
    private Integer visible;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
