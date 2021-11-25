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


DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS select `sys_user`.`user_id` AS `id`,`sys_user`.`username` AS `name`,`sys_user`.`username` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'user' AS `model_type`,'user' AS `model_inner_type`,'target' AS `auth_type`,`sys_user`.`create_by` AS `create_by`,0 AS `level` from `sys_user` where (`sys_user`.`is_admin` <> 1) union all select `sys_role`.`role_id` AS `id`,`sys_role`.`name` AS `name`,`sys_role`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'role' AS `model_type`,'role' AS `model_inner_type`,'target' AS `auth_type`,`sys_role`.`create_by` AS `create_by`,0 AS `level` from `sys_role` union all select `sys_dept`.`dept_id` AS `id`,`sys_dept`.`name` AS `name`,`sys_dept`.`name` AS `lable`,cast(`sys_dept`.`pid` as char charset utf8mb4) AS `pid`,if((`sys_dept`.`sub_count` = 0),'leaf','spine') AS `node_type`,'dept' AS `model_type`,'dept' AS `model_inner_type`,'target' AS `auth_type`,`sys_dept`.`create_by` AS `create_by`,0 AS `level` from `sys_dept` union all select `datasource`.`id` AS `id`,`datasource`.`name` AS `NAME`,`datasource`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'link' AS `model_type`,`datasource`.`type` AS `model_inner_type`,'source' AS `auth_type`,`datasource`.`create_by` AS `create_by`,0 AS `level` from `datasource` union all select `dataset_group`.`id` AS `id`,`dataset_group`.`name` AS `NAME`,`dataset_group`.`name` AS `lable`,if(isnull(`dataset_group`.`pid`),'0',`dataset_group`.`pid`) AS `pid`,'spine' AS `node_type`,'dataset' AS `model_type`,`dataset_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_group`.`create_by` AS `create_by`,`dataset_group`.`level` AS `level` from `dataset_group` union all select `dataset_table`.`id` AS `id`,`dataset_table`.`name` AS `NAME`,`dataset_table`.`name` AS `lable`,`dataset_table`.`scene_id` AS `pid`,'leaf' AS `node_type`,'dataset' AS `model_type`,`dataset_table`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_table`.`create_by` AS `create_by`,0 AS `level` from `dataset_table` union all select `chart_group`.`id` AS `id`,`chart_group`.`name` AS `name`,`chart_group`.`name` AS `label`,if(isnull(`chart_group`.`pid`),'0',`chart_group`.`pid`) AS `pid`,'spine' AS `node_type`,'chart' AS `model_type`,`chart_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_group`.`create_by` AS `create_by`,0 AS `level` from `chart_group` union all select `chart_view`.`id` AS `id`,`chart_view`.`name` AS `name`,`chart_view`.`name` AS `label`,`chart_view`.`scene_id` AS `pid`,'leaf' AS `node_type`,'chart' AS `model_type`,`chart_view`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_view`.`create_by` AS `create_by`,0 AS `level` from `chart_view` union all select `panel_group`.`id` AS `id`,`panel_group`.`name` AS `NAME`,`panel_group`.`name` AS `label`,(case `panel_group`.`id` when 'panel_list' then '0' when 'default_panel' then '0' else `panel_group`.`pid` end) AS `pid`,if((`panel_group`.`node_type` = 'folder'),'spine','leaf') AS `node_type`,'panel' AS `model_type`,`panel_group`.`panel_type` AS `model_inner_type`,'source' AS `auth_type`,`panel_group`.`create_by` AS `create_by`,0 AS `level` from `panel_group` union all select `sys_menu`.`menu_id` AS `menu_id`,`sys_menu`.`title` AS `name`,`sys_menu`.`title` AS `label`,`sys_menu`.`pid` AS `pid`,if((`sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`sys_menu`.`create_by` AS `create_by`,0 AS `level` from `sys_menu` where ((`sys_menu`.`i_frame` <> 1) or isnull(`sys_menu`.`i_frame`)) union all select `plugin_sys_menu`.`menu_id` AS `menu_id`,`plugin_sys_menu`.`title` AS `name`,`plugin_sys_menu`.`title` AS `label`,`plugin_sys_menu`.`pid` AS `pid`,if((`plugin_sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `plugin_sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`plugin_sys_menu`.`create_by` AS `create_by`,0 AS `level` from `plugin_sys_menu` where ((`plugin_sys_menu`.`i_frame` <> 1) or isnull(`plugin_sys_menu`.`i_frame`));
