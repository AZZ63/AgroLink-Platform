package com.agrolink.productservice.controller;

import com.agrolink.common.dto.PageResult;
import com.agrolink.common.dto.ProductDTO;
import com.agrolink.common.result.Result;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员产品管理控制器
 * <p>
 * 提供管理员专属的产品管理接口，包括分页查询所有产品和强制修改产品状态。
 * 所有接口需要 ADMIN 角色权限。
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /**
     * 管理员分页查询产品列表
     *
     * @param page     页码，默认第 1 页
     * @param size     每页大小，默认 20
     * @param infoType 信息类型筛选（SUPPLY / DEMAND）
     * @param status   状态筛选
     * @param role     请求头中的角色（必须为 ADMIN）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<ProductDTO>> listProducts(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String infoType,
                                                       @RequestParam(required = false) String status,
                                                       @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        checkAdmin(role);
        var req = new com.agrolink.common.dto.ProductQueryRequest();
        req.setPage(page);
        req.setSize(size);
        req.setInfoType(infoType);
        req.setStatus(status);
        return Result.success(productService.queryProducts(req));
    }

    /**
     * 管理员强制修改产品状态
     *
     * @param id   产品 ID
     * @param body 请求体，包含新的 status
     * @param role 请求头中的角色（必须为 ADMIN）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        checkAdmin(role);
        productService.forceUpdateStatus(id, body.get("status"));
        return Result.success();
    }

    /**
     * 校验当前用户是否为管理员
     *
     * @param role 用户角色
     * @throws BusinessException 如果不是管理员则抛出 403 异常
     */
    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new BusinessException(403, "仅管理员可操作");
    }
}
