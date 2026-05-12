package com.agrolink.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据传输对象
 * 用于向前端返回订单的完整信息
 */
@Data
public class OrderDTO {
    /** 订单ID */
    private Long id;
    /** 产品ID */
    private Long productId;
    /** 产品名称 */
    private String productName;
    /** 单价 */
    private BigDecimal price;
    /** 信息类型（SUPPLY 供应 / DEMAND 需求） */
    private String infoType;
    /** 数量 */
    private Integer quantity;
    /** 总价 */
    private BigDecimal totalPrice;
    /** 买家ID */
    private Long buyerId;
    /** 买家名称 */
    private String buyerName;
    /** 卖家ID */
    private Long sellerId;
    /** 卖家名称 */
    private String sellerName;
    /** 订单状态（UNPAID/PAID/CONFIRMED/COMPLETED/CANCELLED/REFUNDING/REFUNDED） */
    private String status;
    /** 支付状态（UNPAID/PAID/REFUNDING/REFUNDED） */
    private String payStatus;
    /** 支付金额 */
    private BigDecimal payAmount;
    /** 支付时间 */
    private LocalDateTime payTime;
    /** 退款时间 */
    private LocalDateTime refundTime;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
