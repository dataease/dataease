
DROP TABLE IF EXISTS `sys_external_token`;
CREATE TABLE `sys_external_token` (
      `type` int(4) NOT NULL COMMENT '类型ID',
      `token` varchar(255) NOT NULL COMMENT 'token',
      `exp_time` bigint(13) NOT NULL COMMENT '过期时间',
      PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

UPDATE `sys_menu` set `component` = 'system/datasource/DsForm' where `component` = 'system/datasource/form';


INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.dsCheckInterval', 20, 'text', 1);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.dsCheckIntervalType', 'minute', 'text', 1);

CREATE TABLE `task_instance` (
     `task_id` VARCHAR(128) NOT NULL COMMENT '任务ID',
     `execute_time` bigint(13) DEFAULT NULL COMMENT '执行时间',
     `finish_time` bigint(13) DEFAULT NULL COMMENT '完成时间',
     `status` VARCHAR(128) DEFAULT NULL COMMENT '状态',
     `info` longtext COMMENT '执行信息',
     `qrtz_instance` VARCHAR(128) DEFAULT NULL COMMENT '任务实例ID',
     PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `task_instance` (`task_id`) VALUES ('Datasource_check_status');

