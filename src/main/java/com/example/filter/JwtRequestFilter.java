package com.example.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.service.redis.RedisService;
import com.example.util.JwtUtil;

import io.jsonwebtoken.Claims;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final String REDIS_KEY_PREFIX = "user:";
    private final RedisService jedis;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil, RedisService jedis) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.jedis = jedis;
    }

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        // 获取请求头中的 Authorization 字段
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        System.out.println("Request URL: " + request.getRequestURL());

        // 如果 Authorization 字段不为空且以 Bearer 开头
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 获取开头后面的部分
            jwt = authorizationHeader.substring(7);
            System.out.println(jwt);

            Claims claims = jwtUtil.extractClaims(jwt);
            username = claims.getSubject();

            Integer userId = (Integer)jedis.get(jwt);
            if (userId != null) {
                request.setAttribute("UserId", userId);
            } else {
                // 如果无法获取用户 ID，拒绝请求
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 通过用户名获取用户信息
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 如果传入的用户名验证token有效
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}