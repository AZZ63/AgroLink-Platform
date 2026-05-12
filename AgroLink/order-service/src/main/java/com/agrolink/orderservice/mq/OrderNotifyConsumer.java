package com.agrolink.orderservice.mq;

import com.agrolink.orderservice.config.RabbitConfig;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.repository.OrderRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 订单通知消费者
 * 监听通知队列，当订单取消时向买家和卖家推送通知消息
 */
@Component
@RequiredArgsConstructor
public class OrderNotifyConsumer {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    /**
     * 处理订单事件消息
     * 目前处理订单取消事件，向相关用户发送通知
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void handleOrderEvent(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 反序列化消息内容
            Object payload = new Jackson2JsonMessageConverter().fromMessage(message);
            if (!(payload instanceof Map msg)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            Long orderId = Long.valueOf(msg.get("orderId").toString());
            String action = (String) msg.get("action");
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 根据事件类型处理通知
            if ("CANCELLED".equals(action)) {
                // 通知卖家和买家订单已取消
                sendNotification(order.getSellerId(), "ORDER_CANCELLED", "订单已取消",
                    "订单 " + order.getProductName() + " 已被取消", orderId);
                sendNotification(order.getBuyerId(), "ORDER_CANCELLED", "订单已取消",
                    "您的订单 " + order.getProductName() + " 已取消", orderId);
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                // 处理失败，拒收消息（不重新入队）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 调用通知服务 REST API 发送通知
     * @param userId    目标用户ID
     * @param type      通知类型
     * @param title     通知标题
     * @param content   通知内容
     * @param relatedId 关联的业务ID
     */
    private void sendNotification(Long userId, String type, String title, String content, Long relatedId) {
        try {
            restTemplate.postForEntity("http://localhost:8084/api/notify/internal/send",
                Map.of("userId", userId, "type", type, "title", title, "content", content, "relatedId", relatedId),
                Void.class);
        } catch (Exception e) {
            // 通知发送失败不影响主流程
        }
    }
}
