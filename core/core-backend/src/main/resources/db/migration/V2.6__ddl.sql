ALTER TABLE `xpack_share`
    ADD COLUMN `auto_pwd` tinyint(1) NOT NULL DEFAULT 1 COMMENT '自动生成密码' AFTER `type`;