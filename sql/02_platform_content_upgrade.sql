USE `programmer_escrow`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'id_card_no'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `id_card_no` VARCHAR(32) DEFAULT NULL COMMENT 'identity card number' AFTER `real_name`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'developer_role_type'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `developer_role_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0 normal 1 developer 2 writer 3 both' AFTER `skill_tags`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'id_card_front_url'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `id_card_front_url` VARCHAR(255) DEFAULT NULL COMMENT 'id card front image' AFTER `developer_role_type`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'id_card_back_url'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `id_card_back_url` VARCHAR(255) DEFAULT NULL COMMENT 'id card back image' AFTER `id_card_front_url`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'selfie_url'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `selfie_url` VARCHAR(255) DEFAULT NULL COMMENT 'selfie image for verification' AFTER `id_card_back_url`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'skill_audit_status'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `skill_audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 not submitted 1 pending 2 approved 3 rejected' AFTER `selfie_url`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'skill_audit_reason'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `skill_audit_reason` VARCHAR(255) DEFAULT NULL COMMENT 'skill audit note' AFTER `skill_audit_status`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'developer_skill_tag_ids'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `user` ADD COLUMN `developer_skill_tag_ids` VARCHAR(512) DEFAULT NULL COMMENT 'developer skill tag id list' AFTER `skill_audit_reason`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'demand'
    AND COLUMN_NAME = 'category_id'
);
SET @sql = IF(
  @exists = 0,
  "ALTER TABLE `demand` ADD COLUMN `category_id` BIGINT DEFAULT NULL AFTER `detail`",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `demand_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(64) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='demand category';

CREATE TABLE IF NOT EXISTS `skill_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tag_name` VARCHAR(64) NOT NULL,
  `tag_type` VARCHAR(32) NOT NULL DEFAULT 'developer',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='skill tag';

CREATE TABLE IF NOT EXISTS `home_banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `subtitle` VARCHAR(255) NOT NULL,
  `button_text` VARCHAR(64) DEFAULT NULL,
  `target_url` VARCHAR(255) DEFAULT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='home banner';

CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `intro` VARCHAR(255) NOT NULL,
  `tech_name` VARCHAR(64) NOT NULL,
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `link_url` VARCHAR(255) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='knowledge base';

CREATE TABLE IF NOT EXISTS `resource_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `intro` VARCHAR(255) NOT NULL,
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `link_url` VARCHAR(255) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `like_count` INT NOT NULL DEFAULT 0,
  `favorite_count` INT NOT NULL DEFAULT 0,
  `share_count` INT NOT NULL DEFAULT 0,
  `creator_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='resource share';

INSERT INTO `demand_category` (`category_name`, `sort_order`, `status`, `description`)
SELECT 'Java 后端', 10, 1, '适合 Java、SpringBoot、微服务相关开发'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `demand_category` WHERE `category_name` = 'Java 后端'
);

INSERT INTO `skill_tag` (`tag_name`, `tag_type`, `sort_order`, `status`)
SELECT 'Vue3', 'developer', 10, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `skill_tag` WHERE `tag_name` = 'Vue3'
);

INSERT INTO `home_banner` (`title`, `subtitle`, `button_text`, `target_url`, `image_url`, `sort_order`, `status`)
SELECT '先开发 Demo，满意再合作', '发布真实需求，快速找到能接单的程序员。', '立即发布', '/publish', NULL, 10, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `home_banner` WHERE `title` = '先开发 Demo，满意再合作'
);

INSERT INTO `knowledge_base` (`title`, `intro`, `tech_name`, `cover_url`, `link_url`, `sort_order`, `status`)
SELECT 'Vue3 能做什么', '适合后台、官网、活动页和中小型产品前端。', 'Vue3', NULL, NULL, 10, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `knowledge_base` WHERE `title` = 'Vue3 能做什么'
);

INSERT INTO `resource_post` (`title`, `intro`, `cover_url`, `link_url`, `sort_order`, `status`, `like_count`, `favorite_count`, `share_count`, `creator_id`)
SELECT 'Vue3 后台模板资源包', '适合快速起一个 Demo 或管理后台项目。', NULL, 'https://example.com', 10, 1, 0, 0, 0, NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `resource_post` WHERE `title` = 'Vue3 后台模板资源包'
);

SET FOREIGN_KEY_CHECKS = 1;
