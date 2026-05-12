package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联数据访问层
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供 user_role 表的基础 CRUD 操作。
 */
@Mapper
public interface UserRoleRepository extends BaseMapper<UserRole> {
}
