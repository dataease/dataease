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