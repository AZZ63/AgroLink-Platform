package com.agrolink.orderservice.service;

import com.agrolink.common.result.Result;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.orderservice.dto.CreateOrderRequest;
import com.agrolink.orderservice.dto.OrderDTO;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.mq.OrderEventPublisher;
import com.agrolink.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单服务
 * 处理订单的创建、状态流转（含状态机校验）、查询等核心业务逻辑
 * 通过 RestTemplate 调用产品服务和用户服务获取数据
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OrderEventPublisher eventPublisher;

    /** 产品服务基础 URL */
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/product";
    /** 用户服务基础 URL */
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/user";

    /**
     * 订单状态有限状态机（状态流转规则）
     * UNPAID -> PAID / CANCELLED
     * PAID -> CONFIRMED / REFUNDING
     * CONFIRMED -> COMPLETED / REFUNDING
     * COMPLETED / CANCELLED / REFUNDED -> 终态
     * REFUNDING -> REFUNDED
     */
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "UNPAID", Set.of("PAID", "CANCELLED"),
        "PAID", Set.of("CONFIRMED", "REFUNDING"),
        "CONFIRMED", Set.of("COMPLETED", "REFUNDING"),
        "COMPLETED", Set.of(),
        "CANCELLED", Set.of(),
        "REFUNDING", Set.of("REFUNDED"),
        "REFUNDED", Set.of()
    );

    /**
     * 创建订单
     * 校验角色、产品信息、业务规则后保存订单
     * 发送延迟取消消息（30分钟 TTL，超时未支付自动取消）
     * @param request 创建订单请求
     * @param buyerId 买家ID
     * @param role    用户角色（仅限 MERCHANT）
     * @return 订单 DTO
     */
    public OrderDTO createOrder(CreateOrderRequest request, Long buyerId, String role) {
        // 仅允许商户角色下单
        if (!"MERCHANT".equals(role)) {
            throw new BusinessException(403, "仅商户可以下单");
        }

        // 从产品服务获取产品信息
        String productJson = restTemplate.getForObject(
            PRODUCT_SERVICE_URL + "/" + request.getProductId(), String.class);
        if (productJson == null) {
            throw new BusinessException(404, "产品不存在");
        }

        Map<String, Object> productData;
        try {
            Result<Map<String, Object>> result = objectMapper.readValue(productJson,
                new TypeReference<Result<Map<String, Object>>>() {});
            if (!result.isSuccess() || result.getData() == null) {
                throw new BusinessException(404, "产品不存在");
            }
            productData = result.getData();
        } catch (Exception e) {
            throw new BusinessException(400, "获取产品信息失败");
        }

        String infoType = (String) productData.get("infoType");
        Long sellerId = Long.valueOf(productData.get("userId").toString());
        String productName = (String) productData.get("name");
        BigDecimal price = new BigDecimal(productData.get("price").toString());
        String status = (String) productData.get("status");

        // 业务规则校验：只能对供应信息下单、产品必须是上架状态、不能买自己的产品
        if (!"SUPPLY".equals(infoType)) {
            throw new BusinessException(400, "只能对供应信息下单");
        }
        if (!"LISTED".equals(status)) {
            throw new BusinessException(400, "该产品已下架或已售");
        }
        if (sellerId.equals(buyerId)) {
            throw new BusinessException(400, "不能对自己的产品下单");
        }

        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        // 创建订单实体
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setProductName(productName);
        order.setPrice(price);
        order.setInfoType(infoType);
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(totalPrice);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setStatus("UNPAID");
        order.setPayStatus("UNPAID");
        orderRepository.insert(order);

        // 发送延迟取消消息（通过 RabbitMQ DLX 实现 30 分钟 TTL）
        eventPublisher.sendDelayedCancel(order.getId());

        // 通知卖家有新订单
        sendNotification(sellerId, "ORDER_CREATED", "新订单",
            "您有一笔新订单：" + productName + " x" + request.getQuantity(), order.getId());

        return toDTO(order, null, null);
    }

    /**
     * 更新订单状态
     * 按照状态机规则校验状态变更合法性，并进行权限校验
     * @param orderId   订单ID
     * @param userId    操作用户ID
     * @param newStatus 目标状态
     * @param role      用户角色
     */
    public void updateStatus(Long orderId, Long userId, String newStatus, String role) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 校验状态流转是否合法
        String currentStatus = order.getStatus();
        Set<String> allowedNext = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(newStatus)) {
            throw new BusinessException(400,
                "不允许从 " + statusLabel(currentStatus) + " 变更为 " + statusLabel(newStatus));
        }

        // 权限校验
        boolean isSeller = order.getSellerId().equals(userId);
        boolean isBuyer = order.getBuyerId().equals(userId);

        switch (newStatus) {
            case "CONFIRMED":
                if (!isSeller) throw new BusinessException(403, "仅卖家可以确认订单");
                order.setPayStatus("PAID");
                break;
            case "COMPLETED":
                if (!isSeller && !isBuyer) throw new BusinessException(403, "无权操作此订单");
                break;
            case "CANCELLED":
                if (!isBuyer) throw new BusinessException(403, "仅买家可以取消订单");
                break;
            case "REFUNDED":
                order.setPayStatus("REFUNDED");
                order.setRefundTime(java.time.LocalDateTime.now());
                break;
        }

        // PAID 和 REFUNDING 状态由 PaymentService 处理
        if ("PAID".equals(newStatus) || "REFUNDING".equals(newStatus)) {
            // 这些状态变更走支付服务
        }

        order.setStatus(newStatus);
        orderRepository.updateById(order);

        // 通知推送
        String productName = order.getProductName();
        if ("CONFIRMED".equals(newStatus)) {
            sendNotification(order.getBuyerId(), "ORDER_CONFIRMED", "订单已确认",
                "您的订单 " + productName + " 已被卖家确认", orderId);
        } else if ("COMPLETED".equals(newStatus)) {
            sendNotification(order.getBuyerId(), "ORDER_COMPLETED", "交易已完成",
                "您的订单 " + productName + " 已完成交易", orderId);
            sendNotification(order.getSellerId(), "ORDER_COMPLETED", "交易已完成",
                "订单 " + productName + " 已完成交易", orderId);
        } else if ("CANCELLED".equals(newStatus)) {
            eventPublisher.publishOrderCancelled(orderId, order.getProductId());
        }
    }

    /**
     * 获取用户相关的所有订单（买+卖）
     * @param userId 用户ID
     * @return 订单列表
     */
    public List<OrderDTO> getMyOrders(Long userId) {
        List<Order> asBuyer = orderRepository.findByBuyerId(userId);
        List<Order> asSeller = orderRepository.findBySellerId(userId);
        asBuyer.addAll(asSeller);
        return asBuyer.stream()
            .map(o -> toDTO(o, null, null))
            .collect(Collectors.toList());
    }

    /**
     * 获取买家的采购订单
     * @param userId 买家ID
     * @return 订单列表
     */
    public List<OrderDTO> getBuyerOrders(Long userId) {
        return orderRepository.findByBuyerId(userId).stream()
            .map(o -> toDTO(o, null, null))
            .collect(Collectors.toList());
    }

    /**
     * 获取订单详情，校验用户是否为订单的买家或卖家
     * @param orderId 订单ID
     * @param userId  当前用户ID
     * @return 订单 DTO
     */
    public OrderDTO getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看此订单");
        }
        return toDTO(order, null, null);
    }

    /**
     * 获取卖家的销售订单
     * @param userId 卖家ID
     * @return 订单列表
     */
    public List<OrderDTO> getSellerOrders(Long userId) {
        return orderRepository.findBySellerId(userId).stream()
            .map(o -> toDTO(o, null, null))
            .collect(Collectors.toList());
    }

    /**
     * 将 Order 实体转换为 OrderDTO
     * @param o          订单实体
     * @param buyerName  买家名称（预留，暂未使用）
     * @param sellerName 卖家名称（预留，暂未使用）
     * @return 订单 DTO
     */
    private OrderDTO toDTO(Order o, String buyerName, String sellerName) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId());
        dto.setProductId(o.getProductId());
        dto.setProductName(o.getProductName());
        dto.setPrice(o.getPrice());
        dto.setInfoType(o.getInfoType());
        dto.setQuantity(o.getQuantity());
        dto.setTotalPrice(o.getTotalPrice());
        dto.setBuyerId(o.getBuyerId());
        dto.setBuyerName(buyerName);
        dto.setSellerId(o.getSellerId());
        dto.setSellerName(sellerName);
        dto.setStatus(o.getStatus());
        dto.setPayStatus(o.getPayStatus());
        dto.setPayAmount(o.getPayAmount());
        dto.setPayTime(o.getPayTime());
        dto.setRefundTime(o.getRefundTime());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        return dto;
    }

    /**
     * 调用通知服务发送通知（非关键路径，失败不影响主流程）
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

    /**
     * 将状态码转换为中文标签
     */
    private String statusLabel(String status) {
        return switch (status) {
            case "UNPAID" -> "待支付";
            case "PAID" -> "已付款";
            case "CONFIRMED" -> "已确认";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "REFUNDING" -> "退款中";
            case "REFUNDED" -> "已退款";
            default -> status;
        };
    }
}
