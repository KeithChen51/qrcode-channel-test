-- 地推渠道活码生成系统 - 数据库初始化脚本
-- 从 Drizzle ORM schema 转换而来

-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS qrcode_channel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE qrcode_channel;

-- 微信小程序配置表
CREATE TABLE IF NOT EXISTS `wechat_config` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT '小程序名称',
    `app_id` VARCHAR(64) NOT NULL COMMENT '小程序AppID',
    `original_id` VARCHAR(64) COMMENT '小程序原始ID（gh_开头）',
    `app_secret` VARCHAR(128) NOT NULL COMMENT '小程序AppSecret',
    `page_path` VARCHAR(256) NOT NULL COMMENT '落地页路径',
    `default_env_version` ENUM('release', 'trial', 'develop') NOT NULL DEFAULT 'release' COMMENT '默认环境版本',
    `is_active` INT NOT NULL DEFAULT 0 COMMENT '是否激活',
    `access_token` TEXT COMMENT 'access_token缓存',
    `access_token_expires_at` BIGINT COMMENT 'access_token过期时间戳',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_app_id` (`app_id`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信小程序配置表';

-- 二维码记录表
CREATE TABLE IF NOT EXISTS `qrcode_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `config_id` INT COMMENT '关联的小程序配置ID',
    `app_id` VARCHAR(64) COMMENT '小程序AppID',
    `env_version` ENUM('release', 'trial', 'develop') DEFAULT 'release' COMMENT '环境版本',
    `store_id` VARCHAR(64) NOT NULL COMMENT '门店ID',
    `store_name` VARCHAR(128) COMMENT '门店名称',
    `staff_id` VARCHAR(64) NOT NULL COMMENT '服务人员ID',
    `staff_name` VARCHAR(128) COMMENT '服务人员名称',
    `url_link` TEXT COMMENT '微信URL Link',
    `jump_page_url` TEXT COMMENT 'H5跳转页面URL',
    `campaign_id` INT COMMENT '关联的活动ID',
    `qrcode_url` TEXT COMMENT '二维码图片URL',
    `scan_count` INT NOT NULL DEFAULT 0 COMMENT '扫码次数',
    `register_count` INT NOT NULL DEFAULT 0 COMMENT '注册次数',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_staff_id` (`staff_id`),
    INDEX `idx_campaign_id` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码记录表';

-- 扫码记录表
CREATE TABLE IF NOT EXISTS `scan_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `scan_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '扫码记录唯一ID',
    `qrcode_id` INT NOT NULL COMMENT '关联的二维码ID',
    `config_id` INT COMMENT '关联的小程序配置ID',
    `app_id` VARCHAR(64) COMMENT '小程序AppID',
    `store_id` VARCHAR(64) NOT NULL COMMENT '门店ID',
    `store_name` VARCHAR(128) COMMENT '门店名称',
    `staff_id` VARCHAR(64) NOT NULL COMMENT '服务人员ID',
    `staff_name` VARCHAR(128) COMMENT '服务人员名称',
    `campaign_id` INT COMMENT '关联的活动ID',
    `campaign_name` VARCHAR(128) COMMENT '活动名称',
    `user_open_id` VARCHAR(64) COMMENT '扫码用户OpenID',
    `user_nickname` VARCHAR(128) COMMENT '用户昵称',
    `user_phone` VARCHAR(20) COMMENT '用户手机号',
    `is_registered` INT NOT NULL DEFAULT 0 COMMENT '是否已注册',
    `registered_user_id` VARCHAR(64) COMMENT '注册用户ID',
    `registered_at` TIMESTAMP COMMENT '注册时间',
    `ip_address` VARCHAR(64) COMMENT 'IP地址',
    `user_agent` TEXT COMMENT 'User Agent',
    `scan_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '扫码时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_scan_id` (`scan_id`),
    INDEX `idx_qrcode_id` (`qrcode_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_staff_id` (`staff_id`),
    INDEX `idx_scan_time` (`scan_time`),
    INDEX `idx_is_registered` (`is_registered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫码记录表';

-- 活动配置表
CREATE TABLE IF NOT EXISTS `campaigns` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT '活动名称',
    `description` TEXT COMMENT '活动描述',
    `status` ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '活动状态',
    `theme_color` VARCHAR(16) NOT NULL DEFAULT '#1890ff' COMMENT '主题色',
    `background_color` VARCHAR(16) NOT NULL DEFAULT '#f5f5f5' COMMENT '背景色',
    `title` VARCHAR(128) NOT NULL DEFAULT '欢迎扫码' COMMENT '标题文字',
    `subtitle` VARCHAR(256) COMMENT '副标题文字',
    `button_text` VARCHAR(64) NOT NULL DEFAULT '立即进入小程序' COMMENT '按钮文字',
    `logo_url` TEXT COMMENT 'Logo图片URL',
    `background_image_url` TEXT COMMENT '背景图片URL',
    `start_time` TIMESTAMP COMMENT '活动开始时间',
    `end_time` TIMESTAMP COMMENT '活动结束时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_status` (`status`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动配置表';

-- 插入测试数据（可选）
-- INSERT INTO `wechat_config` (`name`, `app_id`, `app_secret`, `page_path`, `is_active`) 
-- VALUES ('测试小程序', 'wx1234567890', 'secret123456', 'pages/index/index', 1);
