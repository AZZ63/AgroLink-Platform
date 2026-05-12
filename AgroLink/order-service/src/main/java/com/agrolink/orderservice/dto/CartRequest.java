package com.agrolink.orderservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 加入购物车请求
 */
@Data
public class CartRequest {
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
