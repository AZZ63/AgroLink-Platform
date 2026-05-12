package com.agrolink.notifyservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 *
 * <p>映射数据库 {@code notifications} 表，记录发送给用户的系统通知，
 * 包括订单状态变更等业务事件的通知。</p>
 */
@Data
@TableName("notifications")
public class Notification {

    /** 通知主键 ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户 ID */
    private Long userId;

    /**
     * 通知类型
     * <ul>
     *   <li>ORDER_CREATED    - 订单创建</li>
     *   <li>ORDER_CONFIRMED  - 订单确认</li>
     *   <li>ORDER_CANCELLED  - 订单取消</li>
     *   <li>ORDER_COMPLETED  - 订单完成</li>
     * </ul>
     */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知正文内容 */
    private String content;

    /** 关联的业务记录 ID（如订单 ID） */
    private Long relatedId;

    /** 是否已读（false=未读，true=已读） */
    private Boolean isRead;

    /** 通知创建时间，由 MyBatis-Plus 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
