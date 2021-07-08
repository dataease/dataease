INSERT INTO `sys_menu` VALUES (57, 1, 3, 1, '任务管理', 'sys-task', 'system/task/index', 2000,  'task', 'system-task', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (58, 57, 0, 1, '数据集任务', 'sys-task-dataset', 'system/task/dataset', 1,  'task', 'dataset', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
ALTER TABLE `dataset_table_task`
    ADD COLUMN `last_exec_time` BIGINT(13) NULL DEFAULT NULL COMMENT '上次执行时间' AFTER `create_time`,
    ADD COLUMN `status` VARCHAR(50) NULL DEFAULT NULL COMMENT '任务状态' AFTER `last_exec_time`,
    ADD COLUMN `last_exec_status` VARCHAR(50) NULL DEFAULT NULL COMMENT '上次执行结果' AFTER `last_exec_time`;

update dataset_table_task set status='Underway' where rate='CRON';
update dataset_table_task set status='Stopped' where rate='SIMPLE_COMPLETE';


ALTER TABLE `dataset_table_task_log` ADD COLUMN `trigger_type` VARCHAR(45) NULL AFTER `create_time`;


