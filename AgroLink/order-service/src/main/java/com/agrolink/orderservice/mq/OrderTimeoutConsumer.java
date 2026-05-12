package com.agrolink.orderservice.mq;

import com.agrolink.orderservice.config.RabbitConfig;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.repository.OrderRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单超时消费者
 * 监听取消队列（死信队列），处理超时未支付的订单自动取消
 */
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    /**
     * 处理超时消息
     * 订单 30 分钟未支付时，自动将订单状态变更为 CANCELLED
     * 并发布订单取消事件通知相关服务
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitConfig.CANCEL_QUEUE)
    public void handleTimeout(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 反序列化消息内容
            Object payload = new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter()
                .fromMessage(message);
            if (!(payload instanceof Map msg)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            Long orderId = Long.valueOf(msg.get("orderId").toString());
            Order order = orderRepository.selectById(orderId);

            // 仅当订单仍处于"未支付"状态时才执行取消
            if (order != null && "UNPAID".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                orderRepository.updateById(order);

                // 发布取消事件，通知库存恢复和推送通知
                eventPublisher.publishOrderCancelled(orderId, order.getProductId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignored) {
            }
        }
    }
}
