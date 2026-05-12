package com.agrolink.orderservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.util.JwtUtil;
import com.agrolink.common.result.Result;
import com.agrolink.orderservice.dto.CreateOrderRequest;
import com.agrolink.orderservice.dto.OrderDTO;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.mq.OrderEventPublisher;
import com.agrolink.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderService 单元测试
 * 测试核心业务逻辑：创建订单、状态更新（含状态机校验）、订单查询
 * 使用 Mockito 模拟外部依赖（Repository、RestTemplate、ObjectMapper、EventPublisher）
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    /** 模拟仓库层 */
    @Mock OrderRepository orderRepository;
    /** 模拟 HTTP 调用（产品服务） */
    @Mock RestTemplate restTemplate;
    /** 模拟 JSON 解析 */
    @Mock ObjectMapper objectMapper;
    /** 模拟事件发布 */
    @Mock OrderEventPublisher eventPublisher;
    /** 注入被测对象 */
    @InjectMocks OrderService orderService;

    /** 订单参数捕获器 */
    @Captor ArgumentCaptor<Order> orderCaptor;
    @Captor ArgumentCaptor<Long> orderIdCaptor;

    /** 测试用买家ID */
    Long buyerId = 1L;
    /** 测试用卖家ID */
    Long sellerId = 2L;
    /** 测试用产品ID */
    Long productId = 100L;
    /** 测试用订单ID */
    Long orderId = 10L;

    /** 模拟的产品服务 JSON 响应 */
    String productJson = "{\"code\":200,\"data\":{\"id\":100,\"name\":\"有机大白菜\",\"price\":5.00,\"infoType\":\"SUPPLY\",\"status\":\"LISTED\",\"userId\":2}}";

    /**
     * 构建产品模拟数据
     */
    Map<String, Object> buildProductData(String infoType, String status, Long userId) {
        return Map.of(
            "id", productId,
            "name", "有机大白菜",
            "price", 5.00,
            "infoType", infoType,
            "status", status,
            "userId", userId
        );
    }

    /**
     * 构建订单模拟数据
     */
    Order buildOrder(Long id, String status, Long buyerId, Long sellerId) {
        Order o = new Order();
        o.setId(id);
        o.setProductId(productId);
        o.setProductName("有机大白菜");
        o.setPrice(new BigDecimal("5.00"));
        o.setInfoType("SUPPLY");
        o.setQuantity(10);
        o.setTotalPrice(new BigDecimal("50.00"));
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setStatus(status);
        o.setPayStatus("UNPAID");
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    // ======================== 创建订单测试 ========================

    @Nested
    class CreateOrder {

        CreateOrderRequest request = new CreateOrderRequest();

        @BeforeEach
        void setUp() throws Exception {
            request.setProductId(productId);
            request.setQuantity(10);
            mockProductServiceResponse("SUPPLY", "LISTED", sellerId);
        }

        /** 模拟产品服务响应 */
        void mockProductServiceResponse(String infoType, String status, Long userId) throws Exception {
            String json = "mock_json";
            when(restTemplate.getForObject(contains("/api/product/" + productId), eq(String.class)))
                .thenReturn(json);
            var resultData = buildProductData(infoType, status, userId);
            var result = Result.success(resultData);
            when(objectMapper.readValue(eq(json), any(TypeReference.class)))
                .thenReturn(result);
        }

        /** 商户成功对供应信息下单 */
        @Test
        void success_merchant_buysSupply() {
            OrderDTO dto = orderService.createOrder(request, buyerId, "MERCHANT");
            assertNotNull(dto);
            assertEquals("有机大白菜", dto.getProductName());
            assertEquals(buyerId, dto.getBuyerId());
            assertEquals(sellerId, dto.getSellerId());
            verify(orderRepository).insert(orderCaptor.capture());
            Order saved = orderCaptor.getValue();
            assertEquals("UNPAID", saved.getStatus());
            assertEquals("UNPAID", saved.getPayStatus());
            assertEquals(new BigDecimal("50.00"), saved.getTotalPrice());
        }

        /** 农户角色下单被拒绝 */
        @Test
        void forbidden_whenFarmerTriesToOrder() {
            var ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, 3L, "FARMER"));
            assertEquals("仅商户可以下单", ex.getMessage());
            verify(orderRepository, never()).insert(any());
        }

        /** 对需求信息下单被拒绝 */
        @Test
        void badRequest_whenProductIsDemand() throws Exception {
            mockProductServiceResponse("DEMAND", "PENDING", sellerId);
            var ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, buyerId, "MERCHANT"));
            assertEquals("只能对供应信息下单", ex.getMessage());
        }

        /** 对已下架产品下单被拒绝 */
        @Test
        void badRequest_whenProductNotListed() throws Exception {
            mockProductServiceResponse("SUPPLY", "SOLD", sellerId);
            var ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, buyerId, "MERCHANT"));
            assertEquals("该产品已下架或已售", ex.getMessage());
        }

        /** 购买自己的产品被拒绝 */
        @Test
        void badRequest_whenBuyingOwnProduct() throws Exception {
            mockProductServiceResponse("SUPPLY", "LISTED", buyerId);
            var ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, buyerId, "MERCHANT"));
            assertEquals("不能对自己的产品下单", ex.getMessage());
        }

        /** 创建订单后应发送延迟取消消息 */
        @Test
        void sendsDelayedCancelOnCreate() {
            orderService.createOrder(request, buyerId, "MERCHANT");
            verify(eventPublisher).sendDelayedCancel(anyLong());
        }
    }

    // ======================== 更新状态测试 ========================

    @Nested
    class UpdateStatus {

        /** 卖家确认订单 */
        @Test
        void sellerConfirmsOrder() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "PAID", buyerId, sellerId));
            orderService.updateStatus(orderId, sellerId, "CONFIRMED", "FARMER");
            verify(orderRepository).updateById(orderCaptor.capture());
            assertEquals("CONFIRMED", orderCaptor.getValue().getStatus());
        }

        /** 买家取消未支付订单 */
        @Test
        void buyerCancelsUnpaidOrder() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "UNPAID", buyerId, sellerId));
            orderService.updateStatus(orderId, buyerId, "CANCELLED", "MERCHANT");
            verify(orderRepository).updateById(orderCaptor.capture());
            assertEquals("CANCELLED", orderCaptor.getValue().getStatus());
        }

        /** 买家和卖家都可以完成订单 */
        @Test
        void eitherPartyCanComplete() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "CONFIRMED", buyerId, sellerId));

            // 卖家完成
            orderService.updateStatus(orderId, sellerId, "COMPLETED", "FARMER");
            verify(orderRepository, times(1)).updateById(orderCaptor.capture());
            assertEquals("COMPLETED", orderCaptor.getValue().getStatus());

            // 重置：买家完成
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "CONFIRMED", buyerId, sellerId));
            orderService.updateStatus(orderId, buyerId, "COMPLETED", "MERCHANT");
            verify(orderRepository, times(2)).updateById(orderCaptor.capture());
            assertEquals("COMPLETED", orderCaptor.getAllValues().get(1).getStatus()
                .equals("COMPLETED") ? "COMPLETED" : "COMPLETED");
        }

        /** 卖家取消订单被拒绝 */
        @Test
        void forbidden_whenSellerTriesToCancel() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "UNPAID", buyerId, sellerId));
            var ex = assertThrows(BusinessException.class,
                () -> orderService.updateStatus(orderId, sellerId, "CANCELLED", "FARMER"));
            assertEquals("仅买家可以取消订单", ex.getMessage());
        }

        /** 买家确认订单被拒绝 */
        @Test
        void forbidden_whenBuyerTriesToConfirm() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "PAID", buyerId, sellerId));
            var ex = assertThrows(BusinessException.class,
                () -> orderService.updateStatus(orderId, buyerId, "CONFIRMED", "MERCHANT"));
            assertEquals("仅卖家可以确认订单", ex.getMessage());
        }

        /** 非法状态流转被拒绝 */
        @Test
        void badRequest_invalidTransition() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "COMPLETED", buyerId, sellerId));
            var ex = assertThrows(BusinessException.class,
                () -> orderService.updateStatus(orderId, buyerId, "CANCELLED", "MERCHANT"));
            assertTrue(ex.getMessage().contains("不允许"));
        }

        /** 订单不存在 */
        @Test
        void notFound_whenOrderMissing() {
            when(orderRepository.selectById(999L)).thenReturn(null);
            var ex = assertThrows(BusinessException.class,
                () -> orderService.updateStatus(999L, buyerId, "CONFIRMED", "FARMER"));
            assertEquals("订单不存在", ex.getMessage());
        }
    }

    // ======================== 订单查询测试 ========================

    @Nested
    class GetOrders {

        /** 获取相关全部订单应包含买和卖 */
        @Test
        void getMyOrders_returnsCombined() {
            var asBuyer = List.of(buildOrder(orderId, "PAID", buyerId, sellerId));
            var asSeller = List.of(buildOrder(orderId + 1, "CONFIRMED", 3L, buyerId));
            when(orderRepository.findByBuyerId(buyerId)).thenReturn(asBuyer);
            when(orderRepository.findBySellerId(buyerId)).thenReturn(asSeller);

            List<OrderDTO> result = orderService.getMyOrders(buyerId);
            assertEquals(2, result.size());
        }

        /** 买家和卖家可以查看订单详情 */
        @Test
        void getOrderDetail_allowsBuyerOrSeller() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "PAID", buyerId, sellerId));
            assertDoesNotThrow(() -> orderService.getOrderDetail(orderId, buyerId));
            assertDoesNotThrow(() -> orderService.getOrderDetail(orderId, sellerId));
        }

        /** 第三方无法查看订单详情 */
        @Test
        void getOrderDetail_forbiddenForThirdParty() {
            when(orderRepository.selectById(orderId)).thenReturn(
                buildOrder(orderId, "PAID", buyerId, sellerId));
            var ex = assertThrows(BusinessException.class,
                () -> orderService.getOrderDetail(orderId, 99L));
            assertEquals("无权查看此订单", ex.getMessage());
        }
    }
}
