CREATE DATABASE IF NOT EXISTS `programmer_escrow`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `programmer_escrow`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `login_log`;
DROP TABLE IF EXISTS `community_reply`;
DROP TABLE IF EXISTS `community_post`;
DROP TABLE IF EXISTS `resource_post`;
DROP TABLE IF EXISTS `knowledge_base`;
DROP TABLE IF EXISTS `home_notice`;
DROP TABLE IF EXISTS `home_banner`;
DROP TABLE IF EXISTS `skill_tag`;
DROP TABLE IF EXISTS `demand_category`;
DROP TABLE IF EXISTS `dispute`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `order_stage`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `quote`;
DROP TABLE IF EXISTS `demand`;
DROP TABLE IF EXISTS `admin_user`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_no` VARCHAR(32) NOT NULL COMMENT '用户编号',
  `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信公众号openid',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `user_type` TINYINT NOT NULL COMMENT '1普通用户 2开发者(兼容) 3双角色(兼容)',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `id_card_no` VARCHAR(32) DEFAULT NULL COMMENT '身份证号码',
  `id_verify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未认证 1审核中 2已通过 3已拒绝',
  `developer_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未申请 1审核中 2已通过 3已拒绝 4已封禁',
  `skill_tags` VARCHAR(512) DEFAULT NULL COMMENT '技能标签，逗号分隔',
  `developer_role_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0普通用户 1程序开发 2文档撰写 3都能做',
  `id_card_front_url` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面图片',
  `id_card_back_url` VARCHAR(255) DEFAULT NULL COMMENT '身份证反面图片',
  `selfie_url` VARCHAR(255) DEFAULT NULL COMMENT '手持身份证照片',
  `skill_audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未提交 1审核中 2已通过 3已拒绝',
  `skill_audit_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
  `developer_skill_tag_ids` VARCHAR(512) DEFAULT NULL COMMENT '开发者技术栈ID列表',
  `intro` VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
  `rating` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分',
  `completed_order_count` INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 2封禁 3注销',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_user_type_status` (`user_type`, `status`),
  KEY `idx_dev_status` (`developer_status`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台用户表';

CREATE TABLE `admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `role_code` VARCHAR(32) NOT NULL COMMENT 'SUPER_ADMIN/AUDITOR/CS/FINANCE',
  `permission_json` JSON DEFAULT NULL COMMENT '权限集',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 2停用',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role_status` (`role_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE `demand_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求分类表';

CREATE TABLE `skill_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `tag_type` VARCHAR(32) NOT NULL DEFAULT 'developer' COMMENT '标签类型',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术栈标签表';

CREATE TABLE `home_banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `subtitle` VARCHAR(255) NOT NULL COMMENT '副标题',
  `button_text` VARCHAR(64) DEFAULT NULL COMMENT '按钮文案',
  `target_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转地址',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播表';

CREATE TABLE `home_notice` (
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

CREATE TABLE `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `intro` VARCHAR(255) NOT NULL COMMENT '简介',
  `tech_name` VARCHAR(64) NOT NULL COMMENT '技术名',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '外链地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

CREATE TABLE `resource_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `intro` VARCHAR(255) NOT NULL COMMENT '简介',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '资源链接',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分享表';

CREATE TABLE `demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `demand_no` VARCHAR(32) NOT NULL COMMENT '需求编号',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `title` VARCHAR(128) NOT NULL COMMENT '需求标题',
  `summary` VARCHAR(255) NOT NULL COMMENT '摘要',
  `detail` TEXT NOT NULL COMMENT '详细描述',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `category` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `budget_min` DECIMAL(12,2) NOT NULL COMMENT '预算下限',
  `budget_max` DECIMAL(12,2) NOT NULL COMMENT '预算上限',
  `expected_days` INT NOT NULL COMMENT '预计工期',
  `delivery_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1单阶段 2多阶段',
  `attachments_json` JSON DEFAULT NULL COMMENT '附件JSON',
  `contact_note` VARCHAR(255) DEFAULT NULL COMMENT '补充说明',
  `quote_count` INT NOT NULL DEFAULT 0 COMMENT '报价数',
  `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2待报价 3报价中 4已选开发者 5进行中 6已完成 7已取消 8已驳回',
  `publish_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_demand_no` (`demand_no`),
  KEY `idx_publisher_status` (`publisher_id`, `status`),
  KEY `idx_review_status` (`review_status`, `status`),
  KEY `idx_market` (`status`, `category`, `created_at`),
  CONSTRAINT `fk_demand_publisher_id` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

CREATE TABLE `quote` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_no` VARCHAR(32) NOT NULL COMMENT '报价编号',
  `demand_id` BIGINT NOT NULL COMMENT '需求ID',
  `developer_id` BIGINT NOT NULL COMMENT '开发者ID',
  `price_total` DECIMAL(12,2) NOT NULL COMMENT '总报价',
  `estimated_days` INT NOT NULL COMMENT '预计开发天数',
  `tech_solution` TEXT NOT NULL COMMENT '技术方案',
  `delivery_desc` VARCHAR(255) DEFAULT NULL COMMENT '交付说明',
  `attach_json` JSON DEFAULT NULL COMMENT '附件JSON',
  `valid_until` DATETIME DEFAULT NULL COMMENT '报价有效期',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0有效 1已中标 2已落选 3已撤回 4已过期',
  `selected_at` DATETIME DEFAULT NULL COMMENT '中标时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quote_no` (`quote_no`),
  UNIQUE KEY `uk_demand_dev` (`demand_id`, `developer_id`),
  KEY `idx_demand_status` (`demand_id`, `status`, `created_at`),
  KEY `idx_dev_status` (`developer_id`, `status`, `created_at`),
  CONSTRAINT `fk_quote_demand_id` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`),
  CONSTRAINT `fk_quote_developer_id` FOREIGN KEY (`developer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报价表';

CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `demand_id` BIGINT NOT NULL COMMENT '需求ID',
  `quote_id` BIGINT DEFAULT NULL COMMENT '选中的报价ID',
  `client_id` BIGINT NOT NULL COMMENT '甲方用户ID',
  `developer_id` BIGINT DEFAULT NULL COMMENT '乙方开发者ID',
  `order_title` VARCHAR(128) NOT NULL COMMENT '订单标题',
  `amount_total` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `escrow_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '托管金额',
  `platform_fee` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '平台服务费',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未支付 1已托管 2已放款 3已退款 4已分账',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0WAIT_QUOTE 1QUOTED 2PAID 3IN_PROGRESS 4WAIT_ACCEPT 5COMPLETED 6CANCELLED 7DISPUTE',
  `current_stage_no` INT NOT NULL DEFAULT 0 COMMENT '当前阶段号',
  `progress_percent` INT NOT NULL DEFAULT 0 COMMENT '订单进度',
  `selected_at` DATETIME DEFAULT NULL COMMENT '选中报价时间',
  `paid_at` DATETIME DEFAULT NULL COMMENT '托管支付时间',
  `started_at` DATETIME DEFAULT NULL COMMENT '开始开发时间',
  `submitted_at` DATETIME DEFAULT NULL COMMENT '提交交付时间',
  `accepted_at` DATETIME DEFAULT NULL COMMENT '验收通过时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancelled_at` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_demand_id` (`demand_id`),
  KEY `idx_client_status` (`client_id`, `status`, `updated_at`),
  KEY `idx_developer_status` (`developer_id`, `status`, `updated_at`),
  KEY `idx_status_time` (`status`, `updated_at`),
  CONSTRAINT `fk_orders_demand_id` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`),
  CONSTRAINT `fk_orders_quote_id` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`),
  CONSTRAINT `fk_orders_client_id` FOREIGN KEY (`client_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_orders_developer_id` FOREIGN KEY (`developer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE `order_stage` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `stage_no` INT NOT NULL COMMENT '阶段序号',
  `stage_name` VARCHAR(64) NOT NULL COMMENT '阶段名称',
  `stage_desc` VARCHAR(255) DEFAULT NULL COMMENT '阶段说明',
  `stage_amount` DECIMAL(12,2) NOT NULL COMMENT '阶段金额',
  `plan_start_at` DATETIME DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_at` DATETIME DEFAULT NULL COMMENT '计划结束时间',
  `submit_content` TEXT DEFAULT NULL COMMENT '提交说明',
  `deliverable_json` JSON DEFAULT NULL COMMENT '交付物JSON',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待开始 1进行中 2已提交 3已验收 4已打回 5已取消',
  `actual_start_at` DATETIME DEFAULT NULL COMMENT '实际开始时间',
  `actual_submit_at` DATETIME DEFAULT NULL COMMENT '实际提交时间',
  `actual_accept_at` DATETIME DEFAULT NULL COMMENT '实际验收时间',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '打回原因',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_stage` (`order_id`, `stage_no`),
  KEY `idx_order_status` (`order_id`, `status`),
  CONSTRAINT `fk_order_stage_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单阶段表';

CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` TINYINT NOT NULL COMMENT '1需求沟通 2订单沟通',
  `demand_id` BIGINT DEFAULT NULL COMMENT '需求ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
  `msg_type` TINYINT NOT NULL COMMENT '1文本 2图片 3文件 4系统',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `file_url` VARCHAR(255) DEFAULT NULL COMMENT '文件地址',
  `extra_json` JSON DEFAULT NULL COMMENT '扩展字段',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 2撤回 3删除',
  `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_time` (`order_id`, `created_at`),
  KEY `idx_demand_time` (`demand_id`, `created_at`),
  KEY `idx_receiver_unread` (`receiver_id`, `read_status`, `created_at`),
  CONSTRAINT `fk_chat_message_demand_id` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`),
  CONSTRAINT `fk_chat_message_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_chat_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_chat_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

CREATE TABLE `dispute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dispute_no` VARCHAR(32) NOT NULL COMMENT '纠纷编号',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `initiator_id` BIGINT NOT NULL COMMENT '发起人ID',
  `respondent_id` BIGINT NOT NULL COMMENT '被申诉人ID',
  `admin_id` BIGINT DEFAULT NULL COMMENT '处理管理员ID',
  `dispute_type` TINYINT NOT NULL COMMENT '1延期 2质量不达标 3未交付 4付款争议 5违规沟通',
  `reason` VARCHAR(255) NOT NULL COMMENT '纠纷原因',
  `detail` TEXT NOT NULL COMMENT '详细说明',
  `evidence_json` JSON DEFAULT NULL COMMENT '证据JSON',
  `freeze_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待受理 1处理中 2待举证 3已裁决 4已驳回 5已关闭',
  `result_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0无 1退款给甲方 2放款给乙方 3部分分账 4继续履约',
  `resolution_note` VARCHAR(500) DEFAULT NULL COMMENT '处理说明',
  `resolved_at` DATETIME DEFAULT NULL COMMENT '裁决时间',
  `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dispute_no` (`dispute_no`),
  KEY `idx_order_status` (`order_id`, `status`),
  KEY `idx_admin_status` (`admin_id`, `status`),
  KEY `idx_initiator_status` (`initiator_id`, `status`),
  CONSTRAINT `fk_dispute_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_dispute_initiator_id` FOREIGN KEY (`initiator_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_dispute_respondent_id` FOREIGN KEY (`respondent_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_dispute_admin_id` FOREIGN KEY (`admin_id`) REFERENCES `admin_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纠纷表';

CREATE TABLE `community_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creator_id` BIGINT NOT NULL COMMENT '发帖人ID',
  `author_name` VARCHAR(64) NOT NULL COMMENT '作者名',
  `forum_name` VARCHAR(64) NOT NULL COMMENT '版块名',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `summary` VARCHAR(255) NOT NULL COMMENT '摘要',
  `content` TEXT NOT NULL COMMENT '正文',
  `reply_count` INT NOT NULL DEFAULT 0 COMMENT '回复数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 2下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_forum_status` (`forum_name`, `status`, `created_at`),
  KEY `idx_creator_status` (`creator_id`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

CREATE TABLE `community_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `creator_id` BIGINT NOT NULL COMMENT '回复人ID',
  `author_name` VARCHAR(64) NOT NULL COMMENT '作者名',
  `content` TEXT NOT NULL COMMENT '回复内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 2下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_status` (`post_id`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区回复表';

CREATE TABLE `login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `login_type` VARCHAR(32) NOT NULL COMMENT '登录方式',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `login_time`),
  CONSTRAINT `fk_login_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

INSERT INTO `admin_user` (`username`, `password_hash`, `real_name`, `role_code`, `status`)
VALUES ('admin', '$2a$10$replace.with.bcrypt.password', '默认管理员', 'SUPER_ADMIN', 1);

INSERT INTO `demand_category` (`category_name`, `sort_order`, `status`, `description`) VALUES
('Java 后端', 10, 1, '适合 Java、Spring Boot、微服务相关开发'),
('前端开发', 20, 1, '适合 Vue、React、小程序和活动页开发'),
('移动端开发', 30, 1, '适合 iOS、Android、Flutter、UniApp 开发'),
('产品原型/UI', 40, 1, '适合原型设计、UI 设计、交互稿制作');

INSERT INTO `skill_tag` (`tag_name`, `tag_type`, `sort_order`, `status`) VALUES
('Vue3', 'developer', 10, 1),
('React', 'developer', 20, 1),
('Spring Boot', 'developer', 30, 1),
('Java', 'developer', 40, 1),
('MySQL', 'developer', 50, 1),
('UniApp', 'developer', 60, 1),
('Flutter', 'developer', 70, 1),
('Node.js', 'developer', 80, 1);

INSERT INTO `home_banner` (`title`, `subtitle`, `button_text`, `target_url`, `image_url`, `sort_order`, `status`) VALUES
('先开发 Demo，满意再合作', '发布真实需求，快速找到能接单的程序员。', '立即发布', '/publish', NULL, 10, 1),
('实名审核开发者，接单更安心', '开发者通过资料审核后才能报价接单，降低合作风险。', '查看需求', '/market', NULL, 20, 1);

INSERT INTO `home_notice` (`notice_type`, `title`, `summary`, `target_url`, `cover_url`, `sort_order`, `status`) VALUES
(1, '平台规则更新说明', '报价前请先完善交付范围、阶段目标与验收方式，减少来回确认成本。', '/publish', NULL, 10, 1),
(2, '五月需求撮合活动', '完成实名认证和技能审核的开发者，可优先展示在活动推荐位。', '/market', NULL, 20, 1),
(1, '管理员可维护首页内容', '轮播图与右侧公告活动都支持在后台独立新增、编辑、上下线。', '/admin/banners', NULL, 30, 1);

INSERT INTO `knowledge_base` (`title`, `intro`, `tech_name`, `cover_url`, `link_url`, `sort_order`, `status`) VALUES
('Vue3 能做什么', '适合后台、官网、活动页和中小型产品前端。', 'Vue3', NULL, NULL, 10, 1),
('Spring Boot 项目交付建议', '适合服务端 API、后台管理系统和中后台业务。', 'Spring Boot', NULL, NULL, 20, 1);

INSERT INTO `resource_post` (`title`, `intro`, `cover_url`, `link_url`, `sort_order`, `status`, `like_count`, `favorite_count`, `share_count`, `creator_id`) VALUES
('Vue3 后台模板资源包', '适合快速起一个 Demo 或管理后台项目。', NULL, 'https://example.com/vue3-admin', 10, 1, 0, 0, 0, NULL),
('Spring Boot 脚手架模板', '适合快速起一个 API 服务或业务中台。', NULL, 'https://example.com/springboot-starter', 20, 1, 0, 0, 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;
