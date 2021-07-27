INSERT INTO `sys_menu` VALUES (58, 1, 0, 1, 'i18n_timed_task', 'sys-task-dataset', 'system/task/dataset', 1001,  'task', 'dataset', b'0', b'0', b'0', 'task:read', NULL, NULL, NULL, NULL);

ALTER TABLE `dataset_table_task`
    ADD COLUMN `last_exec_time` BIGINT(13) NULL DEFAULT NULL COMMENT '上次执行时间' AFTER `create_time`,
    ADD COLUMN `status` VARCHAR(50) NULL DEFAULT NULL COMMENT '任务状态' AFTER `last_exec_time`,
    ADD COLUMN `last_exec_status` VARCHAR(50) NULL DEFAULT NULL COMMENT '上次执行结果' AFTER `last_exec_time`;

update dataset_table_task set rate='SIMPLE' where rate='SIMPLE_COMPLETE';

update dataset_table_task set status='Stopped' where rate='SIMPLE';
update dataset_table_task set status='Underway' where rate='CRON';


UPDATE dataset_table_task
SET dataset_table_task.last_exec_time = (SELECT dataset_table_task_log.start_time FROM dataset_table_task_log WHERE dataset_table_task_log.task_id = dataset_table_task.id limit 1);

UPDATE dataset_table_task
SET dataset_table_task.last_exec_status = (SELECT dataset_table_task_log.status FROM dataset_table_task_log WHERE dataset_table_task_log.task_id = dataset_table_task.id limit 1);


ALTER TABLE `dataset_table_task_log` ADD COLUMN `trigger_type` VARCHAR(45) NULL AFTER `create_time`;
ALTER TABLE `dataset_table_task` ADD COLUMN `extra_data` LONGTEXT NULL AFTER `last_exec_status`;

update dataset_table_task_log set trigger_type='Cron';

update dataset_table_task_log set dataset_table_task_log.task_id='初始导入' where dataset_table_task_log.task_id is null;
