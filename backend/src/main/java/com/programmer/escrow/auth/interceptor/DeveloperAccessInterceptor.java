package com.programmer.escrow.auth.interceptor;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DeveloperAccessInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    public DeveloperAccessInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        UserEntity user = userMapper.selectById(UserContextHolder.getRequiredUserId());
        if (user == null) {
            throw new BizException(401, "请先登录");
        }
        if (user.getDeveloperStatus() == null || user.getDeveloperStatus() != 2) {
            throw new BizException(403, "开发者申请审核通过后才可访问该接口");
        }
        return true;
    }
}
