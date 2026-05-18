ALTER TABLE `user_inbox_message`
    ADD COLUMN `action_url` varchar(255) DEFAULT NULL COMMENT '站内通知跳转地址' AFTER `content`;
