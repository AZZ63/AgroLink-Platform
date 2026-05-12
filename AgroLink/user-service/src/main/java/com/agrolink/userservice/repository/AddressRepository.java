package com.agrolink.userservice.repository;

import com.agrolink.userservice.entity.Address;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收货地址数据访问层
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供地址表的基础 CRUD 及自定义查询方法。
 */
@Mapper
public interface AddressRepository extends BaseMapper<Address> {

    /**
     * 根据用户 ID 查询其所有地址，按默认地址优先、创建时间倒序排列
     */
    default List<Address> findByUserId(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault).orderByDesc(Address::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 查询指定用户的默认地址
     */
    default Address findDefaultByUserId(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, true);
        return selectOne(wrapper);
    }
}
