package com.example.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.service.redis.RedisService;

@Component
public class JwtUtil {

    private final String secretKey = "2998568539";

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", username);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 提取 token 中的信息
     * @param token token 字符串
     * @return token 中的信息
     */
    public Claims extractClaims(String token) {
        System.out.println("extractClaims");
        return Jwts.parser()
                // 通过密钥解析 token
                .setSigningKey(secretKey)
                // 获取 token 中的 body 部分
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractClaims(token).getSubject()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public static int validateTokenAndExtractUser(String authorizationHeader) {
        RedisService jedis = new RedisService();
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            // // 解析JWT
            // Claims claims = jwtUtil.extractClaims(jwt);
            // String username = claims.getSubject();

            Integer userId = Integer.valueOf(jedis.get(jwt));
            if (userId != null) {
                return userId;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("1");
        System.out.println(token);
    }
}