package com.agrolink.orderservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.entity.Payment;
import com.agrolink.orderservice.entity.Refund;
import com.agrolink.orderservice.repository.OrderRepository;
import com.agrolink.orderservice.repository.PaymentRepository;
import com.agrolink.orderservice.repository.RefundRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentService 单元测试
 * 测试支付和退款核心业务流程：
 * - 创建支付（权限、订单状态校验）
 * - 支付回调（状态更新）
 * - 退款申请（原因类型、状态校验）
 * - 退款审核（通过/拒绝）
 * - 查询方法
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    /** 模拟仓库层 */
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock RefundRepository refundRepository;
    @Mock RestTemplate restTemplate;
    /** 注入被测对象 */
    @InjectMocks PaymentService paymentService;

    /** 参数捕获器 */
    @Captor ArgumentCaptor<Payment> paymentCaptor;
    @Captor ArgumentCaptor<Order> orderCaptor;
    @Captor ArgumentCaptor<Refund> refundCaptor;

    /** 测试用买家ID */
    Long buyerId = 1L;
    /** 测试用卖家ID */
    Long sellerId = 2L;
    /** 测试用订单ID */
    Long orderId = 10L;
    /** 测试用支付ID */
    Long paymentId = 20L;
    /** 测试用退款ID */
    Long refundId = 30L;
    /** 测试用交易号 */
    String tradeNo = "MOCK1234567890abc123";

    /** 已支付订单（测试用） */
    Order paidOrder;
    /** 未支付订单（测试用） */
    Order unpaidOrder;

    @BeforeEach
    void setUp() {
        unpaidOrder = new Order();
        unpaidOrder.setId(orderId);
        unpaidOrder.setProductName("有机大白菜");
        unpaidOrder.setTotalPrice(new BigDecimal("50.00"));
        unpaidOrder.setStatus("UNPAID");
        unpaidOrder.setBuyerId(buyerId);
        unpaidOrder.setSellerId(sellerId);

        paidOrder = new Order();
        paidOrder.setId(orderId);
        paidOrder.setProductName("有机大白菜");
        paidOrder.setTotalPrice(new BigDecimal("50.00"));
        paidOrder.setPayAmount(new BigDecimal("50.00"));
        paidOrder.setStatus("PAID");
        paidOrder.setPayStatus("PAID");
        paidOrder.setPayTime(LocalDateTime.now());
        paidOrder.setBuyerId(buyerId);
        paidOrder.setSellerId(sellerId);
    }

    // ======================== 创建支付测试 ========================

    @Nested
    class CreatePayment {

        /** 买家成功创建支付 */
        @Test
        void success() {
            when(orderRepository.selectById(orderId)).thenReturn(unpaidOrder);
            when(paymentRepository.insert(any())).thenReturn(1);

            String result = paymentService.createPayment(orderId, buyerId, "MERCHANT");
            assertNotNull(result);
            assertTrue(result.startsWith("MOCK"));

            verify(paymentRepository).insert(paymentCaptor.capture());
            Payment p = paymentCaptor.getValue();
            assertEquals(orderId, p.getOrderId());
            assertEquals("PENDING", p.getStatus());
            assertEquals(new BigDecimal("50.00"), p.getAmount());
        }

        /** 非买家创建支付被拒绝 */
        @Test
        void forbidden_whenNotBuyer() {
            when(orderRepository.selectById(orderId)).thenReturn(unpaidOrder);
            var ex = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(orderId, sellerId, "FARMER"));
            assertEquals("无权操作", ex.getMessage());
        }

        /** 非未支付状态不允许支付 */
        @Test
        void badRequest_whenOrderNotUnpaid() {
            when(orderRepository.selectById(orderId)).thenReturn(paidOrder);
            var ex = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(orderId, buyerId, "MERCHANT"));
            assertEquals("订单状态不允许支付", ex.getMessage());
        }
    }

    // ======================== 支付成功回调测试 ========================

    @Nested
    class PaySuccess {

        /** 支付成功回调正常更新订单和支付记录 */
        @Test
        void success_callbackUpdatesOrder() {
            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setOrderId(orderId);
            payment.setTradeNo(tradeNo);
            payment.setAmount(new BigDecimal("50.00"));
            payment.setStatus("PENDING");

            when(paymentRepository.findByTradeNo(tradeNo)).thenReturn(Optional.of(payment));
            when(orderRepository.selectById(orderId)).thenReturn(unpaidOrder);

            paymentService.paySuccess(tradeNo);

            verify(paymentRepository).updateById(paymentCaptor.capture());
            assertEquals("SUCCESS", paymentCaptor.getValue().getStatus());

            verify(orderRepository).updateById(orderCaptor.capture());
            assertEquals("PAID", orderCaptor.getValue().getStatus());
            assertEquals("PAID", orderCaptor.getValue().getPayStatus());
        }

        /** 已处理的支付不能重复回调 */
        @Test
        void badRequest_whenAlreadyProcessed() {
            Payment payment = new Payment();
            payment.setStatus("SUCCESS");
            when(paymentRepository.findByTradeNo(tradeNo)).thenReturn(Optional.of(payment));

            var ex = assertThrows(BusinessException.class,
                () -> paymentService.paySuccess(tradeNo));
            assertEquals("支付已处理", ex.getMessage());
        }
    }

    // ======================== 退款申请测试 ========================

    @Nested
    class RequestRefund {

        /** 买家成功发起退款申请 */
        @Test
        void success() {
            when(orderRepository.selectById(orderId)).thenReturn(paidOrder);
            when(paymentRepository.findByOrderId(orderId)).thenReturn(
                Optional.of(new Payment()));

            Refund result = paymentService.requestRefund(orderId, buyerId,
                "QUALITY", "品质有问题", "");

            assertEquals("PENDING", result.getStatus());
            verify(refundRepository).insert(any());
            verify(orderRepository).updateById(orderCaptor.capture());
            assertEquals("REFUNDING", orderCaptor.getValue().getStatus());
            assertEquals("REFUNDING", orderCaptor.getValue().getPayStatus());
        }

        /** 非买家申请退款被拒绝 */
        @Test
        void forbidden_whenNotBuyer() {
            when(orderRepository.selectById(orderId)).thenReturn(paidOrder);
            var ex = assertThrows(BusinessException.class,
                () -> paymentService.requestRefund(orderId, sellerId, "QUALITY", "", ""));
            assertEquals("仅买家可申请退款", ex.getMessage());
        }

        /** 订单状态不允许退款 */
        @Test
        void badRequest_wrongOrderStatus() {
            when(orderRepository.selectById(orderId)).thenReturn(unpaidOrder);
            var ex = assertThrows(BusinessException.class,
                () -> paymentService.requestRefund(orderId, buyerId, "QUALITY", "", ""));
            assertEquals("当前状态不允许退款", ex.getMessage());
        }

        /** 无效的退款原因类型 */
        @Test
        void badRequest_invalidReasonType() {
            when(orderRepository.selectById(orderId)).thenReturn(paidOrder);
            var ex = assertThrows(BusinessException.class,
                () -> paymentService.requestRefund(orderId, buyerId, "INVALID", "", ""));
            assertEquals("无效的退款原因类型", ex.getMessage());
        }
    }

    // ======================== 退款审核测试 ========================

    @Nested
    class ReviewRefund {

        /** 卖家通过退款审核 */
        @Test
        void approveRefund() {
            Refund refund = new Refund();
            refund.setId(refundId);
            refund.setOrderId(orderId);
            refund.setPaymentId(paymentId);
            refund.setStatus("PENDING");

            Order order = new Order();
            order.setId(orderId);
            order.setProductName("有机大白菜");
            order.setSellerId(sellerId);

            when(refundRepository.selectById(refundId)).thenReturn(refund);
            when(orderRepository.selectById(orderId)).thenReturn(order);

            paymentService.reviewRefund(refundId, sellerId, true, null);

            verify(refundRepository).updateById(refundCaptor.capture());
            assertEquals("APPROVED", refundCaptor.getValue().getStatus());
        }

        /** 卖家拒绝退款审核 */
        @Test
        void rejectRefund() {
            Refund refund = new Refund();
            refund.setId(refundId);
            refund.setOrderId(orderId);
            refund.setPaymentId(paymentId);
            refund.setStatus("PENDING");

            Order order = new Order();
            order.setId(orderId);
            order.setProductName("有机大白菜");
            order.setSellerId(sellerId);

            when(refundRepository.selectById(refundId)).thenReturn(refund);
            when(orderRepository.selectById(orderId)).thenReturn(order);

            paymentService.reviewRefund(refundId, sellerId, false, "产品没问题");

            verify(refundRepository).updateById(refundCaptor.capture());
            assertEquals("REJECTED", refundCaptor.getValue().getStatus());
            assertEquals("产品没问题", refundCaptor.getValue().getRejectReason());
        }

        /** 非卖家审核退款被拒绝 */
        @Test
        void forbidden_whenNotSeller() {
            Refund refund = new Refund();
            refund.setId(refundId);
            refund.setOrderId(orderId);
            refund.setPaymentId(paymentId);
            refund.setStatus("PENDING");

            Order order = new Order();
            order.setId(orderId);
            order.setSellerId(sellerId);

            when(refundRepository.selectById(refundId)).thenReturn(refund);
            when(orderRepository.selectById(orderId)).thenReturn(order);

            var ex = assertThrows(BusinessException.class,
                () -> paymentService.reviewRefund(refundId, buyerId, true, null));
            assertEquals("仅卖家可以审核退款", ex.getMessage());
        }
    }

    // ======================== 查询方法测试 ========================

    @Nested
    class Queries {

        /** 查询订单的退款记录 */
        @Test
        void getRefundsByOrder() {
            when(refundRepository.findByOrderId(orderId))
                .thenReturn(List.of(new Refund()));
            assertEquals(1, paymentService.getRefundsByOrder(orderId).size());
        }

        /** 查询订单的支付记录 */
        @Test
        void getPaymentByOrder() {
            when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(new Payment()));
            assertNotNull(paymentService.getPaymentByOrder(orderId));
        }

        /** 支付记录不存在时返回 null */
        @Test
        void getPaymentByOrder_returnsNullWhenNotFound() {
            when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());
            assertNull(paymentService.getPaymentByOrder(orderId));
        }
    }
}
