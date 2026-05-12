package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问层
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供角色表的基础 CRUD 操作。
 */
@Mapper
public interface RoleRepository extends BaseMapper<Role> {
}
