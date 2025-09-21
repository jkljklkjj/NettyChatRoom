package com.example.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.service.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    private static final Set<String> WHITELIST = Set.of(
            "/user/login",
            "/user/register",
            "/user/loginByEmail",
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/doc.html",
            "/error"
    );

    public JwtRequestFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        // Swagger 资源前缀与静态资源直接放行
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        int userId = jwtService.extractUserIdFromAuthorization(authorizationHeader);
        if (userId == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Unauthorized or invalid token\"}");
            return;
        }
        request.setAttribute("UserId", userId);
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        if (uri == null) return true; // 容错
        if (WHITELIST.contains(uri)) return true;
        // 以 swagger-resources、/webjars、/swagger-ui、/v3/api-docs 等前缀的也放行
        return uri.startsWith("/swagger") || uri.startsWith("/webjars") || uri.startsWith("/v3/api-docs");
    }
}