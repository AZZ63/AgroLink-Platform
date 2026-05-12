package com.agrolink.productservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 * <p>
 * 对应数据库中的 favorites 表，记录用户对产品的收藏关系。
 */
@Data
@TableName("favorites")
public class Favorite {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏者用户 ID */
    private Long userId;

    /** 被收藏的产品 ID */
    private Long productId;

    /** 收藏时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
