package com.agrolink.userservice.controller;

import com.agrolink.common.dto.LoginRequest;
import com.agrolink.common.dto.LoginResponse;
import com.agrolink.common.dto.RegisterRequest;
import com.agrolink.common.dto.UserInfo;
import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.result.Result;
import com.agrolink.common.util.JwtUtil;
import com.agrolink.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证与信息管理控制器
 * <p>
 * 提供注册、登录、Token 刷新、退出登录、用户信息查询及 Token 验证等接口。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 使用 Refresh Token 刷新 Access Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(400, "refreshToken 不能为空");
        }
        return Result.success(userService.refresh(refreshToken));
    }

    /**
     * 退出登录（将当前 Access Token 加入黑名单）
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        userService.logout(token);
        return Result.success();
    }

    /**
     * 获取当前登录用户的详细信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getUserInfo(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 校验 Token 有效性并返回对应用户信息
     */
    @PostMapping("/validate")
    public Result<UserInfo> validateToken(@RequestBody String token) {
        if (!JwtUtil.isTokenValid(token)) {
            throw new BusinessException(401, "无效的Token");
        }
        Long userId = JwtUtil.getUserId(token);
        return Result.success(userService.getUserInfo(userId));
    }
}
