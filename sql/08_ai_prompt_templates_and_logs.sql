CREATE TABLE IF NOT EXISTS `ai_prompt_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `scene_code` varchar(64) NOT NULL COMMENT '场景编码',
  `scene_name` varchar(120) NOT NULL COMMENT '场景名称',
  `system_prompt` text NULL COMMENT '系统 Prompt',
  `user_prompt_template` text NULL COMMENT '用户 Prompt 模板',
  `model` varchar(120) NULL COMMENT '模型名',
  `temperature` decimal(4,2) NULL COMMENT '温度参数',
  `top_k` int(11) NOT NULL DEFAULT 6 COMMENT '检索上下文条数',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 启用 2 停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_prompt_scene_code` (`scene_code`),
  KEY `idx_ai_prompt_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Prompt 模板表';

CREATE TABLE IF NOT EXISTS `ai_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `scene_code` varchar(64) NOT NULL COMMENT '场景编码',
  `prompt_version` varchar(160) NULL COMMENT 'Prompt 版本',
  `provider` varchar(64) NULL COMMENT '模型提供方',
  `model` varchar(120) NULL COMMENT '模型名',
  `creator_user_id` bigint(20) NULL COMMENT '触发用户 ID',
  `request_digest` varchar(64) NULL COMMENT '请求摘要哈希',
  `request_preview` varchar(500) NULL COMMENT '请求内容摘要',
  `context_count` int(11) NOT NULL DEFAULT 0 COMMENT '上下文数量',
  `cache_hit` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 未命中 1 命中缓存',
  `fallback_used` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 LLM 结果 1 规则回退',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 成功 2 失败',
  `latency_ms` bigint(20) NULL COMMENT '耗时毫秒',
  `error_message` varchar(500) NULL COMMENT '错误信息',
  `result_preview` text NULL COMMENT '结果摘要',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_call_scene_time` (`scene_code`, `created_at`),
  KEY `idx_ai_call_status_time` (`status`, `created_at`),
  KEY `idx_ai_call_user_time` (`creator_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志表';

INSERT INTO `ai_prompt_template` (
  `scene_code`, `scene_name`, `system_prompt`, `user_prompt_template`, `model`, `temperature`, `top_k`, `status`
)
SELECT
  'DEMAND_DRAFT',
  '需求发布 AI 初稿',
  NULL,
  NULL,
  NULL,
  0.20,
  6,
  1
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_prompt_template` WHERE `scene_code` = 'DEMAND_DRAFT'
);
