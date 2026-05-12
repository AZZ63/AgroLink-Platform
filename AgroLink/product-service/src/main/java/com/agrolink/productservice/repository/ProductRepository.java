package com.agrolink.productservice.repository;

import com.agrolink.productservice.entity.Product;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品数据访问层
 * <p>
 * 提供对 products 表的数据库操作，封装了多维条件分页查询、
 * 热门产品查询、批量查询等常用方法。
 */
@Mapper
public interface ProductRepository extends BaseMapper<Product> {

    /**
     * 按多维度筛选条件分页查询产品
     * <p>
     * 支持关键词模糊搜索、类型、信息类型、状态、地区（省/市）、
     * 价格范围筛选以及自定义排序字段和排序方向。
     *
     * @param keyword  搜索关键词（模糊匹配产品名称）
     * @param type     产品类型
     * @param infoType 信息类型（SUPPLY / DEMAND）
     * @param status   产品状态
     * @param province 省份
     * @param city     城市
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param sortBy   排序字段（createdAt 或 price）
     * @param sortOrder 排序方向（asc / desc）
     * @param page     页码
     * @param size     每页大小
     * @return 分页结果
     */
    default IPage<Product> queryByFilters(String keyword, String type, String infoType,
                                           String status, String province, String city,
                                           BigDecimal minPrice, BigDecimal maxPrice,
                                           String sortBy, String sortOrder,
                                           int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 关键词模糊匹配
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        // 产品类型精确匹配
        if (type != null && !type.isBlank()) {
            wrapper.eq(Product::getType, type);
        }
        // 信息类型精确匹配（供应/需求）
        if (infoType != null && !infoType.isBlank()) {
            wrapper.eq(Product::getInfoType, infoType);
        }
        // 状态精确匹配
        if (status != null && !status.isBlank()) {
            wrapper.eq(Product::getStatus, status);
        }
        // 省份精确匹配
        if (province != null && !province.isBlank()) {
            wrapper.eq(Product::getProvince, province);
        }
        // 城市精确匹配
        if (city != null && !city.isBlank()) {
            wrapper.eq(Product::getCity, city);
        }
        // 价格范围筛选
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        // 排序处理
        SFunction<Product, ?> sortField = Product::getCreatedAt;
        if ("price".equals(sortBy)) {
            sortField = Product::getPrice;
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            wrapper.orderByAsc(sortField);
        } else {
            wrapper.orderByDesc(sortField);
        }
        return selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询所有不重复的产品类型
     */
    @Select("SELECT DISTINCT type FROM products ORDER BY type")
    List<String> findAllTypes();

    /**
     * 查询所有不重复的省份
     */
    @Select("SELECT DISTINCT province FROM products WHERE province IS NOT NULL ORDER BY province")
    List<String> findAllProvinces();

    /**
     * 增加产品浏览次数（原子操作）
     *
     * @param id    产品 ID
     * @param count 增加的次数
     */
    @Select("UPDATE products SET view_count = view_count + #{count} WHERE id = #{id}")
    void addViewCount(Long id, int count);

    /**
     * 查询最新发布的产品（热门产品）
     *
     * @param limit 限制数量
     * @return 产品列表
     */
    default List<Product> findHotProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return selectList(wrapper);
    }

    /**
     * 根据 ID 列表批量查询产品（保持传入顺序无关）
     *
     * @param ids 产品 ID 列表
     * @return 产品列表
     */
    default List<Product> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, ids);
        return selectList(wrapper);
    }

    /**
     * 查询指定用户发布的所有产品（按创建时间倒序）
     *
     * @param userId 用户 ID
     * @return 产品列表
     */
    default List<Product> findByUserId(Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        wrapper.orderByDesc(Product::getCreatedAt);
        return selectList(wrapper);
    }
}
