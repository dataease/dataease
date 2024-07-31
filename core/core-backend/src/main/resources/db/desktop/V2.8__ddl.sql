ALTER TABLE `core_export_task`
    ADD COLUMN `msg` LONGTEXT NULL COMMENT '错误信息' AFTER `params`;


DROP TABLE IF EXISTS `xpack_plugin`;
CREATE TABLE `xpack_plugin`
(
    `id`              bigint       NOT NULL COMMENT 'ID',
    `name`            varchar(255) NOT NULL COMMENT '插件名称',
    `icon`            longtext     NOT NULL COMMENT '图标',
    `version`         varchar(255) NOT NULL COMMENT '版本',
    `install_time`    bigint       NOT NULL COMMENT '安装时间',
    `flag`            varchar(255) NOT NULL COMMENT '类型',
    `developer`       varchar(255) NOT NULL COMMENT '开发者',
    `config`          longtext     NOT NULL COMMENT '插件配置',
    `require_version` varchar(255) NOT NULL COMMENT 'DE最低版本',
    `module_name`     varchar(255) NOT NULL COMMENT '模块名称',
    `jar_name`        varchar(255) NOT NULL COMMENT 'Jar包名称',
    PRIMARY KEY (`id`)
) COMMENT ='插件表';

ALTER TABLE `xpack_share`
    ADD COLUMN `ticket_require` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'ticket必须' AFTER `auto_pwd`;


DROP TABLE IF EXISTS `core_share_ticket`;
CREATE TABLE `core_share_ticket`
(
    `id`          bigint       NOT NULL COMMENT 'ID',
    `uuid`        varchar(255) NOT NULL COMMENT '分享uuid',
    `ticket`      varchar(255) NOT NULL COMMENT 'ticket',
    `exp`         bigint DEFAULT NULL COMMENT 'ticket有效期',
    `args`        longtext COMMENT 'ticket参数',
    `access_time` bigint DEFAULT NULL COMMENT '首次访问时间',
    PRIMARY KEY (`id`)
) COMMENT ='分享Ticket表';
DROP TABLE IF EXISTS `visualization_report_filter`;
CREATE TABLE `visualization_report_filter` (
                                               `id` bigint NOT NULL COMMENT 'id',
                                               `report_id` bigint DEFAULT NULL COMMENT '定时报告id',
                                               `task_id` bigint DEFAULT NULL COMMENT '任务id',
                                               `resource_id` bigint DEFAULT NULL COMMENT '资源id',
                                               `dv_type` varchar(255) DEFAULT NULL COMMENT '资源类型',
                                               `component_id` bigint DEFAULT NULL COMMENT '组件id',
                                               `filter_id` bigint DEFAULT NULL COMMENT '过滤项id',
                                               `filter_info` longtext COMMENT '过滤组件内容',
                                               `filter_version` int DEFAULT NULL COMMENT '过滤组件版本',
                                               `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                               `create_user` varchar(255) DEFAULT NULL COMMENT '创建人',
                                               PRIMARY KEY (`id`)
);