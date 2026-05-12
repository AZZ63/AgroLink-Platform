package com.agrolink.userservice.service;

import com.agrolink.common.dto.LoginRequest;
import com.agrolink.common.dto.LoginResponse;
import com.agrolink.common.dto.RegisterRequest;
import com.agrolink.common.dto.UserInfo;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.util.JwtUtil;
import com.agrolink.userservice.entity.Role;
import com.agrolink.userservice.entity.User;
import com.agrolink.userservice.entity.UserRole;
import com.agrolink.userservice.repository.RoleRepository;
import com.agrolink.userservice.repository.UserRepository;
import com.agrolink.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证与业务逻辑层
 * <p>
 * 提供注册、登录、Token 刷新、退出登录及用户信息查询等功能，
 * 使用 Redis 维护 Token 黑名单实现登出即时失效。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StringRedisTemplate redisTemplate;

    /** Redis 中 Token 黑名单的 key 前缀 */
    private static final String BLACKLIST_PREFIX = "agrolink:token:blacklist:";

    /**
     * 用户注册
     * <p>
     * 校验用户名唯一性及角色合法性，密码加盐加密后入库，
     * 同时写入 user_role 映射表，最后生成并返回 Access Token 与 Refresh Token。
     */
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (!request.getRole().equals("FARMER") && !request.getRole().equals("MERCHANT")) {
            throw new BusinessException(400, "无效的用户角色");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        userRepository.insert(user);

        // 写入 user_role 映射表
        Role role = roleRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, request.getRole()));
        if (role != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleRepository.insert(ur);
        }

        return buildLoginResponse(user);
    }

    /**
     * 用户登录
     * <p>
     * 根据用户名查找用户，校验密码，验证通过后生成并返回 Token。
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordMatches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return buildLoginResponse(user);
    }

    /**
     * 使用 Refresh Token 刷新 Access Token
     * <p>
     * 先校验 Refresh Token 的有效性及其是否已被撤销，
     * 验证通过后生成新的 Access Token 并返回。
     */
    public LoginResponse refresh(String refreshToken) {
        if (!JwtUtil.isTokenValid(refreshToken)) {
            throw new BusinessException(401, "无效的刷新令牌");
        }
        // 检查 refresh token 是否已被撤销
        String blacklistKey = BLACKLIST_PREFIX + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new BusinessException(401, "刷新令牌已失效");
        }

        Long userId = JwtUtil.getUserId(refreshToken);
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 生成新的 token，返回相同 refresh token
        String newToken = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(newToken, refreshToken, user.getUsername(), user.getRole(), user.getId());
    }

    /**
     * 退出登录
     * <p>
     * 将当前 Access Token 加入 Redis 黑名单，有效期与 Token 剩余有效期一致，
     * 从而实现 Token 即时失效。
     */
    public void logout(String accessToken) {
        // 将 access token 加入黑名单，有效期等于 token 剩余有效期
        long ttl = JwtUtil.getRemainingTtl(accessToken);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "1",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 全设备退出登录
     * <p>
     * 同时将 Access Token 和 Refresh Token 加入黑名单，
     * 使所有设备上的登录态同时失效。
     */
    public void logoutAll(String accessToken, String refreshToken) {
        logout(accessToken);
        // 同时撤销 refresh token
        long refreshTtl = JwtUtil.getRemainingTtl(refreshToken);
        if (refreshTtl > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + refreshToken,
                    "1",
                    refreshTtl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 根据用户 ID 获取用户基本信息
     */
    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRole(user.getRole());
        info.setPhone(user.getPhone());
        return info;
    }

    /**
     * 构建登录响应对象
     * <p>
     * 生成 Access Token 与 Refresh Token 并封装到 LoginResponse 中返回。
     */
    private LoginResponse buildLoginResponse(User user) {
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, refreshToken, user.getUsername(), user.getRole(), user.getId());
    }

    /**
     * 密码加密
     * <p>
     * 使用 MD5 + 固定盐值（_agrolink_salt）对原始密码进行单向哈希。
     */
    private String passwordEncode(String rawPassword) {
        return org.springframework.util.DigestUtils.md5DigestAsHex((rawPassword + "_agrolink_salt").getBytes());
    }

    /**
     * 密码比对
     * <p>
     * 将原始密码加密后与数据库中存储的密文进行比对。
     */
    private boolean passwordMatches(String rawPassword, String encoded) {
        return passwordEncode(rawPassword).equals(encoded);
    }
}
