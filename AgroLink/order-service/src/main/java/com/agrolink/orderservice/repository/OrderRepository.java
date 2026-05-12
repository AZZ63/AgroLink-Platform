package com.agrolink.orderservice.repository;

import com.agrolink.orderservice.entity.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单仓库接口
 * 继承 MyBatis-Plus BaseMapper，提供订单表的通用 CRUD 操作
 * 包含按买家/卖家查询的自定义方法
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {

    /**
     * 根据买家ID查询订单列表，按创建时间倒序
     * @param buyerId 买家用户ID
     * @return 订单列表
     */
    default List<Order> findByBuyerId(Long buyerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, buyerId);
        wrapper.orderByDesc(Order::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据卖家ID查询订单列表，按创建时间倒序
     * @param sellerId 卖家用户ID
     * @return 订单列表
     */
    default List<Order> findBySellerId(Long sellerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, sellerId);
        wrapper.orderByDesc(Order::getCreatedAt);
        return selectList(wrapper);
    }
}
