package com.agrolink.notifyservice.controller;

import com.agrolink.common.result.Result;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.util.JwtUtil;
import com.agrolink.notifyservice.dto.NotificationDTO;
import com.agrolink.notifyservice.service.NotificationService;
import com.agrolink.notifyservice.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 通知模块 —— REST 控制器
 *
 * <p>提供通知列表查询、未读数量统计、标记已读以及内部服务调用的通知发送接口。</p>
 */
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService sseService;

    /**
     * SSE 实时通知流
     *
     * @param userId 用户 ID（从网关透传 header 获取）
     * @param token  JWT token（EventSource 无法设置自定义 header，作为备选）
     * @return SseEmitter
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream(@RequestHeader(value = AuthConstant.USER_ID_HEADER, required = false) Long userId,
                             @RequestParam(value = "token", required = false) String token) {
        if (userId == null && token != null) {
            userId = JwtUtil.getUserId(token);
        }
        if (userId == null) {
            SseEmitter emitter = new SseEmitter(0L);
            try { emitter.send(SseEmitter.event().name("error").data("unauthorized")); } catch (Exception ignored) {}
            return emitter;
        }
        return sseService.connect(userId);
    }

    /**
     * 获取当前用户的所有通知列表（按创建时间倒序）
     *
     * @param userId 用户 ID，从请求头中解析
     * @return 通知 DTO 列表
     */
    @GetMapping("/list")
    public Result<List<NotificationDTO>> list(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(notificationService.getUserNotifications(userId));
    }

    /**
     * 获取当前用户的未读通知数量
     *
     * @param userId 用户 ID
     * @return 包含未读计数的 Map
     */
    @GetMapping("/unread")
    public Result<Map<String, Long>> unread(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    /**
     * 将指定通知标记为已读
     *
     * @param id     通知 ID
     * @param userId 用户 ID（用于校验归属）
     */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id, @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 将当前用户的所有通知标记为已读
     *
     * @param userId 用户 ID
     */
    @PutMapping("/read-all")
    public Result<Void> readAll(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        notificationService.markAllRead(userId);
        return Result.success();
    }

    /**
     * 【内部接口】接收其他微服务发来的通知创建请求
     *
     * <p>仅在服务间调用中使用，不对外暴露。</p>
     *
     * @param body 请求体，包含 userId、type、title、content 及可选的 relatedId
     */
    @PostMapping("/internal/send")
    public Result<Void> sendInternal(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String type = (String) body.get("type");
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Long relatedId = body.get("relatedId") != null ? Long.valueOf(body.get("relatedId").toString()) : null;
        notificationService.create(userId, type, title, content, relatedId);
        return Result.success();
    }

}
