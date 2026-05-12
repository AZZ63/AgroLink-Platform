package com.agrolink.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 映射数据库表 carts，用户加入购物车的商品项
 */
@Data
@TableName("carts")
public class Cart {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 产品ID */
    private Long productId;

    /** 产品名称（冗余） */
    private String productName;

    /** 单价（冗余） */
    private java.math.BigDecimal price;

    /** 单位（冗余） */
    private String unit;

    /** 产品图片（冗余） */
    private String image;

    /** 信息类型 SUPPLY / DEMAND（冗余） */
    private String infoType;

    /** 数量 */
    private Integer quantity;

    /** 是否选中（用于批量下单） */
    private Boolean checked;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
