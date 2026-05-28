CREATE TABLE IF NOT EXISTS `admin_users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Admin username',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Password hash',
    `must_change_password` TINYINT NOT NULL DEFAULT 1 COMMENT 'Must change password after first login',
    `password_changed_at` BIGINT NOT NULL DEFAULT 0 COMMENT 'Password change epoch seconds',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin users';
