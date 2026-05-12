package com.agrolink.orderservice.dto;

import lombok.Data;

/**
 * 购物车更新请求
 * 用于修改数量或选中状态
 */
@Data
public class CartUpdateRequest {
    private Integer quantity;
    private Boolean checked;
}
