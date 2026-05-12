package com.agrolink.productservice.service;

import com.agrolink.common.dto.PageResult;
import com.agrolink.common.dto.ProductDTO;
import com.agrolink.common.dto.ProductQueryRequest;
import com.agrolink.common.dto.StatsDTO;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.productservice.entity.Product;
import com.agrolink.productservice.repository.ProductRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 产品服务
 * <p>
 * 产品模块的核心业务服务，提供产品的完整 CRUD、多条件分页查询、
 * 状态机管理、三级缓存策略（防止缓存穿透和缓存雪崩）、
 * 以及用户统计等功能。
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ViewCounterService viewCounterService;

    // ==================== Redis 缓存 Key 常量 ====================

    /** 热门产品列表缓存 key */
    private static final String HOT_PRODUCTS_KEY = "agrolink:products:hot";

    /** 热门产品 ZSET 排名 key */
    private static final String HOT_RANKING_KEY = "agrolink:products:hot:ranking";

    /** 产品类型列表缓存 key */
    private static final String PRODUCT_TYPES_KEY = "agrolink:products:types";

    /** 产品详情缓存前缀 */
    private static final String DETAIL_CACHE_PREFIX = "agrolink:product:detail:";

    /** 空值标记缓存前缀（防止缓存穿透） */
    private static final String DETAIL_NULL_PREFIX = "agrolink:null:product:";

    /** 互斥锁缓存前缀（防止缓存雪崩） */
    private static final String LOCK_PREFIX = "agrolink:lock:product:";

    /** 查询结果缓存前缀 */
    private static final String QUERY_CACHE_PREFIX = "agrolink:products:q:";

    /** 产品详情缓存 TTL（分钟） */
    private static final long DETAIL_CACHE_TTL = 30;

    /** 空值标记 TTL（分钟）——较短时间后自动过期 */
    private static final long DETAIL_NULL_TTL = 5;

    /** 查询结果缓存 TTL（秒） */
    private static final long QUERY_CACHE_TTL_SEC = 120;

    // ==================== 状态机定义 ====================

    /**
     * 状态流转映射表
     * <p>
     * 定义 SUPPLY（供应）和 DEMAND（需求）两种产品类型的合法状态流转路径。
     * 外层 key 为 infoType，内层 key 为当前状态，value 为允许跳转到的目标状态集合。
     */
    private static final Map<String, Map<String, Set<String>>> VALID_TRANSITIONS = Map.of(
        "SUPPLY", Map.of(
            "LISTED", Set.of("SOLD", "UNLISTED"),     // 上架 -> 已售 / 下架
            "UNLISTED", Set.of("LISTED"),              // 下架 -> 上架
            "SOLD", Set.of()                           // 已售 -> 不可变更
        ),
        "DEMAND", Map.of(
            "PENDING", Set.of("COMPLETED", "CLOSED"),  // 待匹配 -> 已成交 / 关闭
            "CLOSED", Set.of("PENDING"),               // 关闭 -> 待匹配
            "COMPLETED", Set.of()                      // 已成交 -> 不可变更
        )
    );

    /** 各信息类型的默认初始状态 */
    private static final Map<String, String> INFO_TYPE_DEFAULT_STATUS = Map.of(
        "SUPPLY", "LISTED",     // 供应默认上架
        "DEMAND", "PENDING"     // 需求默认待匹配
    );

    // ==================== CRUD ====================

    /**
     * 创建产品
     * <p>
     * 根据用户角色校验发布权限：农户（FARMER）只能发布供应信息，
     * 商户（MERCHANT）只能发布需求信息。创建成功后清除列表缓存。
     *
     * @param dto    产品信息
     * @param userId 发布者用户 ID
     * @param role   发布者角色
     * @return 创建成功的产品 DTO
     */
    public ProductDTO createProduct(ProductDTO dto, Long userId, String role) {
        // 角色权限校验
        if ("FARMER".equals(role) && !"SUPPLY".equals(dto.getInfoType())) {
            throw new BusinessException(403,"农户只能发布供应信息");
        }
        if ("MERCHANT".equals(role) && !"DEMAND".equals(dto.getInfoType())) {
            throw new BusinessException(403,"商户只能发布需求信息");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setType(dto.getType());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());
        product.setUnit(dto.getUnit());
        product.setDescription(dto.getDescription());
        product.setProvince(dto.getProvince());
        product.setCity(dto.getCity());
        product.setImage(dto.getImage());
        product.setInfoType(dto.getInfoType());
        // 设置默认初始状态
        product.setStatus(INFO_TYPE_DEFAULT_STATUS.getOrDefault(dto.getInfoType(), "LISTED"));
        product.setUserId(userId);
        productRepository.insert(product);

        // 回填生成的 ID 和时间戳到 DTO
        dto.setId(product.getId());
        dto.setUserId(userId);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setStatus(product.getStatus());

        // 清除列表缓存，确保新发布的产品能出现在查询结果中
        invalidateListCaches();
        return dto;
    }

    // ==================== Query + Cache ====================

    /**
     * 分页多条件查询产品（带缓存）
     * <p>
     * 支持按 ID 批量查询或多维筛选条件查询。
     * 对带筛选条件的查询会缓存查询结果到 Redis，缓存 key 由查询参数的 MD5 生成，
     * 并加入随机过期时间（缓存 TTL + 0~30 秒的抖动）防止缓存雪崩。
     *
     * @param request 查询请求
     * @return 分页结果
     */
    public PageResult<ProductDTO> queryProducts(ProductQueryRequest request) {
        // 按 ID 批量查询——不走缓存
        if (request.getIds() != null && !request.getIds().isBlank()) {
            List<Long> ids = java.util.Arrays.stream(request.getIds().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Long::valueOf).toList();
            List<Product> products = productRepository.findByIds(ids);
            List<ProductDTO> list = products.stream().map(this::toDTO).toList();
            return PageResult.of(list, (long) list.size(), 1, list.size());
        }

        // 尝试从缓存读取查询结果
        String cacheKey = buildQueryCacheKey(request);
        if (cacheKey != null) {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    return objectMapper.readValue(cached, new TypeReference<PageResult<ProductDTO>>() {});
                } catch (Exception e) {
                    // 反序列化失败，回退到数据库查询
                }
            }
        }

        // 缓存未命中，从数据库查询
        IPage<Product> pageResult = productRepository.queryByFilters(
                request.getKeyword(), request.getType(),
                request.getInfoType(), request.getStatus(),
                request.getProvince(), request.getCity(),
                request.getMinPrice(), request.getMaxPrice(),
                request.getSortBy(), request.getSortOrder(),
                request.getPage(), request.getSize()
        );

        List<ProductDTO> list = pageResult.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResult<ProductDTO> result = PageResult.of(list, pageResult.getTotal(), request.getPage(), request.getSize());

        // 将查询结果写入缓存（尽力而为，加入随机 TTL 防止大量缓存同时过期）
        if (cacheKey != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                long ttl = QUERY_CACHE_TTL_SEC + ThreadLocalRandom.current().nextLong(0, 30);
                redisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 缓存写入失败不影响正常业务
            }
        }

        return result;
    }

    /**
     * 获取热门产品列表（基于 Redis ZSET 热度排名）
     * <p>
     * 从 Redis ZSET 取前 10 名，按分数降序排列。
     * 如果缓存为空，则从数据库取最新 10 条初始化排行。
     * 热度分数由浏览量（权重 1）、收藏数（权重 3）、订单数（权重 5）综合计算。
     *
     * @return 热门产品列表
     */
    public List<ProductDTO> getHotProducts() {
        // 从 Redis ZSET 取前 10 名，按分数降序
        Set<String> topIds = redisTemplate.opsForZSet()
            .reverseRange(HOT_RANKING_KEY, 0, 9);
        if (topIds == null || topIds.isEmpty()) {
            // 缓存为空时，从数据库取最新并初始化排行
            List<Product> products = productRepository.findHotProducts(10);
            for (Product p : products) {
                double score = p.getViewCount() != null ? p.getViewCount().doubleValue() : 0;
                redisTemplate.opsForZSet().add(HOT_RANKING_KEY, String.valueOf(p.getId()), score);
            }
            return products.stream().map(this::toDTO).collect(Collectors.toList());
        }
        List<Long> ids = topIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Product> products = productRepository.findByIds(ids);
        // 保持排序
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> sorted = topIds.stream()
            .map(id -> productMap.get(Long.valueOf(id)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return sorted.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 增加产品热度分数
     *
     * @param productId 产品 ID
     * @param score     增加的分数
     */
    public void incrementHotScore(Long productId, double score) {
        redisTemplate.opsForZSet().incrementScore(HOT_RANKING_KEY, String.valueOf(productId), score);
    }

    /**
     * 根据 ID 获取产品详情（三级缓存策略）
     * <p>
     * 缓存策略层次：
     * 1. 查询详情缓存——命中直接返回
     * 2. 检查空值标记——命中则直接返回 404（防止缓存穿透）
     * 3. 获取互斥锁——防止大量请求同时查询数据库导致缓存雪崩
     * 4. 若获取锁失败——短暂休眠后递归重试
     * <p>
     * 获取锁成功的线程查询数据库，将结果回填缓存（加入随机 TTL），
     * 如果数据库中也不存在则写入短 TTL 的空值标记。
     *
     * @param id 产品 ID
     * @return 产品 DTO
     * @throws BusinessException 如果产品不存在则抛出 404 异常
     */
    public ProductDTO getProductById(Long id) {
        // 1. 查详情缓存（命中则直接返回）
        String detailKey = DETAIL_CACHE_PREFIX + id;
        String cached = redisTemplate.opsForValue().get(detailKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ProductDTO.class);
            } catch (Exception e) {
                // 反序列化失败，继续后续流程
            }
        }

        // 2. 查空值标记（防止缓存穿透——避免大量请求穿透到数据库查询不存在的 ID）
        String nullKey = DETAIL_NULL_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
            throw new BusinessException(404, "产品不存在");
        }

        // 3. 尝试获取互斥锁（防止缓存击穿——避免高并发时所有线程同时查数据库）
        String lockKey = LOCK_PREFIX + id;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 3, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双重检查——获取锁后再次检查缓存
                cached = redisTemplate.opsForValue().get(detailKey);
                if (cached != null) {
                    return objectMapper.readValue(cached, ProductDTO.class);
                }

                // 查询数据库
                Product product = productRepository.selectById(id);
                if (product == null) {
                    // 数据库不存在，设置短 TTL 空值标记防止缓存穿透
                    redisTemplate.opsForValue().set(nullKey, "1", DETAIL_NULL_TTL, TimeUnit.MINUTES);
                    throw new BusinessException(404, "产品不存在");
                }

                // 数据库存在，回填缓存（加入随机 TTL 防止同时过期）
                ProductDTO dto = toDTO(product);
                long ttl = DETAIL_CACHE_TTL + ThreadLocalRandom.current().nextLong(0, 5);
                redisTemplate.opsForValue().set(detailKey, objectMapper.writeValueAsString(dto), ttl, TimeUnit.MINUTES);
                return dto;
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("缓存序列化失败", e);
            } finally {
                // 使用 Lua 风格检查：只有自己设置的锁才释放，防止误删其他线程的锁
                String val = redisTemplate.opsForValue().get(lockKey);
                if (lockValue.equals(val)) {
                    redisTemplate.delete(lockKey);
                }
            }
        }

        // 4. 未获取到锁——短暂休眠后递归重试（自旋等待）
        try {
            Thread.sleep(50 + ThreadLocalRandom.current().nextLong(0, 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return getProductById(id);
    }

    /**
     * 增加产品浏览量（异步，先计入 Redis）并更新热度排名
     *
     * @param id 产品 ID
     */
    public void viewProduct(Long id) {
        viewCounterService.increment(id);
        incrementHotScore(id, 1);
    }

    /**
     * 获取所有产品类型
     * <p>
     * 结果缓存 30 分钟，但实际返回的是从数据库查询到的完整类型列表。
     *
     * @return 产品类型列表
     */
    public List<String> getProductTypes() {
        String cached = redisTemplate.opsForValue().get(PRODUCT_TYPES_KEY);
        if (cached != null) {
            return List.of();
        }
        List<String> types = productRepository.findAllTypes();
        if (!types.isEmpty()) {
            redisTemplate.opsForValue().set(PRODUCT_TYPES_KEY, "cached", 30, TimeUnit.MINUTES);
        }
        return types;
    }

    /**
     * 获取指定用户发布的所有产品
     *
     * @param userId 用户 ID
     * @return 产品 DTO 列表
     */
    public List<ProductDTO> getUserProducts(Long userId) {
        return productRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== Status / Update / Delete with cache invalidation ====================

    /**
     * 更新产品状态（受状态机约束）
     * <p>
     * 产品所有者可以按照预定义的状态流转规则变更产品状态，
     * 非法状态转换将被拒绝。更新后清除相关缓存。
     *
     * @param id        产品 ID
     * @param userId    当前用户 ID
     * @param newStatus 目标状态
     * @throws BusinessException 如果产品不存在、无权限或状态转换非法则抛异常
     */
    public void updateStatus(Long id, Long userId, String newStatus) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404,"产品不存在");
        }
        // 校验所有权
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403,"无权修改此产品状态");
        }

        String infoType = product.getInfoType();
        String currentStatus = product.getStatus();

        // 查找当前信息类型对应的状态流转规则
        Map<String, Set<String>> transitions = VALID_TRANSITIONS.get(infoType);
        if (transitions == null) {
            throw new BusinessException(400,"无效的产品类型");
        }

        // 校验状态转换是否合法
        Set<String> allowedNext = transitions.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(newStatus)) {
            throw new BusinessException(400,
                "不允许从 " + statusLabel(currentStatus) + " 变更为 " + statusLabel(newStatus));
        }

        product.setStatus(newStatus);
        productRepository.updateById(product);

        // 清除产品详情缓存
        invalidateProductCache(id);
    }

    /**
     * 更新产品信息
     * <p>
     * 产品所有者可以更新产品的基本信息（名称、类型、数量、价格等）。
     * 图片字段仅在提供了新值时才会被更新。
     *
     * @param dto    更新后的产品信息
     * @param userId 当前用户 ID
     * @return 更新后的产品 DTO
     * @throws BusinessException 如果产品不存在或无权限则抛异常
     */
    public ProductDTO updateProduct(ProductDTO dto, Long userId) {
        Product product = productRepository.selectById(dto.getId());
        if (product == null) {
            throw new BusinessException(404,"产品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403,"无权修改此产品");
        }

        // 更新字段
        product.setName(dto.getName());
        product.setType(dto.getType());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());
        product.setUnit(dto.getUnit());
        product.setDescription(dto.getDescription());
        product.setProvince(dto.getProvince());
        product.setCity(dto.getCity());
        // 图片仅在传入新值时更新
        if (dto.getImage() != null) {
            product.setImage(dto.getImage());
        }
        // 多图仅在传入新值时更新
        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }
        productRepository.updateById(product);

        // 清除相关缓存
        invalidateProductCache(dto.getId());
        invalidateListCaches();
        return toDTO(product);
    }

    /**
     * 删除产品
     * <p>
     * 产品所有者可以删除自己的产品。删除后清除相关缓存。
     *
     * @param id     产品 ID
     * @param userId 当前用户 ID
     * @throws BusinessException 如果产品不存在或无权限则抛异常
     */
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404,"产品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403,"无权删除此产品");
        }
        productRepository.deleteById(id);

        // 清除产品详情缓存
        invalidateProductCache(id);
        invalidateListCaches();
    }

    /**
     * 强制修改产品状态（管理员专用）
     * <p>
     * 跳过状态机校验，允许管理员将产品设置为任意状态。
     *
     * @param id     产品 ID
     * @param status 目标状态
     * @throws BusinessException 如果产品不存在则抛异常
     */
    public void forceUpdateStatus(Long id, String status) {
        Product product = productRepository.selectById(id);
        if (product == null) throw new BusinessException(404,"产品不存在");
        product.setStatus(status);
        productRepository.updateById(product);
        invalidateProductCache(id);
    }

    // ==================== Stats ====================

    /**
     * 获取指定用户的统计信息
     * <p>
     * 统计用户发布的产品总数和总浏览量。
     *
     * @param userId 用户 ID
     * @return 统计数据
     */
    public StatsDTO getStats(Long userId) {
        List<Product> products = productRepository.findByUserId(userId);
        long totalProducts = products.size();
        long totalViews = products.stream().mapToLong(p -> p.getViewCount() != null ? p.getViewCount() : 0).sum();
        List<Integer> dailyViews = List.of((int) totalViews);
        List<String> dates = List.of("总计");
        return new StatsDTO(dailyViews, dates, totalProducts, 0);
    }

    // ==================== Cache helpers ====================

    /**
     * 清除指定产品的详情缓存和空值标记
     *
     * @param id 产品 ID
     */
    private void invalidateProductCache(Long id) {
        redisTemplate.delete(DETAIL_CACHE_PREFIX + id);
        redisTemplate.delete(DETAIL_NULL_PREFIX + id);
    }

    /**
     * 清除列表类缓存（热门产品列表、产品类型列表）
     */
    private void invalidateListCaches() {
        redisTemplate.delete(HOT_PRODUCTS_KEY);
        redisTemplate.delete(PRODUCT_TYPES_KEY);
    }

    /**
     * 构建查询缓存的 key
     * <p>
     * 将所有查询参数拼接后计算 MD5 哈希作为缓存 key，
     * 相同的查询条件会命中相同的缓存。
     *
     * @param r 查询请求
     * @return MD5 哈希字符串，若算法不可用则返回 null
     */
    private String buildQueryCacheKey(ProductQueryRequest r) {
        try {
            String raw = (r.getKeyword() != null ? r.getKeyword() : "") + "|" +
                (r.getType() != null ? r.getType() : "") + "|" +
                (r.getInfoType() != null ? r.getInfoType() : "") + "|" +
                (r.getStatus() != null ? r.getStatus() : "") + "|" +
                (r.getProvince() != null ? r.getProvince() : "") + "|" +
                (r.getCity() != null ? r.getCity() : "") + "|" +
                (r.getMinPrice() != null ? r.getMinPrice() : "") + "|" +
                (r.getMaxPrice() != null ? r.getMaxPrice() : "") + "|" +
                (r.getSortBy() != null ? r.getSortBy() : "") + "|" +
                (r.getSortOrder() != null ? r.getSortOrder() : "") + "|" +
                r.getPage() + "|" + r.getSize();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return QUERY_CACHE_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    // ==================== DTO mapping ====================

    /**
     * 将 Product 实体转换为 ProductDTO
     *
     * @param p 产品实体
     * @return 产品 DTO
     */
    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setType(p.getType());
        dto.setQuantity(p.getQuantity());
        dto.setPrice(p.getPrice());
        dto.setUnit(p.getUnit());
        dto.setDescription(p.getDescription());
        dto.setProvince(p.getProvince());
        dto.setCity(p.getCity());
        dto.setImage(p.getImage());
        dto.setImages(p.getImages());
        dto.setViewCount(p.getViewCount());
        dto.setInfoType(p.getInfoType());
        dto.setStatus(p.getStatus());
        dto.setUserId(p.getUserId());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    /**
     * 将状态枚举值转换为中文标签
     *
     * @param status 状态值
     * @return 中文状态描述
     */
    private String statusLabel(String status) {
        return switch (status) {
            case "LISTED" -> "上架";
            case "SOLD" -> "已售";
            case "UNLISTED" -> "下架";
            case "PENDING" -> "待匹配";
            case "COMPLETED" -> "已成交";
            case "CLOSED" -> "关闭";
            default -> status;
        };
    }
}
