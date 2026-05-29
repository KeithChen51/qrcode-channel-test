package com.byd.qrcode.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.byd.qrcode.dto.AuthResponse;
import com.byd.qrcode.entity.AdminUser;
import com.byd.qrcode.mapper.AdminUserMapper;
import com.byd.qrcode.security.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordHasher passwordHasher;
    private final AuthTokenService authTokenService;
    private final RateLimitService rateLimitService;
    private final AuthProperties authProperties;

    public AuthResponse login(String username, String password) {
        return login(username, password, "unknown");
    }

    public AuthResponse login(String username, String password, String clientIp) {
        String rateLimitKey = loginRateLimitKey(username, clientIp);
        if (!rateLimitService.tryAcquire(
                "auth-login",
                rateLimitKey,
                authProperties.getLoginMaxAttempts(),
                Math.max(authProperties.getLoginLockMinutes(), 1) * 60)) {
            throw new AuthException(429, "Too many failed login attempts. Please try again later.");
        }

        AdminUser user = findByUsername(username);
        if (user == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthException(401, "Invalid username or password");
        }

        rateLimitService.reset("auth-login", rateLimitKey);
        String token = authTokenService.issue(user.getUsername());
        AuthTokenService.VerifiedToken verifiedToken = authTokenService.verify(token);
        return new AuthResponse(
                token,
                user.getUsername(),
                isMustChangePassword(user),
                verifiedToken.expiresAt());
    }

    public AdminPrincipal verifyToken(String token) {
        AuthTokenService.VerifiedToken verifiedToken;
        try {
            verifiedToken = authTokenService.verify(token);
        } catch (IllegalArgumentException ex) {
            throw new AuthException(401, "Authentication required or session expired");
        }

        AdminUser user = findByUsername(verifiedToken.username());
        if (user == null) {
            throw new AuthException(401, "Authentication required or session expired");
        }
        Long passwordChangedAt = user.getPasswordChangedAt();
        if (passwordChangedAt != null && verifiedToken.issuedAt() < passwordChangedAt) {
            throw new AuthException(401, "Password changed. Please sign in again.");
        }
        return new AdminPrincipal(
                user.getUsername(),
                isMustChangePassword(user),
                verifiedToken.issuedAt(),
                verifiedToken.expiresAt());
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        AdminUser user = findByUsername(username);
        if (user == null) {
            throw new AuthException(401, "Authentication required or session expired");
        }
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        validateNewPassword(newPassword);

        user.setPasswordHash(passwordHasher.hash(newPassword));
        user.setMustChangePassword(0);
        user.setPasswordChangedAt(Instant.now().getEpochSecond());
        adminUserMapper.updateById(user);
    }

    private AdminUser findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return adminUserMapper.selectOne(new QueryWrapper<AdminUser>().eq("username", username.trim()));
    }

    private boolean isMustChangePassword(AdminUser user) {
        return user.getMustChangePassword() != null && user.getMustChangePassword() == 1;
    }

    private String loginRateLimitKey(String username, String clientIp) {
        String normalizedUsername = StringUtils.hasText(username)
                ? username.trim().toLowerCase()
                : "unknown";
        String normalizedClientIp = StringUtils.hasText(clientIp)
                ? clientIp.trim()
                : "unknown";
        return normalizedUsername + "|" + normalizedClientIp;
    }

    private void validateNewPassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8 || password.length() > 64) {
            throw new IllegalArgumentException("New password length must be between 8 and 64 characters");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            hasLetter = hasLetter || Character.isLetter(ch);
            hasDigit = hasDigit || Character.isDigit(ch);
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("New password must contain at least one letter and one digit");
        }
    }
}
