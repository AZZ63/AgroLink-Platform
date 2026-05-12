package com.agrolink.notifyservice.service;

import com.agrolink.notifyservice.dto.NotificationDTO;
import com.agrolink.notifyservice.entity.Notification;
import com.agrolink.notifyservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知业务逻辑层
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseService sseService;

    public NotificationService(NotificationRepository notificationRepository, NotificationSseService sseService) {
        this.notificationRepository = notificationRepository;
        this.sseService = sseService;
    }

    /**
     * 创建通知并实时推送未读数
     */
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notificationRepository.insert(notification);
        // SSE 推送最新未读数
        long count = getUnreadCount(userId);
        sseService.push(userId, "unread", Map.of("count", count));
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.selectById(id);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.updateById(notification);
        }
    }

    public void markAllRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserId(userId);
        list.forEach(n -> {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.updateById(n);
            }
        });
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setRelatedId(notification.getRelatedId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
