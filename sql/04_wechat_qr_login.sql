USE `programmer_escrow`;

ALTER TABLE `user`
  ADD COLUMN `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信公众号openid' AFTER `user_no`,
  ADD UNIQUE KEY `uk_openid` (`openid`);

CREATE TABLE IF NOT EXISTS `login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `login_type` VARCHAR(32) NOT NULL COMMENT '登录方式',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `login_time`),
  CONSTRAINT `fk_login_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
