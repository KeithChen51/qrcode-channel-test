-- Backfill migration for environments where campaigns table was not created.
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
