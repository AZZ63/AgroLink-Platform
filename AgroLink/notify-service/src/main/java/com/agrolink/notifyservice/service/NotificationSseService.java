package com.agrolink.notifyservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 推送服务，维护每个用户的 SseEmitter 连接
 */
@Service
public class NotificationSseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {}
        return emitter;
    }

    public void push(Long userId, String event, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
