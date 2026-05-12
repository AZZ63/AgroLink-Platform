package com.agrolink.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车数据传输对象
 * 返回给前端，包含商品状态信息
 */
@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String unit;
    private String image;
    private String infoType;
    private Integer quantity;
    private Boolean checked;
    /** 商品当前状态（用于前端判断是否可下单） */
    private String productStatus;
    /** 商品库存数量 */
    private Integer productQuantity;
    private LocalDateTime createdAt;
}
