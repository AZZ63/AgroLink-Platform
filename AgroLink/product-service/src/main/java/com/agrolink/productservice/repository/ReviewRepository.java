package com.agrolink.productservice.repository;

import com.agrolink.productservice.entity.Review;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价数据访问层
 * <p>
 * 提供对 reviews 表的数据库操作，封装了按产品、按用户、按订单查询评价
 * 以及评分统计等常用方法。
 */
@Mapper
public interface ReviewRepository extends BaseMapper<Review> {

    /**
     * 根据产品 ID 查询所有评价（按创建时间倒序）
     *
     * @param productId 产品 ID
     * @return 评价列表
     */
    default List<Review> findByProductId(Long productId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        wrapper.orderByDesc(Review::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据被评价人用户 ID 查询所有评价（按创建时间倒序）
     *
     * @param userId 被评价人用户 ID
     * @return 评价列表
     */
    default List<Review> findByUserId(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getToUserId, userId);
        wrapper.orderByDesc(Review::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据订单 ID 查询评价（订单与评价为一对一关系）
     *
     * @param orderId 订单 ID
     * @return 评价实体，不存在则返回 null
     */
    default Review findByOrderId(Long orderId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        return selectOne(wrapper);
    }

    /**
     * 获取指定产品的平均评分（四舍五入保留一位小数）
     *
     * @param productId 产品 ID
     * @return 平均评分，无评价时返回 0.0
     */
    @Select("SELECT COALESCE(ROUND(AVG(rating), 1), 0) FROM reviews WHERE product_id = #{productId}")
    Double averageRatingByProductId(Long productId);

    /**
     * 统计指定产品的评价数量
     *
     * @param productId 产品 ID
     * @return 评价数
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE product_id = #{productId}")
    int countByProductId(Long productId);
}
