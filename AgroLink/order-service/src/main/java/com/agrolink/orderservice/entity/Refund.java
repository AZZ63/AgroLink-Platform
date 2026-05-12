package com.agrolink.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * 映射数据库表 refunds，存储退款申请和处理信息
 */
@Data
@TableName("refunds")
public class Refund {
    /** 退款ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的支付记录ID */
    private Long paymentId;
    /** 关联的订单ID */
    private Long orderId;
    /** 退款单号 */
    private String refundNo;
    /** 退款金额 */
    private BigDecimal amount;
    /** 退款原因类型：QUALITY（品质问题）/ QUANTITY（数量不符）/ WRONG（发错货）/ OTHER（其他） */
    private String reasonType;
    /** 退款原因详情 */
    private String reasonDetail;
    /** 图片凭证（JSON 或逗号分隔的 URL 列表） */
    private String images;
    /** 退款状态：PENDING（待审核）/ APPROVED（已通过）/ REJECTED（已拒绝）/ SUCCESS（已退款）/ FAILED（退款失败） */
    private String status;
    /** 拒绝原因（审核拒绝时填写） */
    private String rejectReason;
    /** 申请人ID（买家） */
    private Long appliedBy;
    /** 审核人ID（卖家） */
    private Long reviewedBy;
    /** 申请时间 */
    private LocalDateTime appliedAt;
    /** 审核时间 */
    private LocalDateTime reviewedAt;
    /** 退款成功时间 */
    private LocalDateTime successAt;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
