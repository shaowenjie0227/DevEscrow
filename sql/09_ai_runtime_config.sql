CREATE TABLE IF NOT EXISTS `ai_runtime_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `enabled` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 关闭 1 启用',
  `provider` varchar(64) NOT NULL COMMENT '模型提供方',
  `base_url` varchar(255) NOT NULL COMMENT '接口基础地址',
  `chat_path` varchar(255) NOT NULL COMMENT '聊天接口路径',
  `api_key_ciphertext` text NULL COMMENT '加密后的 API Key',
  `model` varchar(120) NOT NULL COMMENT '默认模型名',
  `temperature` decimal(4,2) NOT NULL DEFAULT 0.20 COMMENT '温度参数',
  `top_k` int(11) NOT NULL DEFAULT 6 COMMENT '检索条数',
  `fallback_enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0 不回退 1 允许回退',
  `cache_ttl_seconds` bigint(20) NOT NULL DEFAULT 1800 COMMENT '缓存秒数',
  `connect_timeout_ms` bigint(20) NOT NULL DEFAULT 3000 COMMENT '连接超时毫秒',
  `read_timeout_ms` bigint(20) NOT NULL DEFAULT 20000 COMMENT '读取超时毫秒',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_runtime_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 运行时配置表';

INSERT INTO `ai_runtime_config` (
  `config_key`, `enabled`, `provider`, `base_url`, `chat_path`, `api_key_ciphertext`, `model`,
  `temperature`, `top_k`, `fallback_enabled`, `cache_ttl_seconds`, `connect_timeout_ms`, `read_timeout_ms`
)
SELECT
  'DEFAULT', 1, 'deepseek', 'https://api.deepseek.com', '/v1/chat/completions', NULL, 'deepseek-v4-flash',
  0.20, 6, 1, 1800, 3000, 20000
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_runtime_config` WHERE `config_key` = 'DEFAULT'
);
