package com.byd.qrcode.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.byd.qrcode.entity.AdminUser;
import com.byd.qrcode.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AdminUserBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final AdminUserMapper adminUserMapper;
    private final PasswordHasher passwordHasher;
    private final AuthProperties authProperties;

    @Override
    public void run(ApplicationArguments args) {
        createAdminUsersTable();
        Long userCount = adminUserMapper.selectCount(new QueryWrapper<>());
        if (userCount != null && userCount > 0) {
            return;
        }
        if (!StringUtils.hasText(authProperties.getAdminInitialPassword())) {
            throw new IllegalStateException("ADMIN_INITIAL_PASSWORD must be configured when no admin user exists");
        }

        AdminUser user = new AdminUser();
        user.setUsername(resolveAdminUsername());
        user.setPasswordHash(passwordHasher.hash(authProperties.getAdminInitialPassword()));
        user.setMustChangePassword(1);
        user.setPasswordChangedAt(Instant.now().getEpochSecond());
        adminUserMapper.insert(user);
    }

    private String resolveAdminUsername() {
        return StringUtils.hasText(authProperties.getAdminUsername())
                ? authProperties.getAdminUsername().trim()
                : "admin";
    }

    private void createAdminUsersTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `admin_users` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `username` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Admin username',
                    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Password hash',
                    `must_change_password` TINYINT NOT NULL DEFAULT 1 COMMENT 'Must change password after first login',
                    `password_changed_at` BIGINT NOT NULL DEFAULT 0 COMMENT 'Password change epoch seconds',
                    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX `idx_username` (`username`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin users'
                """);
    }
}
