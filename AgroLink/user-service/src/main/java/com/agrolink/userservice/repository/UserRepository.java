package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 用户数据访问层
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供用户表的基础 CRUD 及自定义查询方法。
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    /**
     * 根据用户名查询用户（Optional 包装，避免空指针）
     */
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)));
    }

    /**
     * 判断用户名是否已被注册
     */
    default boolean existsByUsername(String username) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }
}
