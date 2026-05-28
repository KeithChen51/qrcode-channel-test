package com.byd.qrcode.controller;

import com.byd.qrcode.auth.AdminPrincipal;
import com.byd.qrcode.auth.AdminAuthService;
import com.byd.qrcode.auth.AdminUserContext;
import com.byd.qrcode.auth.AuthException;
import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.AuthResponse;
import com.byd.qrcode.dto.ChangePasswordRequest;
import com.byd.qrcode.dto.CurrentUserResponse;
import com.byd.qrcode.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService authService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request.getUsername(), request.getPassword()));
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

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Result<Void>> handleAuthException(AuthException ex) {
        return ResponseEntity.status(ex.status()).body(Result.error(ex.status(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Result.error(400, ex.getMessage()));
    }
}
