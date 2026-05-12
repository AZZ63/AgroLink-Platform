package com.agrolink.productservice.controller;

import com.agrolink.common.dto.PageResult;
import com.agrolink.common.dto.ProductDTO;
import com.agrolink.common.dto.ProductQueryRequest;
import com.agrolink.common.dto.StatsDTO;
import com.agrolink.common.result.Result;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品控制器
 * <p>
 * 提供产品的增删改查、分页查询、热门产品、产品类型获取、
 * 浏览量记录、用户统计等接口。是产品服务的核心控制器。
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建产品（供应或需求信息）
     *
     * @param request 产品信息
     * @param userId  当前用户 ID（从请求头获取）
     * @param role    当前用户角色（FARMER 只能发供应，MERCHANT 只能发需求）
     * @return 创建成功的产品 DTO
     */
    @PostMapping("/create")
    public Result<ProductDTO> createProduct(@Valid @RequestBody ProductDTO request,
                                            @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId,
                                            @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        return Result.success(productService.createProduct(request, userId, role));
    }

    /**
     * 分页条件查询产品
     * <p>
     * 支持按关键词、类型、信息类型、状态、地区、价格范围等多维筛选，
     * 并支持排序。查询结果会被缓存到 Redis。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    public Result<PageResult<ProductDTO>> queryProducts(@RequestBody ProductQueryRequest request) {
        return Result.success(productService.queryProducts(request));
    }

    /**
     * 获取热门产品列表
     *
     * @return 产品列表
     */
    @GetMapping("/hot")
    public Result<List<ProductDTO>> getHotProducts() {
        return Result.success(productService.getHotProducts());
    }

    /**
     * 根据 ID 获取产品详情
     * <p>
     * 使用三级缓存策略（缓存 -> 空值标记 -> 互斥锁）防止缓存穿透和缓存雪崩。
     *
     * @param id 产品 ID
     * @return 产品 DTO
     */
    @GetMapping("/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    /**
     * 增加产品浏览量
     * <p>
     * 浏览量先计入 Redis，再定时批量同步到数据库。
     *
     * @param id 产品 ID
     */
    @PostMapping("/{id}/view")
    public Result<Void> viewProduct(@PathVariable Long id) {
        productService.viewProduct(id);
        return Result.success();
    }

    /**
     * 手动增加产品热度分数
     * <p>
     * 用于外部事件（如收藏、下单等）触发热度更新。
     * 适合在订单服务或用户服务中通过 Feign 调用。
     *
     * @param id    产品 ID
     * @param score 增加的分数（默认 1）
     * @return 操作结果
     */
    @PostMapping("/{id}/hot")
    public Result<Void> boostHot(@PathVariable Long id, @RequestParam(defaultValue = "1") double score) {
        productService.incrementHotScore(id, score);
        return Result.success();
    }

    /**
     * 获取所有产品类型列表
     *
     * @return 产品类型字符串列表
     */
    @GetMapping("/types")
    public Result<List<String>> getProductTypes() {
        return Result.success(productService.getProductTypes());
    }

    /**
     * 获取当前用户发布的产品列表
     *
     * @param userId 当前用户 ID
     * @return 产品 DTO 列表
     */
    @GetMapping("/my")
    public Result<List<ProductDTO>> getMyProducts(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(productService.getUserProducts(userId));
    }

    /**
     * 删除产品
     *
     * @param id     产品 ID
     * @param userId 当前用户 ID（用于校验所有权）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id,
                                      @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        productService.deleteProduct(id, userId);
        return Result.success();
    }

    /**
     * 更新产品信息
     *
     * @param request 更新后的产品信息
     * @param userId  当前用户 ID（用于校验所有权）
     * @return 更新后的产品 DTO
     */
    @PutMapping("/update")
    public Result<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO request,
                                            @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(productService.updateProduct(request, userId));
    }

    /**
     * 获取当前用户的统计信息
     *
     * @param userId 当前用户 ID
     * @return 统计数据（产品数、总浏览量等）
     */
    @GetMapping("/stats")
    public Result<StatsDTO> getStats(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(productService.getStats(userId));
    }

    /**
     * 更新产品状态
     * <p>
     * 状态变更受状态机约束，只能按合法路径流转。
     *
     * @param id     产品 ID
     * @param status 目标状态
     * @param userId 当前用户 ID（用于校验所有权）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        productService.updateStatus(id, userId, status);
        return Result.success();
    }

}
