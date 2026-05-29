package com.byd.qrcode.auth;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.byd.qrcode.entity.AdminUser;
import com.byd.qrcode.mapper.AdminUserMapper;
import com.byd.qrcode.security.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AuthTokenService authTokenService;

    private AuthProperties properties;
    private AdminAuthService service;

    @BeforeEach
    void setUp() {
        properties = new AuthProperties();
        properties.setLoginMaxAttempts(3);
        properties.setLoginLockMinutes(10);
        service = new AdminAuthService(
                adminUserMapper,
                passwordHasher,
                authTokenService,
                new RateLimitService(),
                properties);
    }

    @Test
    void locksLoginAfterRepeatedBadPasswordsForSameClient() {
        AdminUser user = new AdminUser();
        user.setUsername("admin");
        user.setPasswordHash("hash");
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);

        assertEquals(401, assertThrows(AuthException.class,
                () -> service.login("admin", "wrong", "1.2.3.4")).status());
        assertEquals(401, assertThrows(AuthException.class,
                () -> service.login("admin", "wrong", "1.2.3.4")).status());
        assertEquals(401, assertThrows(AuthException.class,
                () -> service.login("admin", "wrong", "1.2.3.4")).status());

        AuthException blocked = assertThrows(AuthException.class,
                () -> service.login("admin", "wrong", "1.2.3.4"));

        assertEquals(429, blocked.status());
    }
}
