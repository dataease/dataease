UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'SysParam', 'sysParam')
where (`component` like '%SysParam%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'privateForm', 'PrivateForm')
where (`component` like '%privateForm%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'personPwd', 'PersonPwd')
where (`component` like '%personPwd%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'dataset', 'Dataset')
where (`component` = 'system/task/dataset');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'form', 'Form')
where (`component` = 'system/task/form');

ALTER TABLE `dataset_table_field`
    ADD COLUMN `date_format` VARCHAR(255) NULL AFTER `accuracy`;

ALTER TABLE `sys_task_email`
    ADD COLUMN `view_data_range` VARCHAR(255) NULL DEFAULT 'view' AFTER `reci_users`;


UPDATE `sys_msg_type`
set `type_name` = 'i18n_msg_type_dataset_sync_failed'
WHERE (`msg_type_id` = 6);

ALTER TABLE `sys_user_assist`
    ADD COLUMN `larksuite_id` VARCHAR(255) NULL DEFAULT NULL AFTER `lark_id`;

ALTER TABLE `dataset_table_field` CHANGE COLUMN `type` `type` VARCHAR(255) NOT NULL COMMENT '原始字段类型' ;

INSERT INTO `my_plugin` (`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `creator`, `load_mybatis`,
                         `install_time`, `module_name`, `ds_type`)
VALUES ('Apache Kylin 数据源插件', 'default', '0', '0', 'datasource', 'Apache Kylin 数据源插件', '1.0-SNAPSHOT', 'DATAEASE', '0',
        '1650765903630', 'kylin-backend', 'kylin');



INSERT INTO `sys_msg_channel` (`msg_channel_id`, `channel_name`, `service_name`) VALUES ('6', 'webmsg.channel_larksuite_msg', 'sendLarksuite');

CREATE TABLE `dataset_sql_log` (
       `id` varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
       `dataset_id` varchar(50) NOT NULL DEFAULT '' COMMENT '数据集ID',
       `start_time` bigint(13) DEFAULT NULL COMMENT '开始时间',
       `end_time` bigint(13) DEFAULT NULL COMMENT '结束时间',
       `spend` bigint(13) DEFAULT NULL COMMENT '耗时(毫秒)',
       `sql` longtext NOT NULL COMMENT '详细信息',
       `status` varchar(45) DEFAULT NULL COMMENT '状态',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
