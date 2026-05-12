package com.agrolink.orderservice.repository;

import com.agrolink.orderservice.entity.Cart;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 购物车仓库接口
 * 继承 MyBatis-Plus BaseMapper，提供购物车表的通用 CRUD 操作
 */
@Mapper
public interface CartRepository extends BaseMapper<Cart> {

    /**
     * 查询用户的购物车列表，按创建时间倒序
     */
    default List<Cart> findByUserId(Long userId) {
        LambdaQueryWrapper<Cart> w = new LambdaQueryWrapper<>();
        w.eq(Cart::getUserId, userId);
        w.orderByDesc(Cart::getCreatedAt);
        return selectList(w);
    }

    /**
     * 查询用户选中的购物车项
     */
    default List<Cart> findCheckedByUserId(Long userId) {
        LambdaQueryWrapper<Cart> w = new LambdaQueryWrapper<>();
        w.eq(Cart::getUserId, userId);
        w.eq(Cart::getChecked, true);
        return selectList(w);
    }

    /**
     * 查询用户某个产品的购物车记录
     */
    default Cart findByUserAndProduct(Long userId, Long productId) {
        LambdaQueryWrapper<Cart> w = new LambdaQueryWrapper<>();
        w.eq(Cart::getUserId, userId);
        w.eq(Cart::getProductId, productId);
        return selectOne(w);
    }
}
