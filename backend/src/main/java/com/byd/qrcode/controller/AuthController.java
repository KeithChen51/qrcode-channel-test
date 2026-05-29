package com.byd.qrcode.controller;

import com.byd.qrcode.auth.AdminAuthInterceptor;
import com.byd.qrcode.auth.AdminAuthService;
import com.byd.qrcode.auth.AdminPrincipal;
import com.byd.qrcode.auth.AdminUserContext;
import com.byd.qrcode.auth.AuthException;
import com.byd.qrcode.auth.AuthProperties;
import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.AuthResponse;
import com.byd.qrcode.dto.ChangePasswordRequest;
import com.byd.qrcode.dto.CurrentUserResponse;
import com.byd.qrcode.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService authService;
    private final AuthProperties authProperties;

    @PostMapping("/login")
    public Result<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(
                request.getUsername(),
                request.getPassword(),
                getClientIp(httpRequest));
        writeAuthCookie(httpRequest, httpResponse, response.token(), authProperties.getTokenTtlMinutes());
        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<CurrentUserResponse> me() {
        AdminPrincipal principal = AdminUserContext.current();
        return Result.success(new CurrentUserResponse(principal.username(), principal.mustChangePassword()));
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(
                AdminUserContext.current().username(),
                request.getCurrentPassword(),
                request.getNewPassword());
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        writeAuthCookie(request, response, "", 0);
        return Result.success();
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Result<Void>> handleAuthException(AuthException ex) {
        return ResponseEntity.status(ex.status()).body(Result.error(ex.status(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Result.error(400, ex.getMessage()));
    }

    private void writeAuthCookie(HttpServletRequest request, HttpServletResponse response, String token, long maxAgeMinutes) {
        ResponseCookie cookie = ResponseCookie.from(AdminAuthInterceptor.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(maxAgeMinutes))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(forwardedProto);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
