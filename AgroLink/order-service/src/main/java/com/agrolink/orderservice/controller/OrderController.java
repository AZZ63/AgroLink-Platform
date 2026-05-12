package com.agrolink.orderservice.controller;

import com.agrolink.common.result.Result;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.orderservice.dto.CreateOrderRequest;
import com.agrolink.orderservice.dto.OrderDTO;
import com.agrolink.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * 提供订单的创建、状态更新、查询等 RESTful API
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * @param request 创建订单请求体（含产品ID和数量）
     * @param userId  买家用户ID（从请求头获取）
     * @param role    用户角色（从请求头获取）
     * @return 创建的订单详情
     */
    @PostMapping("/create")
    public Result<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                         @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId,
                                         @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        return Result.success(orderService.createOrder(request, userId, role));
    }

    /**
     * 更新订单状态
     * @param id     订单ID
     * @param status 目标状态（如 CONFIRMED / COMPLETED / CANCELLED）
     * @param userId 操作用户ID
     * @param role   用户角色
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId,
                                      @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        orderService.updateStatus(id, userId, status, role);
        return Result.success();
    }

    /**
     * 获取订单详情
     * @param id     订单ID
     * @param userId 当前用户ID（用于权限校验）
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderDTO> getOrder(@PathVariable Long id,
                                      @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(orderService.getOrderDetail(id, userId));
    }

    /**
     * 获取我相关的所有订单（作为买家或卖家）
     * @param userId 当前用户ID
     * @return 订单列表
     */
    @GetMapping("/my")
    public Result<List<OrderDTO>> getMyOrders(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(orderService.getMyOrders(userId));
    }

    /**
     * 获取我的采购订单（作为买家）
     * @param userId 当前用户ID
     * @return 订单列表
     */
    @GetMapping("/buy")
    public Result<List<OrderDTO>> getBuyOrders(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(orderService.getBuyerOrders(userId));
    }

    /**
     * 获取我的销售订单（作为卖家）
     * @param userId 当前用户ID
     * @return 订单列表
     */
    @GetMapping("/sell")
    public Result<List<OrderDTO>> getSellOrders(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(orderService.getSellerOrders(userId));
    }

}
