package com.agrolink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 统计数据 DTO。
 * <p>
 * 用于封装仪表盘或管理后台所需的统计数据，
 * 包括每日浏览量趋势、产品总数和活跃订单数。
 * </p>
 */
@Data
@AllArgsConstructor
public class StatsDTO {

    /** 每日浏览量的列表，按日期顺序排列，与 dates 列表一一对应 */
    private List<Integer> dailyViews;

    /** 日期字符串列表，格式如 "2024-01-01"，与 dailyViews 一一对应 */
    private List<String> dates;

    /** 产品总数 */
    private long totalProducts;

    /** 活跃订单数 */
    private long activeOrders;
}
