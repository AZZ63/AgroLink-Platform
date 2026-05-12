package com.agrolink.orderservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.orderservice.dto.CartDTO;
import com.agrolink.orderservice.dto.CartRequest;
import com.agrolink.orderservice.dto.CartUpdateRequest;
import com.agrolink.orderservice.entity.Cart;
import com.agrolink.orderservice.repository.CartRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务
 * 提供加入购物车、修改数量、删除、勾选结算等操作
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/product";

    /**
     * 加入购物车
     * 如果该商品已在购物车中，则增加数量
     */
    @Transactional
    public CartDTO add(Long userId, CartRequest request) {
        // 校验商品是否存在且可购买
        Map<String, Object> product = fetchProduct(request.getProductId());
        if (!"SUPPLY".equals(product.get("infoType"))) {
            throw new BusinessException(400, "只能将供应信息加入购物车");
        }
        if (!"LISTED".equals(product.get("status"))) {
            throw new BusinessException(400, "该商品已下架或已售");
        }
        if (product.get("userId").equals(userId)) {
            throw new BusinessException(400, "不能将自己的商品加入购物车");
        }

        // 检查是否已在购物车中
        Cart existing = cartRepository.findByUserAndProduct(userId, request.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartRepository.updateById(existing);
            return toDTO(existing, product);
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setProductName((String) product.get("name"));
        cart.setPrice(new java.math.BigDecimal(product.get("price").toString()));
        cart.setUnit((String) product.get("unit"));
        cart.setImage((String) product.get("image"));
        cart.setInfoType((String) product.get("infoType"));
        cart.setQuantity(request.getQuantity());
        cart.setChecked(true);
        cartRepository.insert(cart);
        return toDTO(cart, product);
    }

    /**
     * 获取用户购物车列表
     */
    public List<CartDTO> list(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        return carts.stream().map(c -> {
            Map<String, Object> product;
            try {
                product = fetchProduct(c.getProductId());
            } catch (Exception e) {
                product = Map.of();
            }
            return toDTO(c, product);
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户选中的购物车项（用于结算）
     */
    public List<CartDTO> getChecked(Long userId) {
        List<Cart> carts = cartRepository.findCheckedByUserId(userId);
        return carts.stream().map(c -> {
            Map<String, Object> product = fetchProduct(c.getProductId());
            return toDTO(c, product);
        }).collect(Collectors.toList());
    }

    /**
     * 获取购物车中选中的商品数量
     */
    public long getCheckedCount(Long userId) {
        return cartRepository.findCheckedByUserId(userId).size();
    }

    /**
     * 修改购物车项数量或选中状态
     */
    @Transactional
    public CartDTO update(Long userId, Long cartId, CartUpdateRequest request) {
        Cart cart = cartRepository.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(404, "购物车项不存在");
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) throw new BusinessException(400, "数量必须大于0");
            cart.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            cart.setChecked(request.getChecked());
        }
        cartRepository.updateById(cart);
        Map<String, Object> product = fetchProduct(cart.getProductId());
        return toDTO(cart, product);
    }

    /**
     * 全选/取消全选
     */
    @Transactional
    public void toggleAll(Long userId, Boolean checked) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        for (Cart c : carts) {
            c.setChecked(checked);
            cartRepository.updateById(c);
        }
    }

    /**
     * 删除购物车项
     */
    @Transactional
    public void remove(Long userId, Long cartId) {
        Cart cart = cartRepository.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(404, "购物车项不存在");
        }
        cartRepository.deleteById(cartId);
    }

    /**
     * 批量删除
     */
    @Transactional
    public void removeBatch(Long userId, List<Long> cartIds) {
        for (Long id : cartIds) {
            remove(userId, id);
        }
    }

    /**
     * 清空用户购物车
     */
    @Transactional
    public void clear(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        for (Cart c : carts) {
            cartRepository.deleteById(c.getId());
        }
    }

    /**
     * 从产品服务获取产品信息
     */
    private Map<String, Object> fetchProduct(Long productId) {
        String json = restTemplate.getForObject(
            PRODUCT_SERVICE_URL + "/" + productId, String.class);
        if (json == null) {
            throw new BusinessException(404, "产品不存在");
        }
        try {
            com.agrolink.common.result.Result<Map<String, Object>> result = objectMapper.readValue(json,
                new TypeReference<com.agrolink.common.result.Result<Map<String, Object>>>() {});
            if (!result.isSuccess() || result.getData() == null) {
                throw new BusinessException(404, "产品不存在");
            }
            return result.getData();
        } catch (Exception e) {
            throw new BusinessException(400, "获取产品信息失败");
        }
    }

    /**
     * 实体转 DTO，补充商品实时状态
     */
    private CartDTO toDTO(Cart cart, Map<String, Object> product) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setProductId(cart.getProductId());
        dto.setProductName(cart.getProductName());
        dto.setPrice(cart.getPrice());
        dto.setUnit(cart.getUnit());
        dto.setImage(cart.getImage());
        dto.setInfoType(cart.getInfoType());
        dto.setQuantity(cart.getQuantity());
        dto.setChecked(cart.getChecked());
        dto.setCreatedAt(cart.getCreatedAt());
        if (product != null && !product.isEmpty()) {
            dto.setProductStatus((String) product.get("status"));
            Object qty = product.get("quantity");
            dto.setProductQuantity(qty instanceof Number ? ((Number) qty).intValue() : 0);
        }
        return dto;
    }
}
