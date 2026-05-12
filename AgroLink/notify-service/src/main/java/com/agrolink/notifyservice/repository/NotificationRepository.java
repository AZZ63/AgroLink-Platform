package com.agrolink.notifyservice.repository;

import com.agrolink.notifyservice.entity.Notification;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 通知数据访问层（Repository）
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供基本的 CRUD 能力，
 * 并通过默认方法封装常用的业务查询（按用户查通知、统计未读）。</p>
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * 根据用户 ID 查询其所有通知，按创建时间倒序排列
     *
     * @param userId 用户 ID
     * @return 该用户的通知列表
     */
    default List<Notification> findByUserId(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 统计指定用户的未读通知数量
     *
     * @param userId 用户 ID
     * @return 未读通知总数
     */
    default long countUnread(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false);
        return selectCount(wrapper);
    }
}
