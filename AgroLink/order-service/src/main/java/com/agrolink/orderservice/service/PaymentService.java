package com.agrolink.orderservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.orderservice.entity.Order;
import com.agrolink.orderservice.entity.Payment;
import com.agrolink.orderservice.entity.Refund;
import com.agrolink.orderservice.repository.OrderRepository;
import com.agrolink.orderservice.repository.PaymentRepository;
import com.agrolink.orderservice.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 支付服务
 * 处理支付的创建、支付成功回调、以及企业级退款流程（申请 -> 审核 -> 处理）
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RestTemplate restTemplate;

    /** 有效的退款原因类型 */
    private static final Set<String> VALID_REASON_TYPES = Set.of("QUALITY", "QUANTITY", "WRONG", "OTHER");

    // ========== 支付流程 ==========

    /**
     * 创建支付记录（模拟）
     * 校验订单状态和权限后生成交易号
     * @param orderId 订单ID
     * @param userId  当前用户ID（必须是买家）
     * @param role    用户角色
     * @return 交易号 tradeNo
     */
    @Transactional
    public String createPayment(Long orderId, Long userId, String role) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (!"UNPAID".equals(order.getStatus())) throw new BusinessException(400, "订单状态不允许支付");

        // 生成模拟交易号
        String tradeNo = "MOCK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setTradeNo(tradeNo);
        payment.setAmount(order.getTotalPrice());
        payment.setMethod("MOCK");
        payment.setStatus("PENDING");
        paymentRepository.insert(payment);
        return tradeNo;
    }

    /**
     * 支付成功回调
     * 更新支付记录状态为 SUCCESS，并将订单状态改为 PAID
     * @param tradeNo 交易号
     */
    @Transactional
    public void paySuccess(String tradeNo) {
        Payment payment = paymentRepository.findByTradeNo(tradeNo)
                .orElseThrow(() -> new BusinessException(404, "支付记录不存在"));
        if (!"PENDING".equals(payment.getStatus())) throw new BusinessException(400, "支付已处理");

        // 更新支付记录
        payment.setStatus("SUCCESS");
        payment.setPayTime(LocalDateTime.now());
        paymentRepository.updateById(payment);

        // 更新订单状态
        Order order = orderRepository.selectById(payment.getOrderId());
        order.setStatus("PAID");
        order.setPayStatus("PAID");
        order.setPayAmount(payment.getAmount());
        order.setPayTime(LocalDateTime.now());
        orderRepository.updateById(order);
    }

    // ========== 企业级退款流程 ==========

    /**
     * 第一步：买家发起退款申请
     * 校验订单状态、退款原因类型后创建退款记录，并将订单状态置为 REFUNDING
     * @param orderId      订单ID
     * @param userId       当前用户ID（买家）
     * @param reasonType   退款原因类型
     * @param reasonDetail 退款原因详情
     * @param images       图片凭证
     * @return 退款记录
     */
    @Transactional
    public Refund requestRefund(Long orderId, Long userId, String reasonType, String reasonDetail, String images) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(403, "仅买家可申请退款");
        if (!Set.of("PAID", "CONFIRMED").contains(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许退款");
        }
        if (!VALID_REASON_TYPES.contains(reasonType)) {
            throw new BusinessException(400, "无效的退款原因类型");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(404, "支付记录不存在"));

        // 创建退款记录
        String refundNo = "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setOrderId(orderId);
        refund.setRefundNo(refundNo);
        refund.setAmount(order.getPayAmount() != null ? order.getPayAmount() : order.getTotalPrice());
        refund.setReasonType(reasonType);
        refund.setReasonDetail(reasonDetail);
        refund.setImages(images);
        refund.setStatus("PENDING");
        refund.setAppliedBy(userId);
        refund.setAppliedAt(LocalDateTime.now());
        refundRepository.insert(refund);

        // 更新订单为退款中状态
        order.setStatus("REFUNDING");
        order.setPayStatus("REFUNDING");
        orderRepository.updateById(order);

        // 通知卖家有退款申请
        sendNotification(order.getSellerId(), "REFUND_REQUESTED", "退款申请",
            "买家申请退款：" + order.getProductName() + "，原因：" + reasonLabel(reasonType), orderId);

        return refund;
    }

    /**
     * 第二步：卖家审核退款申请
     * 审核通过则调用 processApprovedRefund 处理退款，
     * 审核拒绝则恢复订单状态为 PAID
     * @param refundId     退款ID
     * @param reviewerId   审核人ID（卖家）
     * @param approved     是否通过
     * @param rejectReason 拒绝原因
     */
    @Transactional
    public void reviewRefund(Long refundId, Long reviewerId, boolean approved, String rejectReason) {
        Refund refund = refundRepository.selectById(refundId);
        if (refund == null) throw new BusinessException(404, "退款申请不存在");
        if (!"PENDING".equals(refund.getStatus())) throw new BusinessException(400, "退款申请已处理");

        Order order = orderRepository.selectById(refund.getOrderId());
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getSellerId().equals(reviewerId)) throw new BusinessException(403, "仅卖家可以审核退款");

        refund.setReviewedBy(reviewerId);
        refund.setReviewedAt(LocalDateTime.now());

        if (approved) {
            refund.setStatus("APPROVED");
            refundRepository.updateById(refund);
            processApprovedRefund(refund, order);
        } else {
            refund.setStatus("REJECTED");
            refund.setRejectReason(rejectReason);
            refundRepository.updateById(refund);

            // 恢复订单到之前的状态
            order.setStatus("PAID");
            order.setPayStatus("PAID");
            orderRepository.updateById(order);

            sendNotification(order.getBuyerId(), "REFUND_REJECTED", "退款被拒绝",
                "卖家拒绝了退款申请" + (rejectReason != null ? "：" + rejectReason : ""), order.getId());
        }
    }

    /**
     * 第三步：执行已审核通过的退款
     * 更新退款状态为 SUCCESS，订单状态变更为 REFUNDED
     * @param refund 退款记录
     * @param order  订单实体
     */
    private void processApprovedRefund(Refund refund, Order order) {
        Payment payment = paymentRepository.selectById(refund.getPaymentId());
        if (payment != null) {
            // 模拟退款处理
            refund.setStatus("SUCCESS");
            refund.setSuccessAt(LocalDateTime.now());
            refundRepository.updateById(refund);

            order.setStatus("REFUNDED");
            order.setPayStatus("REFUNDED");
            order.setRefundTime(LocalDateTime.now());
            orderRepository.updateById(order);

            // 通知买家和卖家退款结果
            sendNotification(order.getBuyerId(), "REFUND_SUCCESS", "退款成功",
                "订单 " + order.getProductName() + " 已退款 ¥" + refund.getAmount(), order.getId());
            sendNotification(order.getSellerId(), "REFUND_SUCCESS", "退款完成",
                "订单 " + order.getProductName() + " 退款已完成", order.getId());
        }
    }

    // ========== 查询方法 ==========

    /**
     * 根据订单ID获取退款记录列表
     */
    public List<Refund> getRefundsByOrder(Long orderId) {
        return refundRepository.findByOrderId(orderId);
    }

    /**
     * 获取卖家待审核的退款申请
     */
    public List<Refund> getPendingRefundsForSeller(Long sellerId) {
        return refundRepository.findPendingBySeller(sellerId);
    }

    /**
     * 根据订单ID获取支付记录
     */
    public Payment getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    // ========== 辅助方法 ==========

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
     * 将退款原因类型转换为中文标签
     */
    private String reasonLabel(String type) {
        return switch (type) {
            case "QUALITY" -> "品质问题";
            case "QUANTITY" -> "数量不符";
            case "WRONG" -> "发错货";
            default -> "其他";
        };
    }
}
