ALTER TABLE `dataset_table_field` ADD COLUMN `dataease_name`  varchar(255) NOT NULL COMMENT '字段名'  AFTER `name`;
ALTER TABLE dataset_table_task_log CHANGE COLUMN `task_id` `task_id` VARCHAR(50) NULL COMMENT '任务ID' ;
ALTER TABLE `dataset_table_task_log` CHANGE COLUMN `info` `info` LONGTEXT NULL DEFAULT NULL COMMENT '错误信息' ;
