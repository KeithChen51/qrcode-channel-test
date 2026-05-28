-- QR code channel system database initialization script
-- This script is used by docker-compose mysql init.

-- CREATE DATABASE IF NOT EXISTS qrcode_channel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE qrcode_channel;

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

CREATE TABLE IF NOT EXISTS `wechat_config` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT 'Mini program name',
    `app_id` VARCHAR(64) NOT NULL COMMENT 'Mini program app id',
    `original_id` VARCHAR(64) COMMENT 'Mini program original id (gh_*)',
    `app_secret` VARCHAR(128) NOT NULL COMMENT 'Mini program app secret',
    `page_path` VARCHAR(256) NOT NULL COMMENT 'Landing page path',
    `default_env_version` ENUM('release', 'trial', 'develop') NOT NULL DEFAULT 'release' COMMENT 'Default env version',
    `is_active` INT NOT NULL DEFAULT 0 COMMENT 'Whether this config is active',
    `access_token` TEXT COMMENT 'Cached access token',
    `access_token_expires_at` BIGINT COMMENT 'Access token expire timestamp',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_app_id` (`app_id`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mini program configuration';

CREATE TABLE IF NOT EXISTS `qrcode_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `config_id` INT COMMENT 'Linked mini program config id',
    `app_id` VARCHAR(64) COMMENT 'Mini program app id',
    `env_version` ENUM('release', 'trial', 'develop') DEFAULT 'release' COMMENT 'Environment version',
    `store_id` VARCHAR(64) NOT NULL COMMENT 'Store id',
    `store_name` VARCHAR(128) COMMENT 'Store name',
    `staff_id` VARCHAR(64) NOT NULL COMMENT 'Staff id',
    `staff_name` VARCHAR(128) COMMENT 'Staff name',
    `url_link` TEXT COMMENT 'WeChat URL Link',
    `jump_page_url` TEXT COMMENT 'H5 jump page URL',
    `campaign_id` INT COMMENT 'Linked campaign id',
    `qrcode_url` TEXT COMMENT 'QR code image URL',
    `scan_count` INT NOT NULL DEFAULT 0 COMMENT 'Scan count',
    `register_count` INT NOT NULL DEFAULT 0 COMMENT 'Register count',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_staff_id` (`staff_id`),
    INDEX `idx_campaign_id` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='QR code records';

CREATE TABLE IF NOT EXISTS `scan_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `scan_id` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Unique scan id',
    `qrcode_id` INT NOT NULL COMMENT 'Linked qr code id',
    `config_id` INT COMMENT 'Linked mini program config id',
    `app_id` VARCHAR(64) COMMENT 'Mini program app id',
    `store_id` VARCHAR(64) NOT NULL COMMENT 'Store id',
    `store_name` VARCHAR(128) COMMENT 'Store name',
    `staff_id` VARCHAR(64) NOT NULL COMMENT 'Staff id',
    `staff_name` VARCHAR(128) COMMENT 'Staff name',
    `campaign_id` INT COMMENT 'Linked campaign id',
    `campaign_name` VARCHAR(128) COMMENT 'Campaign name',
    `user_open_id` VARCHAR(64) COMMENT 'User open id',
    `user_nickname` VARCHAR(128) COMMENT 'User nickname',
    `user_phone` VARCHAR(20) COMMENT 'User phone',
    `is_registered` INT NOT NULL DEFAULT 0 COMMENT 'Whether user registered',
    `registered_user_id` VARCHAR(64) COMMENT 'Registered user id',
    `registered_at` TIMESTAMP COMMENT 'Register time',
    `ip_address` VARCHAR(64) COMMENT 'IP address',
    `user_agent` TEXT COMMENT 'User agent',
    `scan_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Scan time',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_scan_id` (`scan_id`),
    INDEX `idx_qrcode_id` (`qrcode_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_staff_id` (`staff_id`),
    INDEX `idx_scan_time` (`scan_time`),
    INDEX `idx_is_registered` (`is_registered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scan records';

CREATE TABLE IF NOT EXISTS `campaigns` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT 'Campaign name',
    `description` TEXT COMMENT 'Campaign description',
    `status` ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT 'Campaign status',
    `theme_color` VARCHAR(16) NOT NULL DEFAULT '#1890ff' COMMENT 'Theme color',
    `background_color` VARCHAR(16) NOT NULL DEFAULT '#f5f5f5' COMMENT 'Background color',
    `title` VARCHAR(128) NOT NULL DEFAULT 'Welcome' COMMENT 'Title text',
    `subtitle` VARCHAR(256) COMMENT 'Subtitle text',
    `button_text` VARCHAR(64) NOT NULL DEFAULT 'Open Mini Program' COMMENT 'Button text',
    `logo_url` TEXT COMMENT 'Logo URL',
    `background_image_url` TEXT COMMENT 'Background image URL',
    `start_time` DATETIME NULL COMMENT 'Start time',
    `end_time` DATETIME NULL COMMENT 'End time',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_status` (`status`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Campaign configuration';
