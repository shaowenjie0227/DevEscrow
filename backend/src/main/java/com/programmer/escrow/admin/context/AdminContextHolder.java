package com.programmer.escrow.admin.context;

import com.programmer.escrow.common.exception.BizException;

public final class AdminContextHolder {

    private static final ThreadLocal<AdminLoginUser> ADMIN_HOLDER = new ThreadLocal<>();

    private AdminContextHolder() {
    }

    public static void set(AdminLoginUser adminLoginUser) {
        ADMIN_HOLDER.set(adminLoginUser);
    }

    public static AdminLoginUser get() {
        return ADMIN_HOLDER.get();
    }

    public static Long getRequiredAdminId() {
        AdminLoginUser loginUser = ADMIN_HOLDER.get();
        if (loginUser == null || loginUser.getAdminId() == null) {
            throw new BizException(401, "请先登录管理后台");
        }
        return loginUser.getAdminId();
    }

    public static void clear() {
        ADMIN_HOLDER.remove();
    }
}
