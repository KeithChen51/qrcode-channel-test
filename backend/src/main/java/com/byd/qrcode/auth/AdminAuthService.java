package com.byd.qrcode.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.byd.qrcode.dto.AuthResponse;
import com.byd.qrcode.entity.AdminUser;
import com.byd.qrcode.mapper.AdminUserMapper;
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

    public AuthResponse login(String username, String password) {
        AdminUser user = findByUsername(username);
        if (user == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthException(401, "账号或密码不正确");
        }

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
            throw new AuthException(401, "未登录或登录已过期");
        }

        AdminUser user = findByUsername(verifiedToken.username());
        if (user == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        Long passwordChangedAt = user.getPasswordChangedAt();
        if (passwordChangedAt != null && verifiedToken.issuedAt() < passwordChangedAt) {
            throw new AuthException(401, "密码已变更，请重新登录");
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
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("当前密码不正确");
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

    private void validateNewPassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8 || password.length() > 64) {
            throw new IllegalArgumentException("新密码长度需要在 8 到 64 位之间");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            hasLetter = hasLetter || Character.isLetter(ch);
            hasDigit = hasDigit || Character.isDigit(ch);
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("新密码至少需要包含字母和数字");
        }
    }
}
