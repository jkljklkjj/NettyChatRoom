package com.example.aspect;

import com.example.annotation.RequireUserId;
import com.example.common.api.ApiResponse;
import com.example.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserIdAspect {

    private final HttpServletRequest request;

    public UserIdAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@annotation(requireUserId)")
    public Object checkUserId(ProceedingJoinPoint joinPoint, RequireUserId requireUserId) throws Throwable {
        Integer userId = (Integer) request.getAttribute("UserId");
        if (userId == null) {
            return ApiResponse.failure(ErrorCode.LOGIN_FAIL,"用户未登录");
        }
        return joinPoint.proceed();
    }
}
