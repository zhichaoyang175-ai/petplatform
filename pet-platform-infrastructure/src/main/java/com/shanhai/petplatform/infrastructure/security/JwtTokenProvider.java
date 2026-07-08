package com.shanhai.petplatform.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 令牌工具 — 生成、解析、校验
 *
 * @author PetPlatform Team
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secretBase64;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ────────────────── 令牌生成 ──────────────────

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @return JWT 字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌（有效期更长）
     *
     * @param userId 用户ID
     * @return JWT 字符串
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // ────────────────── 令牌解析 ──────────────────

    /**
     * 从令牌中提取用户ID
     *
     * @param token JWT 字符串
     * @return 用户ID
     * @throws JwtException 令牌无效时抛出
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 获取令牌剩余有效时间（毫秒），用于登出时将令牌加入黑名单并设置 TTL
     *
     * @param token JWT 字符串
     * @return 剩余毫秒，已过期或无效则返回 0
     */
    public long getRemainingExpirationMs(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (JwtException e) {
            return 0;
        }
    }

    // ────────────────── 令牌校验 ──────────────────

    /**
     * 验证令牌有效性
     *
     * @param token JWT 字符串
     * @return true-有效, false-无效/过期
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("不支持的 JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("JWT 格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.debug("JWT 签名不匹配: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT 参数非法: {}", e.getMessage());
        }
        return false;
    }

}
