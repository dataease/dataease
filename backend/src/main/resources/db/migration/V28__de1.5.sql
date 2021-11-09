-- ----------------------------
-- 新增我分享出去的功能
-- ----------------------------
ALTER TABLE `dataease`.`panel_share`
ADD COLUMN `granter` varchar(255) NULL COMMENT '分享人' AFTER `target_id`;