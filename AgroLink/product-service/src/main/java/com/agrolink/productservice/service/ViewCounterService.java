package com.agrolink.productservice.service;

import com.agrolink.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 浏览量计数器服务
 * <p>
 * 使用 Redis 实现高性能的浏览量计数。
 * 核心策略：先累加计数到 Redis 中，再通过定时任务（每 60 秒）
 * 将增量批量同步到数据库，避免频繁写库带来的性能开销。
 */
@Service
@RequiredArgsConstructor
public class ViewCounterService {

    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;

    /** Redis 中浏览量 key 的前缀 */
    private static final String VIEW_PREFIX = "agrolink:views:";

    /** 热门产品 ZSET 排名 key */
    private static final String HOT_RANKING_KEY = "agrolink:products:hot:ranking";

    /**
     * 增加产品浏览量
     * <p>
     * 将浏览量的增量先记录在 Redis 中，通过 INCR 命令原子递增，
     * 同时更新热门产品排名（浏览量权重为 1）。
     *
     * @param productId 产品 ID
     */
    public void increment(Long productId) {
        redisTemplate.opsForValue().increment(VIEW_PREFIX + productId);
        redisTemplate.opsForZSet().incrementScore(HOT_RANKING_KEY, String.valueOf(productId), 1);
    }

    /**
     * 定时将 Redis 中的浏览量批量同步到数据库
     * <p>
     * 每 60 秒执行一次。遍历所有浏览量 key，将计数累加到数据库，
     * 并从 Redis 中扣除已同步的计数，确保不丢失数据。
     */
    @Scheduled(fixedRate = 60000) // 每 60 秒执行一次
    public void syncToDb() {
        // 获取所有浏览量 key
        Set<String> keys = redisTemplate.keys(VIEW_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                // 读取当前累计的浏览量
                String val = redisTemplate.opsForValue().get(key);
                if (val == null) continue;
                long count = Long.parseLong(val);
                if (count == 0) continue;

                // 解析出产品 ID，将计数更新到数据库
                Long productId = Long.parseLong(key.substring(VIEW_PREFIX.length()));
                productRepository.addViewCount(productId, (int) count);
                // 从 Redis 中扣除已同步的计数
                redisTemplate.opsForValue().decrement(key, count);
            } catch (Exception e) {
                // 跳过有问题的 key，避免影响其他 key 的同步
            }
        }
    }
}
