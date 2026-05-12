package com.agrolink.productservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.productservice.entity.Favorite;
import com.agrolink.productservice.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 * <p>
 * 提供产品的收藏/取消收藏切换、收藏列表查询、收藏数量统计以及检查是否已收藏等功能。
 */
@RestController
@RequestMapping("/api/product/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 切换收藏状态（已收藏则取消，未收藏则添加）
     *
     * @param productId 产品 ID
     * @param userId    当前用户 ID（从请求头获取）
     * @return 包含收藏状态和当前收藏数的 Map
     */
    @PostMapping("/{productId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long productId,
                                               @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        Map<String, Object> result = favoriteService.toggle(userId, productId);
        return Result.success(result);
    }

    /**
     * 获取当前用户的收藏列表
     *
     * @param userId 当前用户 ID
     * @return 收藏实体列表
     */
    @GetMapping("/list")
    public Result<List<Favorite>> list(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(favoriteService.getUserFavorites(userId));
    }

    /**
     * 获取当前用户收藏的产品 ID 列表
     *
     * @param userId 当前用户 ID
     * @return 产品 ID 列表
     */
    @GetMapping("/ids")
    public Result<List<Long>> ids(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(favoriteService.getUserFavoriteProductIds(userId));
    }

    /**
     * 查询指定产品的收藏数量
     *
     * @param productId 产品 ID
     * @return 收藏数
     */
    @GetMapping("/count/{productId}")
    public Result<Integer> count(@PathVariable Long productId) {
        return Result.success(favoriteService.getFavoriteCount(productId));
    }

    /**
     * 检查当前用户是否已收藏指定产品
     *
     * @param productId 产品 ID
     * @param userId    当前用户 ID
     * @return true 表示已收藏
     */
    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@PathVariable Long productId,
                                  @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(favoriteService.isFavorited(userId, productId));
    }

}
