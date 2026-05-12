package com.agrolink.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 映射数据库表 orders，存储订单信息
 */
@Data
@TableName("orders")
public class Order {
    /** 订单ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 产品ID */
    private Long productId;

    /** 产品名称 */
    private String productName;

    /** 单价 */
    private BigDecimal price;

    /** 信息类型：SUPPLY（供应）/ DEMAND（需求） */
    private String infoType;

    /** 购买数量 */
    private Integer quantity;

    /** 总价 */
    private BigDecimal totalPrice;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /**
     * 订单状态
     * UNPAID（待支付）/ PAID（已付款）/ CONFIRMED（已确认）
     * / COMPLETED（已完成）/ CANCELLED（已取消）
     * / REFUNDING（退款中）/ REFUNDED（已退款）
     */
    private String status;
    /**
     * 支付状态
     * UNPAID（未支付）/ PAID（已支付）/ REFUNDING（退款中）/ REFUNDED（已退款）
     */
    private String payStatus;
    /** 支付金额 */
    private BigDecimal payAmount;
    /** 支付时间 */
    private LocalDateTime payTime;
    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
