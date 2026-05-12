package com.agrolink.productservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.productservice.entity.Product;
import com.agrolink.productservice.entity.Review;
import com.agrolink.productservice.repository.ProductRepository;
import com.agrolink.productservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 评价服务
 * <p>
 * 提供评价的创建、查询（按产品/订单/用户）、评分统计和删除功能。
 * 创建评价时校验产品存在性、订单是否已评价以及评分的合法性。
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    /**
     * 创建评价
     * <p>
     * 校验产品存在、订单尚未评价、评分在 1-5 范围内后，保存评价记录。
     *
     * @param orderId   订单 ID
     * @param productId 产品 ID
     * @param fromUserId 评价人 ID
     * @param toUserId   被评价人 ID
     * @param rating    评分（1-5）
     * @param content   评价内容
     * @return 创建成功的评价实体
     * @throws BusinessException 如果产品不存在、订单已评价或评分不合法则抛异常
     */
    @Transactional
    public Review createReview(Long orderId, Long productId, Long fromUserId, Long toUserId,
                                Integer rating, String content) {
        // 校验产品是否存在
        Product product = productRepository.selectById(productId);
        if (product == null) throw new BusinessException(404, "产品不存在");

        // 校验订单是否已评价（一个订单只能有一条评价）
        Review existing = reviewRepository.findByOrderId(orderId);
        if (existing != null) throw new BusinessException(400, "该订单已评价");

        // 校验评分范围
        if (rating < 1 || rating > 5) throw new BusinessException(400, "评分必须在1-5之间");

        // 创建并保存评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setFromUserId(fromUserId);
        review.setToUserId(toUserId);
        review.setRating(rating);
        review.setContent(content != null ? content.trim() : "");
        reviewRepository.insert(review);
        return review;
    }

    /**
     * 获取指定产品的所有评价
     *
     * @param productId 产品 ID
     * @return 评价列表
     */
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    /**
     * 获取指定用户的评价列表（作为被评价人）
     *
     * @param userId 用户 ID
     * @return 评价列表
     */
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    /**
     * 获取指定订单的评价
     *
     * @param orderId 订单 ID
     * @return 评价实体，可能为 null
     */
    public Review getOrderReview(Long orderId) {
        return reviewRepository.findByOrderId(orderId);
    }

    /**
     * 获取指定产品的评分统计
     *
     * @param productId 产品 ID
     * @return 包含 average（平均分）和 count（评价数）的 Map
     */
    public Map<String, Object> getProductRating(Long productId) {
        double avg = reviewRepository.averageRatingByProductId(productId);
        int count = reviewRepository.countByProductId(productId);
        return Map.of("average", avg, "count", count);
    }

    /**
     * 删除评价
     * <p>
     * 只有评价人本人可以删除自己的评价。
     *
     * @param reviewId 评价 ID
     * @param userId   当前用户 ID
     * @throws BusinessException 如果评价不存在或无权限则抛异常
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) throw new BusinessException(404, "评价不存在");
        if (!review.getFromUserId().equals(userId)) throw new BusinessException(403, "无权删除");
        reviewRepository.deleteById(reviewId);
    }
}
