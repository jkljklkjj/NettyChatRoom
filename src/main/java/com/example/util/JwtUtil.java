package com.example.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {

    private static final String SECRET_KEY = "2998568539";

    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", username);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 提取 token 中的信息
     * @param token token 字符串
     * @return token 中的信息
     */
    public static Claims extractClaims(String token) {
        // System.out.println("extractClaims");
        return Jwts.parser()
                // 通过密钥解析 token
                .setSigningKey(SECRET_KEY)
                // 获取 token 中的 body 部分
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token, String username) {
        return username.equals(extractClaims(token).getSubject()) && !isTokenExpired(token);
    }

    private static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public static int validateTokenAndExtractUser(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            // 解析JWT
            Claims claims = JwtUtil.extractClaims(jwt);
            String username=claims.get("userId", String.class);
            Integer userId = Integer.valueOf(username);
            return userId;
        }
        return 0;
    }

    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("1");
        System.out.println(token);
    }
}