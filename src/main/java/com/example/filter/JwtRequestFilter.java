package com.example.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Request URL: " + request.getRequestURL());
        int serverPort = request.getServerPort();
        if (serverPort != 8088) {
            // System.out.println("非 8080 端口请求，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/user/login") || requestURI.endsWith("/user/register")) {
            filterChain.doFilter(request, response);
            System.out.println("用户登录或注册请求，放行");
            return;
        }
        // 获取请求头中的 Authorization 字段
        final String authorizationHeader = request.getHeader("Authorization");

        try {
            // 如果 Authorization 字段不为空且以 Bearer 开头
            int userId = JwtUtil.validateTokenAndExtractUser(authorizationHeader);
            if (userId != 0) {
                request.setAttribute("UserId", userId);
            } else {
                // 如果无法获取用户 ID，拒绝请求
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid token");
                return;
            }
        } catch (Exception e) {
            // 捕获异常并返回 401 未授权状态
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}