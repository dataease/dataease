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
INSERT INTO `core_sys_startup_job` VALUES ('chartFilterMerge', 'chartFilterMerge', 'ready');
COMMIT;


DROP TABLE IF EXISTS `core_export_task`;
CREATE TABLE `core_export_task`
(
    `id`                  VARCHAR(255) NOT NULL,
    `user_id`             BIGINT(20)   NOT NULL,
    `file_name`           VARCHAR(2048) DEFAULT NULL,
    `file_size`           DOUBLE        DEFAULT NULL,
    `file_size_unit`      VARCHAR(255)  DEFAULT NULL,
    `export_from`         VARCHAR(255)  DEFAULT NULL,
    `export_status`       VARCHAR(255)  DEFAULT NULL,
    `export_from_type`    VARCHAR(255)  DEFAULT NULL,
    `export_time`         BIGINT(20)    DEFAULT NULL,
    `export_progress`     VARCHAR(255)  DEFAULT NULL,
    `export_machine_name` VARCHAR(512)  DEFAULT NULL,
    `params`              longtext      NOT NULL COMMENT '过滤参数',
    PRIMARY KEY (`id`)
) COMMENT='导出任务表';

UPDATE `QRTZ_JOB_DETAILS` SET `JOB_CLASS_NAME` = 'io.dataease.job.schedule.CheckDsStatusJob' WHERE (`SCHED_NAME` = 'deSyncJob') and (`JOB_NAME` = 'Datasource') and (`JOB_GROUP` = 'check_status');
