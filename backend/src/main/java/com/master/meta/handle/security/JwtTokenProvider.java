package com.master.meta.handle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    /**
     * 生成用于JWT签名的密钥
     * 确保密钥符合JWT JWA规范要求（至少256位）
     *
     * @return SecretKey 用于JWT签名的安全密钥
     * @throws IllegalArgumentException 当密钥不符合安全要求时抛出异常
     */
    private SecretKey key() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
            // JWT JWA规范要求HMAC-SHA算法的密钥长度必须≥256位
            if (keyBytes.length < 32) {
                log.error("JWT secret key is too short: {} bits, minimum required: 256 bits", keyBytes.length * 8);
                throw new IllegalArgumentException(
                        "JWT secret key must be at least 256 bits as per RFC 7518, Section 3.2. Current key size: " + (keyBytes.length * 8) + " bits"
                );
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to create JWT signing key: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid JWT configuration", e);
        }
    }

    public String generateToken(String username, String tokenType) {
        // 验证令牌类型是否有效
        if (!"access_token".equals(tokenType) && !"refresh_token".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        // 根据令牌类型设置过期时间
        long expirationTime = System.currentTimeMillis() + (tokenType.equals("access_token") ? jwtProperties.getAccessTokenValidity() : jwtProperties.getRefreshTokenValidity());

        // 构建并返回JWT令牌
        return Jwts.builder()
                .header().type("JWT").and()
                .subject(username)
                .issuedAt(new Date()).expiration(new Date(expirationTime))
                .claim("token_type", tokenType)
                .signWith(key())
                .compact();
    }

    /**
     * 提取JWT令牌中的声明信息
     *
     * @param token JWT令牌字符串
     * @return 包含令牌中所有声明信息的Claims对象
     * @throws JwtException 当令牌无效、过期或解析失败时抛出异常
     */
    public Claims extractClaims(String token) {
        try {
            // 使用JWT解析器验证并解析签名的令牌，然后获取载荷部分
            return Jwts.parser()
                    .verifyWith(key()).build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to parse JWT token: {}", token, e);
            throw new JwtException("Invalid or expired token");
        }
    }

    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    public Date extractExpiration(String token) {
        Claims claims = extractClaims(token);
        return claims.getExpiration();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
