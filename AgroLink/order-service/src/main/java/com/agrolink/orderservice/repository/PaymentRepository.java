package com.agrolink.orderservice.repository;

import com.agrolink.orderservice.entity.Payment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录仓库接口
 * 继承 MyBatis-Plus BaseMapper，提供支付表的基础 CRUD 操作
 * 包含按订单ID、交易号查询的自定义方法
 */
@Mapper
public interface PaymentRepository extends BaseMapper<Payment> {
    /**
     * 按订单ID查询最近一条支付记录
     * @param orderId 订单ID
     * @return 支付记录（Optional）
     */
    default Optional<Payment> findByOrderId(Long orderId) {
        List<Payment> list = selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getCreatedAt)
                .last("LIMIT 1"));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 按交易号查询支付记录
     * @param tradeNo 交易号
     * @return 支付记录（Optional）
     */
    default Optional<Payment> findByTradeNo(String tradeNo) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getTradeNo, tradeNo)));
    }

    /**
     * 按订单ID查询所有支付记录，按创建时间倒序
     * @param orderId 订单ID
     * @return 支付记录列表
     */
    default List<Payment> findAllByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getCreatedAt));
    }
}
