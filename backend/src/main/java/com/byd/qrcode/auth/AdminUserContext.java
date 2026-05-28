package com.byd.qrcode.auth;

public final class AdminUserContext {

    private static final ThreadLocal<AdminPrincipal> HOLDER = new ThreadLocal<>();

    private AdminUserContext() {
    }

    public static void set(AdminPrincipal principal) {
        HOLDER.set(principal);
    }

    public static AdminPrincipal current() {
        AdminPrincipal principal = HOLDER.get();
        if (principal == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        return principal;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
