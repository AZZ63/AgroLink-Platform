package com.agrolink.productservice.service;

import com.agrolink.common.dto.PageResult;
import com.agrolink.common.dto.ProductDTO;
import com.agrolink.common.dto.ProductQueryRequest;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.productservice.entity.Product;
import com.agrolink.productservice.repository.ProductRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProductService 单元测试
 * <p>
 * 使用 Mockito 框架对 ProductService 的核心业务逻辑进行单元测试，
 * 覆盖产品的创建、查询、热门产品、状态更新、产品更新和删除等场景。
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    /** 模拟 ProductRepository */
    @Mock ProductRepository productRepository;

    /** 模拟 Redis 模板 */
    @Mock StringRedisTemplate redisTemplate;

    /** 模拟 Jackson ObjectMapper */
    @Mock ObjectMapper objectMapper;

    /** 模拟浏览量计数器服务 */
    @Mock ViewCounterService viewCounterService;

    /** 注入被测试的 ProductService */
    @InjectMocks ProductService productService;

    /** 用于捕获插入/更新的 Product 实体参数 */
    @Captor ArgumentCaptor<Product> productCaptor;

    /** 测试用：农户 ID */
    Long farmerId = 1L;

    /** 测试用：商户 ID */
    Long merchantId = 2L;

    /** 测试用：产品 ID */
    Long productId = 100L;

    /**
     * 构建测试用的 Product 实体
     *
     * @param id       产品 ID
     * @param infoType 信息类型（SUPPLY / DEMAND）
     * @param status   产品状态
     * @param userId   发布者用户 ID
     * @return Product 实体
     */
    Product buildProduct(Long id, String infoType, String status, Long userId) {
        Product p = new Product();
        p.setId(id);
        p.setName("有机大白菜");
        p.setType("蔬菜");
        p.setQuantity(100);
        p.setPrice(new BigDecimal("5.00"));
        p.setUnit("斤");
        p.setInfoType(infoType);
        p.setStatus(status);
        p.setUserId(userId);
        p.setProvince("山东");
        p.setCity("寿光");
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    /**
     * 构建测试用的 ProductDTO
     *
     * @param infoType 信息类型
     * @return ProductDTO
     */
    ProductDTO buildProductDTO(String infoType) {
        ProductDTO dto = new ProductDTO();
        dto.setName("有机大白菜");
        dto.setType("蔬菜");
        dto.setQuantity(100);
        dto.setPrice(new BigDecimal("5.00"));
        dto.setUnit("斤");
        dto.setInfoType(infoType);
        dto.setProvince("山东");
        dto.setCity("寿光");
        return dto;
    }

    // ======================== createProduct 测试 ========================

    @Nested
    class CreateProduct {

        /** 农户创建供应信息应成功 */
        @Test
        void farmerCreatesSupply() {
            ProductDTO dto = productService.createProduct(
                buildProductDTO("SUPPLY"), farmerId, "FARMER");
            verify(productRepository).insert(productCaptor.capture());
            Product saved = productCaptor.getValue();
            assertEquals("有机大白菜", saved.getName());
            assertEquals("SUPPLY", saved.getInfoType());
            assertEquals("LISTED", saved.getStatus());
            assertEquals(farmerId, saved.getUserId());
            // 验证缓存被清除
            verify(redisTemplate).delete(contains("agrolink:products:hot"));
        }

        /** 商户创建需求信息应成功 */
        @Test
        void merchantCreatesDemand() {
            ProductDTO dto = productService.createProduct(
                buildProductDTO("DEMAND"), merchantId, "MERCHANT");
            verify(productRepository).insert(productCaptor.capture());
            assertEquals("DEMAND", productCaptor.getValue().getInfoType());
            assertEquals("PENDING", productCaptor.getValue().getStatus());
        }

        /** 农户发布需求信息应被拒绝 */
        @Test
        void forbidden_farmerCreatesDemand() {
            var ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(buildProductDTO("DEMAND"), farmerId, "FARMER"));
            assertEquals("农户只能发布供应信息", ex.getMessage());
        }

        /** 商户发布供应信息应被拒绝 */
        @Test
        void forbidden_merchantCreatesSupply() {
            var ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(buildProductDTO("SUPPLY"), merchantId, "MERCHANT"));
            assertEquals("商户只能发布需求信息", ex.getMessage());
        }
    }

    // ======================== 产品查询测试 ========================

    @Nested
    class QueryProducts {

        /** 按条件分页查询成功 */
        @Test
        void success() {
            ProductQueryRequest request = new ProductQueryRequest();
            request.setPage(1);
            request.setSize(10);

            IPage<Product> mockPage = mock(IPage.class);
            when(mockPage.getRecords()).thenReturn(
                List.of(buildProduct(productId, "SUPPLY", "LISTED", farmerId)));
            when(mockPage.getTotal()).thenReturn(1L);
            when(productRepository.queryByFilters(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

            PageResult<ProductDTO> result = productService.queryProducts(request);
            assertEquals(1, result.getTotal());
            assertEquals("有机大白菜", result.getRecords().get(0).getName());
        }
    }

    // ======================== 热门产品测试 ========================

    @Nested
    class GetHotProducts {

        /** 缓存命中时从缓存返回 */
        @Test
        void returnsFromCache_whenAvailable() throws Exception {
            ValueOperations<String, String> ops = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(ops);
            when(ops.get("agrolink:products:hot")).thenReturn("[{\"name\":\"有机大白菜\"}]");
            when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of(new ProductDTO()));

            List<ProductDTO> result = productService.getHotProducts();
            assertEquals(1, result.size());
            verify(productRepository, never()).findHotProducts(anyInt());
        }

        /** 缓存未命中时从数据库查询并回填缓存 */
        @Test
        void queriesDb_whenCacheMiss() throws Exception {
            ValueOperations<String, String> ops = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(ops);
            when(ops.get("agrolink:products:hot")).thenReturn(null);
            when(productRepository.findHotProducts(10))
                .thenReturn(List.of(buildProduct(productId, "SUPPLY", "LISTED", farmerId)));

            List<ProductDTO> result = productService.getHotProducts();
            assertEquals(1, result.size());
            verify(ops).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        }
    }

    // ======================== 状态更新测试 ========================

    @Nested
    class UpdateStatus {

        /** 上架 -> 下架 合法转换 */
        @Test
        void listToUnlist() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));
            productService.updateStatus(productId, farmerId, "UNLISTED");
            verify(productRepository).updateById(productCaptor.capture());
            assertEquals("UNLISTED", productCaptor.getValue().getStatus());
        }

        /** 下架 -> 上架 合法转换 */
        @Test
        void unlistToList() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "UNLISTED", farmerId));
            productService.updateStatus(productId, farmerId, "LISTED");
            verify(productRepository).updateById(productCaptor.capture());
            assertEquals("LISTED", productCaptor.getValue().getStatus());
        }

        /** 非产品所有者操作应被拒绝 */
        @Test
        void forbidden_whenNotOwner() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));
            var ex = assertThrows(BusinessException.class,
                () -> productService.updateStatus(productId, 99L, "UNLISTED"));
            assertEquals("无权修改此产品状态", ex.getMessage());
        }

        /** 非法状态转换应被拒绝 */
        @Test
        void badRequest_invalidTransition() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "SOLD", farmerId));
            var ex = assertThrows(BusinessException.class,
                () -> productService.updateStatus(productId, farmerId, "LISTED"));
            assertTrue(ex.getMessage().contains("不允许"));
        }
    }

    // ======================== 产品更新测试 ========================

    @Nested
    class UpdateProduct {

        /** 更新产品信息成功 */
        @Test
        void success() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));

            ProductDTO update = new ProductDTO();
            update.setId(productId);
            update.setName("有机大白菜（精品）");
            update.setType("蔬菜");
            update.setQuantity(50);
            update.setPrice(new BigDecimal("8.00"));
            update.setUnit("斤");

            ProductDTO result = productService.updateProduct(update, farmerId);
            assertEquals("有机大白菜（精品）", result.getName());

            verify(productRepository).updateById(productCaptor.capture());
            assertEquals("有机大白菜（精品）", productCaptor.getValue().getName());
            assertEquals(new BigDecimal("8.00"), productCaptor.getValue().getPrice());
        }

        /** 非产品所有者更新应被拒绝 */
        @Test
        void forbidden_whenNotOwner() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));

            ProductDTO update = new ProductDTO();
            update.setId(productId);

            var ex = assertThrows(BusinessException.class,
                () -> productService.updateProduct(update, 99L));
            assertEquals("无权修改此产品", ex.getMessage());
        }
    }

    // ======================== 产品删除测试 ========================

    @Nested
    class DeleteProduct {

        /** 产品所有者删除成功 */
        @Test
        void success() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));
            productService.deleteProduct(productId, farmerId);
            verify(productRepository).deleteById(productId);
        }

        /** 非产品所有者删除应被拒绝 */
        @Test
        void forbidden_whenNotOwner() {
            when(productRepository.selectById(productId))
                .thenReturn(buildProduct(productId, "SUPPLY", "LISTED", farmerId));
            var ex = assertThrows(BusinessException.class,
                () -> productService.deleteProduct(productId, 99L));
            assertEquals("无权删除此产品", ex.getMessage());
        }
    }
}
