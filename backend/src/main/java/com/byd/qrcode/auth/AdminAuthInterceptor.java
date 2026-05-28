package com.byd.qrcode.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicApi(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        try {
            AdminPrincipal principal = authService.verifyToken(authorization.substring("Bearer ".length()).trim());
            if (principal.mustChangePassword() && !isPasswordChangeApi(request)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "请先修改初始密码");
                return false;
            }
            AdminUserContext.set(principal);
            return true;
        } catch (AuthException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AdminUserContext.clear();
    }

    private boolean isPublicApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        path = normalizePath(request, path);
        return "/api/auth/login".equals(path)
                || "/api/health".equals(path)
                || path.startsWith("/api/public/")
                || path.matches("^/api/qrcodes/\\d+/image$")
                || "/api/scans/h5".equals(path)
                || "/api/scans/register".equals(path);
    }

    private boolean isPasswordChangeApi(HttpServletRequest request) {
        String path = normalizePath(request, request.getRequestURI());
        return "/api/auth/me".equals(path) || "/api/auth/change-password".equals(path);
    }

    private String normalizePath(HttpServletRequest request, String path) {
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + escapeJson(message) + "\",\"data\":null}");
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
