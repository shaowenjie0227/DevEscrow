package com.programmer.escrow.auth.context;

import com.programmer.escrow.common.exception.BizException;

public final class UserContextHolder {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(LoginUser loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getRequiredUserId() {
        LoginUser loginUser = USER_HOLDER.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BizException(401, "请先登录");
        }
        return loginUser.getUserId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
