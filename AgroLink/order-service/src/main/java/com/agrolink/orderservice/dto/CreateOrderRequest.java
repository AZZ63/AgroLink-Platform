package com.agrolink.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求 DTO
 * 包含产品ID和购买数量
 */
@Data
public class CreateOrderRequest {
    /** 产品ID，不能为空 */
    @NotNull
    private Long productId;

    /** 购买数量，不能为空且至少为 1 */
    @NotNull
    @Min(1)
    private Integer quantity;
}
