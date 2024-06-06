alter table `core_chart_view`
    add aggregate bit null comment '区间条形图开启时间纬度开启聚合';

DROP TABLE IF EXISTS `core_sys_startup_job`;
CREATE TABLE `core_sys_startup_job`
(
    `id`     varchar(64) NOT NULL COMMENT 'ID',
    `name`   varchar(255) DEFAULT NULL COMMENT '任务名称',
    `status` varchar(255) DEFAULT NULL COMMENT '任务状态',
    PRIMARY KEY (`id`)
) COMMENT ='项目启动任务';

BEGIN;
INSERT INTO `core_sys_startup_job`
VALUES ('chartFilterMerge', 'chartFilterMerge', 'ready');
COMMIT;

TRUNCATE TABLE `xpack_setting_authentication`;
ALTER TABLE `xpack_setting_authentication`
    ADD COLUMN `plugin_json` longtext NULL COMMENT '插件配置' AFTER `relational_ids`;
ALTER TABLE `xpack_setting_authentication`
    ADD COLUMN `synced` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已同步' AFTER `plugin_json`;
ALTER TABLE `xpack_setting_authentication`
    ADD COLUMN `valid` tinyint(1) NOT NULL DEFAULT 0 COMMENT '有效' AFTER `synced`;


DROP TABLE IF EXISTS `core_export_task`;
CREATE TABLE `core_export_task`
(
    `id`                  varchar(255) NOT NULL,
    `user_id`             bigint(20)   NOT NULL,
    `file_name`           varchar(2048) DEFAULT NULL,
    `file_size`           DOUBLE        DEFAULT NULL,
    `file_size_unit`      varchar(255)  DEFAULT NULL,
    `export_from`         varchar(255)  DEFAULT NULL,
    `export_status`       varchar(255)  DEFAULT NULL,
    `export_from_type`    varchar(255)  DEFAULT NULL,
    `export_time`         bigint(20)    DEFAULT NULL,
    `export_progress`      varchar(255)  DEFAULT NULL,
    `export_machine_name` varchar(512)  DEFAULT NULL,
    `params`              longtext     NOT NULL COMMENT '过滤参数',
    PRIMARY KEY (`id`)
) COMMENT='导出任务表';

DROP TABLE IF EXISTS `xpack_platform_token`;
CREATE TABLE `xpack_platform_token`
(
    `id`          int          NOT NULL,
    `token`       varchar(255) NOT NULL,
    `create_time` bigint       NOT NULL,
    `exp_time`    bigint       NOT NULL,
    PRIMARY KEY (`id`)
);



UPDATE `QRTZ_JOB_DETAILS` SET `JOB_CLASS_NAME` = 'io.dataease.job.schedule.CheckDsStatusJob' WHERE (`SCHED_NAME` = 'deSyncJob') and (`JOB_NAME` = 'Datasource') and (`JOB_GROUP` = 'check_status');
