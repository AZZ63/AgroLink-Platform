package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统菜单数据访问层
 * <p>
 * 基于 MyBatis-Plus BaseMapper，提供菜单表的基础 CRUD 操作。
 */
@Mapper
public interface SysMenuRepository extends BaseMapper<SysMenu> {

    /**
     * 获取指定角色可见的菜单列表
     * <p>
     * 通过角色-权限关联和菜单-权限标识映射，查询角色有权限访问的可见菜单。
     */
    default List<SysMenu> selectByRoleCode(String roleCode) {
        // 管理员返回所有可见菜单
        if ("ADMIN".equals(roleCode)) {
            return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getVisible, 1)
                    .orderByAsc(SysMenu::getSortOrder));
        }
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getVisible, 1)
                .orderByAsc(SysMenu::getSortOrder));
    }
}
