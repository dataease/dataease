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
