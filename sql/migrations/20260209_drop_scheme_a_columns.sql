-- 仅方案B迁移：删除A方案相关字段
ALTER TABLE `qrcode_records`
    DROP COLUMN `scheme_type`,
    DROP COLUMN `h5_link_type`,
    DROP COLUMN `url_scheme`;

-- 删除无用索引
DROP INDEX `idx_scheme_type` ON `qrcode_records`;
