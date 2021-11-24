-- ----------------------------
-- 新增我分享出去的功能
-- ----------------------------
ALTER TABLE `panel_share`
ADD COLUMN `granter` varchar(255) NULL COMMENT '分享人' AFTER `target_id`;


-- ----------------------------
-- Table structure for panel_link_mapping
-- ----------------------------
CREATE TABLE `panel_link_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_id` varchar(255) DEFAULT NULL COMMENT '仪表板ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=1;


-- ----------------------------
-- 策略模式优化发送消息
-- ----------------------------
ALTER TABLE `sys_msg_channel`
ADD COLUMN `service_name` varchar(255) NULL COMMENT '策略名称' AFTER `channel_name`;


UPDATE `sys_msg_channel` SET `service_name` = 'sendStation' WHERE `msg_channel_id` = 1;
INSERT INTO `sys_msg_channel`(`msg_channel_id`, `channel_name`, `service_name`) VALUES (2, 'webmsg.channel_email_msg', 'sendEmail');

-- ----------------------------
-- 新增定时报告
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` VALUES (60, 1, 2, 1, '任务管理', 'sys-task', 'system/task/index', 1001, 'task', 'system-task', b'0', b'0', b'0', '', NULL, NULL, NULL, NULL);
update sys_menu set pid = 60 ,title = '数据同步', icon = 'dataset-task' where menu_id = 58;
COMMIT;


-- ----------------------------
-- Table structure for sys_task
-- ----------------------------
-- DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(255) NOT NULL COMMENT '任务名称',
  `task_type` varchar(100) NOT NULL COMMENT '任务类型',
  `start_time` bigint(13) DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint(13) DEFAULT NULL COMMENT '结束时间',
  `rate_type` int(10) NOT NULL COMMENT '频率方式',
  `rate_val` varchar(255) DEFAULT NULL COMMENT '频率值',
  `creator` bigint(20) NOT NULL COMMENT '创建者ID',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`task_id`),
  KEY `sys_task_type` (`task_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=1;

-- ----------------------------
-- Table structure for sys_task_instance
-- ----------------------------
-- DROP TABLE IF EXISTS `sys_task_instance`;
CREATE TABLE `sys_task_instance` (
  `instance_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务实例ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `execute_time` bigint(13) DEFAULT NULL COMMENT '执行时间',
  `finish_time` bigint(13) DEFAULT NULL COMMENT '完成时间',
  `status` int(10) DEFAULT NULL COMMENT '实例状态',
  `info` longtext COMMENT '执行信息',
  PRIMARY KEY (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=1;

-- ----------------------------
-- Table structure for sys_task_email
-- ----------------------------
-- DROP TABLE IF EXISTS `sys_task_email`;
CREATE TABLE `sys_task_email` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '邮件模版ID',
  `title` varchar(255) DEFAULT NULL COMMENT '模版标题',
  `panel_id` varchar(255) DEFAULT NULL COMMENT '仪表板ID',
  `recipients` varchar(255) DEFAULT NULL COMMENT '收件人',
  `content` blob COMMENT '内容',
  `pixel` varchar(255) DEFAULT NULL COMMENT '像素',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=1;
