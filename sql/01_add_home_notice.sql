USE `programmer_escrow`;

CREATE TABLE IF NOT EXISTS `home_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1公告 2活动',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `summary` VARCHAR(255) NOT NULL COMMENT '摘要',
  `target_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转地址',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页公告活动表';

INSERT INTO `home_notice` (`notice_type`, `title`, `summary`, `target_url`, `cover_url`, `sort_order`, `status`)
SELECT * FROM (
  SELECT 1, '平台规则更新说明', '报价前请先完善交付范围、阶段目标与验收方式，减少来回确认成本。', '/publish', NULL, 10, 1
  UNION ALL
  SELECT 2, '五月需求撮合活动', '完成实名认证和技能审核的开发者，可优先展示在活动推荐位。', '/market', NULL, 20, 1
  UNION ALL
  SELECT 1, '管理员可维护首页内容', '轮播图与右侧公告活动都支持在后台独立新增、编辑、上下线。', '/admin/banners', NULL, 30, 1
) AS seed (`notice_type`, `title`, `summary`, `target_url`, `cover_url`, `sort_order`, `status`)
WHERE NOT EXISTS (
  SELECT 1 FROM `home_notice`
);
