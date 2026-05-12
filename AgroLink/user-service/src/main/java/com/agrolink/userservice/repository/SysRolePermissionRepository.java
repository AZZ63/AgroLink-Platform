package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联数据访问层
 * <p>
 * 基于 MyBatis-Plus BaseMapper，提供 sys_role_permission 表的基础 CRUD 操作。
 */
@Mapper
public interface SysRolePermissionRepository extends BaseMapper<SysRolePermission> {
}
