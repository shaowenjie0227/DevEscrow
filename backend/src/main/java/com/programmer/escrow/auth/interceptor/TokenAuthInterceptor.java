package com.programmer.escrow.auth.interceptor;

import com.programmer.escrow.auth.context.LoginUser;
import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.infra.redis.TokenCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
public class TokenAuthInterceptor implements HandlerInterceptor {

    private final TokenCacheService tokenCacheService;

    public TokenAuthInterceptor(TokenCacheService tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/community/") && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BizException(401, "缺少登录凭证");
        }
        String cacheValue = tokenCacheService.getUserTokenPayload(token);
        if (!StringUtils.hasText(cacheValue)) {
            throw new BizException(401, "登录已过期或凭证无效");
        }
        String[] parts = cacheValue.split(":");
        if (parts.length < 2) {
            throw new BizException(401, "登录凭证格式无效");
        }
        LoginUser loginUser = new LoginUser(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        validateRouteAccess(requestUri, loginUser);
        UserContextHolder.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return request.getHeader("X-Token");
    }

    private void validateRouteAccess(String requestUri, LoginUser loginUser) {
        if (requestUri.startsWith("/api/client/") && !loginUser.canAccessClientSide()) {
            throw new BizException(403, "当前账号无权访问用户端接口");
        }
        if (requestUri.startsWith("/api/developer/") && !loginUser.canAccessDeveloperSide()) {
            throw new BizException(403, "当前账号无权访问开发者端接口");
        }
        Objects.requireNonNull(loginUser.getUserId(), "userId must not be null");
    }
}
