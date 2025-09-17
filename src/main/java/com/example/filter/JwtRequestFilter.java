package com.example.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.service.security.JwtService; // 新增

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtRequestFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Request URL: " + request.getRequestURL());
        int serverPort = request.getServerPort();
        if (serverPort != 8088) {
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
            int userId = jwtService.extractUserIdFromAuthorization(authorizationHeader);
            if (userId != 0) {
                request.setAttribute("UserId", userId);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid token");
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}