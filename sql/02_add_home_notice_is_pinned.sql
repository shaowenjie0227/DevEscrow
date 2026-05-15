USE `programmer_escrow`;

SET @column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'home_notice'
    AND COLUMN_NAME = 'is_pinned'
);

SET @alter_sql := IF(
  @column_exists = 0,
  'ALTER TABLE `home_notice` ADD COLUMN `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT ''1置顶 0普通'' AFTER `cover_url`',
  'SELECT 1'
);

PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `home_notice`
SET `is_pinned` = 1
WHERE `notice_type` = 2
  AND `title` = '五月需求撮合活动'
  AND `status` = 1;
