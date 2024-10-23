package com.example.util;

import com.example.service.redis.RedisService;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtUtil jwtUtil;
    private final RedisService jedis;

    public JwtService(JwtUtil jwtUtil, RedisService jedis) {
        this.jwtUtil = jwtUtil;
        this.jedis = jedis;
    }

    public int validateTokenAndExtractUser(String authorizationHeader){
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            // // 解析JWT
            // Claims claims = jwtUtil.extractClaims(jwt);
            // String username = claims.getSubject();

            Integer userId = (Integer) jedis.get(jwt);
            if (userId != null) {
                return userId;
            }
        }
        return 0;
    }
}