package com.agrolink.productservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.productservice.entity.Favorite;
import com.agrolink.productservice.entity.Product;
import com.agrolink.productservice.repository.FavoriteRepository;
import com.agrolink.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务
 * <p>
 * 提供收藏/取消收藏切换、收藏列表查询、收藏数统计以及检查收藏状态等功能。
 * 使用 @Transactional 保证收藏切换操作的原子性。
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    /**
     * 切换收藏状态
     * <p>
     * 如果用户已收藏该产品则取消收藏，否则添加收藏。
     * 返回操作后的收藏状态和最新的收藏数量。
     *
     * @param userId    用户 ID
     * @param productId 产品 ID
     * @return 包含 favorited（当前收藏状态）和 count（收藏数）的 Map
     * @throws BusinessException 如果产品不存在则抛出 404 异常
     */
    @Transactional
    public Map<String, Object> toggle(Long userId, Long productId) {
        // 校验产品是否存在
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "产品不存在");
        }

        // 查询是否已收藏
        Favorite existing = favoriteRepository.findByUserAndProduct(userId, productId);
        if (existing != null) {
            // 已收藏 -> 取消收藏
            favoriteRepository.deleteById(existing.getId());
            return Map.of("favorited", false, "count", favoriteRepository.countByProductId(productId));
        }

        // 未收藏 -> 添加收藏
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        favoriteRepository.insert(fav);

        return Map.of("favorited", true, "count", favoriteRepository.countByProductId(productId));
    }

    /**
     * 获取指定用户的所有收藏记录
     *
     * @param userId 用户 ID
     * @return 收藏列表
     */
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    /**
     * 获取指定用户收藏的所有产品 ID
     *
     * @param userId 用户 ID
     * @return 产品 ID 列表
     */
    public List<Long> getUserFavoriteProductIds(Long userId) {
        return favoriteRepository.findProductIdsByUserId(userId);
    }

    /**
     * 获取指定产品的收藏数
     *
     * @param productId 产品 ID
     * @return 收藏数
     */
    public int getFavoriteCount(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }

    /**
     * 检查指定用户是否已收藏指定产品
     *
     * @param userId    用户 ID
     * @param productId 产品 ID
     * @return true 表示已收藏
     */
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.exists(userId, productId);
    }
}
