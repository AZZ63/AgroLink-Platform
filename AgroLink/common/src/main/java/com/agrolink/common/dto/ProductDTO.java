package com.agrolink.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 农产品 DTO（数据传输对象）。
 * <p>
 * 用于在服务层和控制器层之间传输农产品/农资信息。
 * 既可用于供应信息，也可用于需求信息，通过 {@code infoType} 字段区分。
 * </p>
 */
@Data
public class ProductDTO {

    /** 产品唯一标识 */
    private Long id;

    /** 产品名称 */
    private String name;

    /** 产品分类 / 类型 */
    private String type;

    /** 数量 */
    private Integer quantity;

    /** 价格 */
    private BigDecimal price;

    /** 计量单位（如：斤、吨、箱） */
    private String unit;

    /** 产品描述 */
    private String description;

    /** 所在省份 */
    private String province;

    /** 所在城市 */
    private String city;

    /** 产品图片 URL */
    private String image;

    /** 多图 JSON 数组（用于轮播图） */
    private String images;

    /** 信息类型：SUPPLY（供应）或 DEMAND（需求） */
    private String infoType;

    /**
     * 状态。
     * <ul>
     *   <li>供应（SUPPLY）：LISTED（上架）/ SOLD（已售）/ UNLISTED（下架）</li>
     *   <li>需求（DEMAND）：PENDING（待处理）/ COMPLETED（已完成）/ CLOSED（已关闭）</li>
     * </ul>
     */
    private String status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 发布用户 ID */
    private Long userId;

    /** 发布用户名称 */
    private String username;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
