package com.agrolink.orderservice.repository;

import com.agrolink.orderservice.entity.Refund;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 退款记录仓库接口
 * 继承 MyBatis-Plus BaseMapper，提供退款表的基础 CRUD 操作
 * 包含按订单ID和按卖家查询待审核退款的方法
 */
@Mapper
public interface RefundRepository extends BaseMapper<Refund> {

    /**
     * 按订单ID查询退款记录，按创建时间倒序
     * @param orderId 订单ID
     * @return 退款记录列表
     */
    default List<Refund> findByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getOrderId, orderId)
                .orderByDesc(Refund::getCreatedAt));
    }

    /**
     * 查询指定卖家的所有待审核退款申请
     * 通过联表查询关联订单的卖家ID
     * @param sellerId 卖家用户ID
     * @return 待审核退款列表
     */
    @Select("SELECT r.* FROM refunds r JOIN orders o ON r.order_id = o.id " +
            "WHERE o.seller_id = #{sellerId} AND r.status = 'PENDING' " +
            "ORDER BY r.created_at DESC")
    List<Refund> findPendingBySeller(Long sellerId);
}
