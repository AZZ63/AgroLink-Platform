package com.agrolink.orderservice.controller;

import com.agrolink.common.result.Result;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.orderservice.entity.Payment;
import com.agrolink.orderservice.entity.Refund;
import com.agrolink.orderservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 * 提供支付创建、支付回调、退款申请/审核等 API
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付（模拟）
     * @param body   请求体，包含 orderId 订单ID
     * @param userId 买家用户ID
     * @param role   用户角色
     * @return 交易号 tradeNo 和模拟支付链接
     */
    @PostMapping("/create")
    public Result<Map<String, String>> createPayment(@RequestBody Map<String, Long> body,
                                                      @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId,
                                                      @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        String tradeNo = paymentService.createPayment(body.get("orderId"), userId, role);
        return Result.success(Map.of("tradeNo", tradeNo, "payUrl", "/mock-pay?tradeNo=" + tradeNo));
    }

    /**
     * 支付回调（模拟）
     * 支付成功后由模拟支付系统调用此接口更新订单状态
     * @param body 请求体，包含 tradeNo 交易号
     */
    @PostMapping("/callback")
    public Result<Void> paymentCallback(@RequestBody Map<String, String> body) {
        paymentService.paySuccess(body.get("tradeNo"));
        return Result.success();
    }

    /**
     * 发起退款申请
     * @param body   请求体，包含 orderId（订单ID）、reasonType（原因类型）、reasonDetail（详情）、images（图片）
     * @param userId 当前用户ID（买家）
     * @return 退款记录
     */
    @PostMapping("/refund/request")
    public Result<Refund> requestRefund(@RequestBody Map<String, Object> body,
                                         @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reasonType = (String) body.getOrDefault("reasonType", "OTHER");
        String reasonDetail = (String) body.getOrDefault("reasonDetail", "");
        String images = (String) body.getOrDefault("images", "");
        Refund refund = paymentService.requestRefund(orderId, userId, reasonType, reasonDetail, images);
        return Result.success(refund);
    }

    /**
     * 审核退款申请
     * @param body   请求体，包含 refundId（退款ID）、approved（是否通过）、rejectReason（拒绝原因）
     * @param userId 当前用户ID（卖家）
     */
    @PostMapping("/refund/review")
    public Result<Void> reviewRefund(@RequestBody Map<String, Object> body,
                                      @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        Long refundId = Long.valueOf(body.get("refundId").toString());
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String rejectReason = (String) body.getOrDefault("rejectReason", "");
        paymentService.reviewRefund(refundId, userId, approved, rejectReason);
        return Result.success();
    }

    /**
     * 根据订单ID获取退款记录列表
     * @param orderId 订单ID
     * @return 退款记录列表
     */
    @GetMapping("/refund/{orderId}")
    public Result<List<Refund>> getRefunds(@PathVariable Long orderId) {
        return Result.success(paymentService.getRefundsByOrder(orderId));
    }

    /**
     * 获取卖家待审核的退款申请列表
     * @param userId 当前用户ID（卖家）
     * @return 待审核退款列表
     */
    @GetMapping("/refund/pending")
    public Result<List<Refund>> getPendingRefunds(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(paymentService.getPendingRefundsForSeller(userId));
    }

    /**
     * 根据订单ID获取支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    @GetMapping("/{orderId}")
    public Result<Payment> getPayment(@PathVariable Long orderId) {
        return Result.success(paymentService.getPaymentByOrder(orderId));
    }

}
