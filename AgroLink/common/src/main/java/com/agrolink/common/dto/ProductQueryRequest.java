package com.agrolink.common.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 产品查询请求 DTO。
 * <p>
 * 封装农产品查询 / 筛选条件，支持关键字搜索、按类型/状态/地区筛选、
 * 价格区间过滤、排序以及批量 ID 查询。
 * </p>
 */
@Data
public class ProductQueryRequest {

    /** 搜索关键字（匹配产品名称或描述） */
    private String keyword;

    /** 产品分类 / 类型筛选 */
    private String type;

    /** 信息类型筛选：SUPPLY（供应）或 DEMAND（需求） */
    private String infoType;

    /**
     * 状态筛选。
     * <ul>
     *   <li>供应（SUPPLY）：LISTED / SOLD / UNLISTED</li>
     *   <li>需求（DEMAND）：PENDING / COMPLETED / CLOSED</li>
     * </ul>
     */
    private String status;

    /** 省份筛选 */
    private String province;

    /** 城市筛选 */
    private String city;

    /** 最低价格筛选 */
    private BigDecimal minPrice;

    /** 最高价格筛选 */
    private BigDecimal maxPrice;

    /** 排序字段：price（价格）或 created_at（创建时间） */
    private String sortBy;

    /** 排序方向：asc（升序）或 desc（降序） */
    private String sortOrder;

    /** 逗号分隔的产品 ID 列表，用于批量查询 */
    private String ids;

    /** 当前页码，默认 1 */
    private Integer page = 1;

    /** 每页大小，默认 20 */
    private Integer size = 20;
}
