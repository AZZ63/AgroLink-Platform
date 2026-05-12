package com.agrolink.common.dto;

import lombok.Data;
import java.util.List;

/**
 * 通用分页结果包装类。
 * <p>
 * 封装分页查询的返回结果，包含当前页数据、总记录数、当前页码、每页大小和总页数。
 * 通过静态工厂方法 {@link #of(List, long, int, int)} 快速创建实例。
 * </p>
 *
 * @param <T> 分页记录的类型
 */
@Data
public class PageResult<T> {

    /** 当前页的数据记录列表 */
    private List<T> records;

    /** 符合查询条件的总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页大小 */
    private int size;

    /** 总页数，根据 total 和 size 计算得出 */
    private long pages;

    /**
     * 静态工厂方法，构建分页结果对象。
     *
     * @param records 当前页记录列表
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页大小
     * @param <T>     记录类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.records = records;
        r.total = total;
        r.page = page;
        r.size = size;
        // 向上取整计算总页数：当 size > 0 时，(total + size - 1) / size 等价于 ceil(total / size)
        r.pages = (size > 0) ? (total + size - 1) / size : 0;
        return r;
    }
}
