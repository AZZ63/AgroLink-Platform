package com.agrolink.orderservice.mq;

import com.agrolink.orderservice.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 订单事件发布者
 * 通过 RabbitMQ 发布订单相关事件消息（延迟取消、取消通知、支付通知）
 */
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟取消消息
     * 订单创建后发送到延迟队列，30分钟 TTL 到期后若未支付则自动取消
     * @param orderId 订单ID
     */
    public void sendDelayedCancel(Long orderId) {
        Map<String, Object> msg = Map.of("orderId", orderId);
        rabbitTemplate.convertAndSend(
            RabbitConfig.ORDER_EXCHANGE,
            RabbitConfig.RK_ORDER_CREATED,
            msg,
            correlationData()
        );
    }

    /**
     * 发布订单取消事件
     * 通知库存服务恢复库存，通知服务推送消息
     * @param orderId   订单ID
     * @param productId 产品ID
     */
    public void publishOrderCancelled(Long orderId, Long productId) {
        Map<String, Object> msg = Map.of(
            "orderId", orderId,
            "productId", productId,
            "action", "CANCELLED"
        );
        rabbitTemplate.convertAndSend(
            RabbitConfig.ORDER_EXCHANGE,
            RabbitConfig.RK_ORDER_CANCELLED,
            msg,
            correlationData()
        );
    }

    /**
     * 发布订单支付事件
     * @param orderId   订单ID
     * @param productId 产品ID
     */
    public void publishOrderPaid(Long orderId, Long productId) {
        Map<String, Object> msg = Map.of(
            "orderId", orderId,
            "productId", productId,
            "action", "PAID"
        );
        rabbitTemplate.convertAndSend(
            RabbitConfig.ORDER_EXCHANGE,
            RabbitConfig.RK_ORDER_PAID,
            msg,
            correlationData()
        );
    }

    /**
     * 生成消息关联数据（用于消息确认）
     * @return CorrelationData 对象，包含唯一 ID
     */
    private org.springframework.amqp.rabbit.connection.CorrelationData correlationData() {
        return new org.springframework.amqp.rabbit.connection.CorrelationData(UUID.randomUUID().toString());
    }
}
