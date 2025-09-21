package com.example.service.security;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 面向业务的 JWT 服务，取代静态工具类。线程安全（无可变共享状态）。
 */
@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    /** 生成包含用户ID的 Token */
    public String generateToken(int userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + properties.getExpirationMs());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("uid", userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, properties.getSecret())
                .compact();
    }

    /** 验证 Token 是否有效 */
    public boolean validate(String token) {
        return parseClaims(token).isPresent();
    }

    /** 从 Token 中获取用户ID，如果无效返回 0 */
    public int extractUserId(String token) {
        return parseClaims(token).map(c -> {
            Object uid = c.get("uid");
            if (uid instanceof Integer) return (Integer) uid;
            if (uid instanceof Number) return ((Number) uid).intValue();
            try { return Integer.parseInt(c.getSubject()); } catch (NumberFormatException e) { return 0; }
        }).orElse(0);
    }

    /** 解析 Authorization 头，自动剥离前缀 */
    public int extractUserIdFromAuthorization(String authorizationHeader) {
        // todo 用redis存储解析结果
        String token = resolveToken(authorizationHeader);
        if (token == null) return 0;
        return extractUserId(token);
    }

    /** 解析 Claims */
    public Optional<Claims> parseClaims(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(properties.getSecret())
                    .parseClaimsJws(token);
            return Optional.ofNullable(jws.getBody());
        } catch (ExpiredJwtException e) {
            log.debug("JWT 过期: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 无效: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /** 提取纯 token */
    public String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = properties.getPrefix();
        if (authorizationHeader.startsWith(prefix)) {
            return authorizationHeader.substring(prefix.length()).trim();
        }
        return authorizationHeader; // 允许直接传裸 token
    }
}

