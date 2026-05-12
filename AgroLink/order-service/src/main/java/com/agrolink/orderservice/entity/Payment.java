package com.agrolink.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 映射数据库表 payments，存储每笔订单的支付信息
 */
@Data
@TableName("payments")
public class Payment {
    /** 支付记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的订单ID */
    private Long orderId;
    /** 交易号 */
    private String tradeNo;
    /** 支付金额 */
    private BigDecimal amount;
    /** 支付方式 */
    private String method;
    /** 支付状态：PENDING（处理中）/ SUCCESS（成功）/ FAILED（失败） */
    private String status;
    /** 支付时间 */
    private LocalDateTime payTime;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
