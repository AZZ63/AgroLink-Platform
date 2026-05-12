package com.agrolink.productservice.mq;

import com.agrolink.productservice.config.RabbitConfig;
import com.agrolink.productservice.entity.Product;
import com.agrolink.productservice.repository.ProductRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单取消消息消费者
 * <p>
 * 监听 RabbitMQ 中订单取消事件，当订单被取消时，
 * 将 SUPPLY（供应）类型的产品状态恢复为 LISTED（上架），
 * 从而实现库存恢复的功能。
 */
@Component
@RequiredArgsConstructor
public class OrderCancelConsumer {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String HOT_RANKING_KEY = "agrolink:products:hot:ranking";

    /**
     * 处理订单状态变更消息
     * <p>
     * 从消息队列中消费订单事件：
     * <ul>
     *   <li>订单取消（CANCELLED）：将 SUPPLY 类型产品状态恢复为 LISTED</li>
     *   <li>订单创建（其他 action）：增加对应产品的热度分数（权重 5）</li>
     * </ul>
     * 手动确认消息（basicAck）确保消息处理可靠性。
     *
     * @param message RabbitMQ 消息
     * @param channel 消息通道
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitConfig.INVENTORY_QUEUE)
    public void handleOrderCancelled(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 将消息体反序列化为 Map
            Object payload = new Jackson2JsonMessageConverter().fromMessage(message);
            if (!(payload instanceof Map msg)) {
                // 非预期格式，确认消息并跳过
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 解析产品 ID
            Long productId = Long.valueOf(msg.get("productId").toString());

            // 校验操作类型
            String action = (String) msg.get("action");

            if ("CANCELLED".equals(action)) {
                // 订单取消：恢复供应类型产品状态为上架
                Product product = productRepository.selectById(productId);
                if (product != null && "SUPPLY".equals(product.getInfoType())) {
                    product.setStatus("LISTED");
                    productRepository.updateById(product);
                }
            } else {
                // 订单创建：增加商品热度分数（权重 5）
                redisTemplate.opsForZSet().incrementScore(HOT_RANKING_KEY, String.valueOf(productId), 5);
            }

            // 手动确认消息处理完成
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                // 处理失败，拒绝消息并不重新入队
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignored) {
                // 忽略确认异常
            }
        }
    }
}
