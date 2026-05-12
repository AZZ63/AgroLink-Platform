package com.agrolink.productservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.productservice.entity.Review;
import com.agrolink.productservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 * <p>
 * 提供产品评价的创建、查询（按产品/订单/用户）、评分统计和删除功能。
 */
@RestController
@RequestMapping("/api/product/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     * <p>
     * 订单交易完成后，买家或卖家可以对交易进行评价。
     *
     * @param body   请求体，包含 orderId、productId、toUserId、rating、content
     * @param userId 评价人 ID（当前用户）
     * @return 创建成功的评价实体
     */
    @PostMapping("/create")
    public Result<Review> create(@RequestBody Map<String, Object> body,
                                  @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        Long toUserId = Long.valueOf(body.get("toUserId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = (String) body.getOrDefault("content", "");
        return Result.success(reviewService.createReview(orderId, productId, userId, toUserId, rating, content));
    }

    /**
     * 获取指定产品的所有评价
     *
     * @param productId 产品 ID
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<Review>> getProductReviews(@PathVariable Long productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }

    /**
     * 获取指定产品的评分统计（平均分 + 评价数）
     *
     * @param productId 产品 ID
     * @return 包含 average 和 count 的 Map
     */
    @GetMapping("/product/{productId}/rating")
    public Result<Map<String, Object>> getProductRating(@PathVariable Long productId) {
        return Result.success(reviewService.getProductRating(productId));
    }

    /**
     * 获取指定订单的评价
     *
     * @param orderId 订单 ID
     * @return 评价实体，可能为空
     */
    @GetMapping("/order/{orderId}")
    public Result<Review> getOrderReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReview(orderId));
    }

    /**
     * 获取指定用户的评价列表（作为被评价人）
     *
     * @param userId 用户 ID
     * @return 评价列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Review>> getUserReviews(@PathVariable Long userId) {
        return Result.success(reviewService.getUserReviews(userId));
    }

    /**
     * 删除评价
     *
     * @param reviewId 评价 ID
     * @param userId   当前用户 ID（用于校验是否评价人本人）
     */
    @DeleteMapping("/{reviewId}")
    public Result<Void> delete(@PathVariable Long reviewId,
                                @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return Result.success();
    }

}
