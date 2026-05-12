package com.agrolink.notifyservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知数据传输对象（DTO）
 *
 * <p>用于将 {@link com.agrolink.notifyservice.entity.Notification Notification} 实体
 * 转换为前端所需的 JSON 结构，隔离内部实体与对外接口。</p>
 */
@Data
public class NotificationDTO {

    /** 通知唯一标识 */
    private Long id;

    /** 接收通知的用户 ID */
    private Long userId;

    /** 通知类型，如 ORDER_CREATED、ORDER_CONFIRMED 等 */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知正文内容 */
    private String content;

    /** 关联的业务记录 ID（如订单 ID） */
    private Long relatedId;

    /** 是否已读 */
    private Boolean isRead;

    /** 通知创建时间 */
    private LocalDateTime createdAt;
}
