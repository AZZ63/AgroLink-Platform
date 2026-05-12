package com.agrolink.productservice.repository;

import com.agrolink.productservice.entity.Favorite;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收藏数据访问层
 * <p>
 * 提供对 favorites 表的数据库操作，封装了常用的收藏查询方法。
 * 使用 MyBatis-Plus 的 BaseMapper 和 LambdaQueryWrapper 实现。
 */
@Mapper
public interface FavoriteRepository extends BaseMapper<Favorite> {

    /**
     * 根据用户 ID 和产品 ID 查找收藏记录
     *
     * @param userId    用户 ID
     * @param productId 产品 ID
     * @return 收藏记录，不存在则返回 null
     */
    default Favorite findByUserAndProduct(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        return selectOne(wrapper);
    }

    /**
     * 查询指定用户的所有收藏（按收藏时间倒序）
     *
     * @param userId 用户 ID
     * @return 收藏列表
     */
    default List<Favorite> findByUserId(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 查询指定用户收藏的所有产品 ID 列表
     *
     * @param userId 用户 ID
     * @return 产品 ID 列表
     */
    default List<Long> findProductIdsByUserId(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.select(Favorite::getProductId);
        return selectList(wrapper).stream().map(Favorite::getProductId).toList();
    }

    /**
     * 统计指定产品的收藏数量
     *
     * @param productId 产品 ID
     * @return 收藏数
     */
    default int countByProductId(Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getProductId, productId);
        return Math.toIntExact(selectCount(wrapper));
    }

    /**
     * 判断指定用户是否已收藏指定产品
     *
     * @param userId    用户 ID
     * @param productId 产品 ID
     * @return true 表示已收藏
     */
    default boolean exists(Long userId, Long productId) {
        return findByUserAndProduct(userId, productId) != null;
    }
}
