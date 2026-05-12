package com.agrolink.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT（JSON Web Token）工具类。
 * <p>
 * 提供 JWT 令牌的生成、解析和验证功能。
 * 使用 HMAC-SHA256 算法签名，支持访问令牌（短有效期）和刷新令牌（长有效期）。
 * 令牌中包含用户 ID、用户名和角色信息。
 * </p>
 */
public class JwtUtil {

    /** JWT 签名密钥字符串（长度需满足 HMAC-SHA256 最少 256 位的要求） */
    private static final String SECRET = "AgroLinkSecretKeyForJWTTokenGeneration2024MustBe256BitsLong!";

    /** 根据密钥字符串生成的 HMAC-SHA256 签名密钥对象 */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /** 访问令牌过期时间：30 分钟（单位：毫秒） */
    private static final long ACCESS_EXPIRATION = 1800000L;

    /** 刷新令牌过期时间：7 天（单位：毫秒） */
    private static final long REFRESH_EXPIRATION = 604800000L;

    /**
     * 生成访问令牌（Access Token）。
     * <p>
     * 有效期较短（默认 30 分钟），用于接口鉴权。
     * </p>
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT 访问令牌字符串
     */
    public static String generateToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, ACCESS_EXPIRATION);
    }

    /**
     * 生成刷新令牌（Refresh Token）。
     * <p>
     * 有效期较长（默认 7 天），用于在访问令牌过期后获取新的访问令牌。
     * </p>
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT 刷新令牌字符串
     */
    public static String generateRefreshToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, REFRESH_EXPIRATION);
    }

    /**
     * 构建 JWT 令牌的私有方法。
     * <p>
     * 将用户 ID 作为 subject（主题），用户名和角色作为自定义声明，
     * 设置签发时间和过期时间，使用 HMAC-SHA256 密钥签名。
     * </p>
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param role       用户角色
     * @param expiration 令牌有效期（毫秒）
     * @return JWT 令牌字符串
     */
    private static String buildToken(Long userId, String username, String role, long expiration) {
        return Jwts.builder()
                .subject(userId.toString())                           // 主题：用户 ID
                .claim("username", username)                          // 自定义声明：用户名
                .claim("role", role)                                  // 自定义声明：角色
                .issuedAt(new Date())                                 // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(KEY)                                        // 使用 HMAC-SHA256 签名
                .compact();
    }

    /**
     * 解析 JWT 令牌，获取其声明（Claims）。
     *
     * @param token JWT 令牌字符串
     * @return 解析后的 Claims 对象，包含令牌中的所有声明信息
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)      // 使用密钥验证签名
                .build()
                .parseSignedClaims(token)
                .getPayload();        // 获取令牌负载
    }

    /**
     * 从令牌中提取用户 ID。
     *
     * @param token JWT 令牌字符串
     * @return 用户 ID
     */
    public static Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从令牌中提取用户名。
     *
     * @param token JWT 令牌字符串
     * @return 用户名
     */
    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从令牌中提取用户角色。
     *
     * @param token JWT 令牌字符串
     * @return 用户角色
     */
    public static String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 验证 JWT 令牌是否有效。
     * <p>
     * 通过尝试解析令牌来判断其是否合法（签名正确且在有效期内）。
     * 如果解析过程中抛出任何异常，则视为无效令牌。
     * </p>
     *
     * @param token JWT 令牌字符串
     * @return 令牌有效返回 true，否则返回 false
     */
    public static boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌的剩余有效期。
     *
     * @param token JWT 令牌字符串
     * @return 剩余毫秒数，如果已过期则返回 0
     */
    public static long getRemainingTtl(String token) {
        Date exp = parseToken(token).getExpiration();
        long remaining = exp.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }
}
