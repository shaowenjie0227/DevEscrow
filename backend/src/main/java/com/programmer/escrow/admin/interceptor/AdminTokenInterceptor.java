package com.programmer.escrow.admin.interceptor;

import com.programmer.escrow.admin.context.AdminContextHolder;
import com.programmer.escrow.admin.context.AdminLoginUser;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.infra.redis.TokenCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminTokenInterceptor implements HandlerInterceptor {

    private final TokenCacheService tokenCacheService;

    public AdminTokenInterceptor(TokenCacheService tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestUri = request.getRequestURI();
        if ("/api/admin/auth/login".equals(requestUri)) {
            return true;
        }
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BizException(401, "缺少管理员登录凭证");
        }
        String cacheValue = tokenCacheService.getAdminTokenPayload(token);
        if (!StringUtils.hasText(cacheValue)) {
            throw new BizException(401, "管理员登录已过期或凭证无效");
        }
        String[] parts = cacheValue.split(":");
        if (parts.length < 2) {
            throw new BizException(401, "管理员登录凭证格式无效");
        }
        AdminContextHolder.set(new AdminLoginUser(Long.parseLong(parts[0]), parts[1]));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AdminContextHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return request.getHeader("X-Admin-Token");
    }
}
