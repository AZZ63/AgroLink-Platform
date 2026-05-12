package com.agrolink.productservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体类
 * <p>
 * 对应数据库中的 reviews 表，记录订单完成后买卖双方之间的评价。
 * 一条评价关联一个订单、一个产品、评价人和被评价人。
 */
@Data
@TableName("reviews")
public class Review {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的订单 ID */
    private Long orderId;

    /** 关联的产品 ID */
    private Long productId;

    /** 评价人（买家/卖家）用户 ID */
    private Long fromUserId;

    /** 被评价人用户 ID */
    private Long toUserId;

    /** 评分（1-5 分） */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
