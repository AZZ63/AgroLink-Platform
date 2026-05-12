package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统权限数据访问层
 * <p>
 * 基于 MyBatis-Plus BaseMapper，提供权限表的基础 CRUD 操作。
 */
@Mapper
public interface SysPermissionRepository extends BaseMapper<SysPermission> {
}
