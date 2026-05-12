package com.agrolink.productservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类
 * <p>
 * 对应数据库中的 products 表，表示一条农产品供应或需求信息。
 * infoType 区分"供应"（SUPPLY）和"需求"（DEMAND），
 * status 根据 infoType 不同有各自的状态流转。
 */
@Data
@TableName("products")
public class Product {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 产品名称 */
    private String name;

    /** 产品类型（如：蔬菜、水果、粮食等） */
    private String type;

    /** 数量 */
    private Integer quantity;

    /** 价格 */
    private BigDecimal price;

    /** 单位（如：斤、吨、箱等） */
    private String unit;

    /** 产品描述 */
    private String description;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 产品图片 URL */
    private String image;

    /** 多图 JSON 数组（用于轮播图） */
    private String images;

    /** 浏览次数 */
    private Integer viewCount;

    /** 信息类型：SUPPLY（供应）或 DEMAND（需求） */
    private String infoType;

    /**
     * 产品状态
     * <p>
     * SUPPLY 类型的状态：LISTED（上架）、SOLD（已售）、UNLISTED（下架）
     * DEMAND 类型的状态：PENDING（待匹配）、COMPLETED（已成交）、CLOSED（关闭）
     */
    private String status;

    /** 发布者用户 ID */
    private Long userId;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
