package com.agrolink.orderservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.orderservice.dto.CartDTO;
import com.agrolink.orderservice.dto.CartRequest;
import com.agrolink.orderservice.dto.CartUpdateRequest;
import com.agrolink.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 * 提供加入购物车、修改数量、删除、勾选结算等 RESTful API
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 加入购物车
     */
    @PostMapping("/add")
    public Result<CartDTO> add(@Valid @RequestBody CartRequest request,
                               @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(cartService.add(userId, request));
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartDTO>> list(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(cartService.list(userId));
    }

    /**
     * 获取选中项（用于结算）
     */
    @GetMapping("/checked")
    public Result<List<CartDTO>> getChecked(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(cartService.getChecked(userId));
    }

    /**
     * 获取选中数量
     */
    @GetMapping("/checked/count")
    public Result<Long> getCheckedCount(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(cartService.getCheckedCount(userId));
    }

    /**
     * 更新购物车项（数量/选中状态）
     */
    @PutMapping("/{id}")
    public Result<CartDTO> update(@PathVariable Long id,
                                   @Valid @RequestBody CartUpdateRequest request,
                                   @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(cartService.update(userId, id, request));
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/toggle-all")
    public Result<Void> toggleAll(@RequestParam Boolean checked,
                                   @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        cartService.toggleAll(userId, checked);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id,
                               @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        cartService.remove(userId, id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> removeBatch(@RequestBody List<Long> cartIds,
                                    @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        cartService.removeBatch(userId, cartIds);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        cartService.clear(userId);
        return Result.success();
    }
}
