BEGIN;
INSERT INTO `core_sys_startup_job`
VALUES ('chartFilterDynamic', 'chartFilterDynamic', 'ready');
COMMIT;

alter table `core_dataset_table_field`
    add params text null comment '计算字段参数';

alter table `core_datasource`
    add `enable_data_fill` tinyint default 0 null comment '启用数据填报功能';

INSERT INTO `core_menu`
VALUES (64, 15, 2, 'font', 'system/font', 10, 'icon_font', '/font', 0, 1, 0);


DROP TABLE IF EXISTS `xpack_threshold_info`;
CREATE TABLE `xpack_threshold_info`
(
    `id`               bigint       NOT NULL,
    `name`             varchar(255) NOT NULL COMMENT '告警名称',
    `enable`           tinyint(1)   NOT NULL COMMENT '是否启用',
    `rate_type`        int          NOT NULL COMMENT '频率类型',
    `rate_value`       varchar(255) NOT NULL COMMENT '频率值',
    `resource_type`    varchar(50)  NOT NULL COMMENT '资源类型',
    `resource_id`      bigint       NOT NULL COMMENT '资源ID',
    `chart_type`       varchar(255) NOT NULL COMMENT '图表类型',
    `chart_id`         bigint       NOT NULL COMMENT '图表ID',
    `threshold_rules`  longtext COMMENT '告警规则',
    `recisetting`      varchar(50)  NOT NULL DEFAULT '0' COMMENT '消息渠道',
    `reci_users`       longtext COMMENT '接收人',
    `reci_roles`       longtext COMMENT '接收角色',
    `reci_emails`      longtext COMMENT '接收邮箱',
    `reci_lark_groups` longtext COMMENT '飞书群聊',
    `reci_webhooks`    longtext COMMENT 'Web hooks',
    `msg_title`        varchar(255) NOT NULL COMMENT '消息标题',
    `msg_type`         int          NOT NULL DEFAULT '0' COMMENT '消息类型',
    `msg_content`      longtext COMMENT '消息内容',
    `repeat_send`      tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否重复发送',
    `status`           tinyint(1)   NOT NULL DEFAULT '0' COMMENT '数据状态',
    `creator`          bigint       NOT NULL COMMENT '创建者ID',
    `creator_name`     varchar(255) NOT NULL COMMENT '创建人名称',
    `create_time`      bigint       NOT NULL COMMENT '创建时间',
    `oid`              bigint       NOT NULL COMMENT '所属组织',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `xpack_threshold_instance`;
CREATE TABLE `xpack_threshold_instance`
(
    `id`        bigint     NOT NULL,
    `task_id`   bigint     NOT NULL COMMENT '阈值信息ID',
    `exec_time` bigint     NOT NULL COMMENT '检测时间',
    `status`    tinyint(1) NOT NULL DEFAULT '0' COMMENT '数据状态',
    `content`   longtext COMMENT '通知内容',
    `msg`       longtext COMMENT '报错信息',
    PRIMARY KEY (`id`)
);


ALTER TABLE `visualization_outer_params_target_view_info`
    MODIFY COLUMN `target_view_id` varchar(50) NULL DEFAULT NULL COMMENT '联动视图ID/联动过滤项ID' ,
    ADD COLUMN `target_ds_id` varchar(50) NULL COMMENT '联动数据集id/联动过滤组件id' ;

alter table `core_chart_view`
    add flow_map_start_name longtext comment '流向地图起点名称field';
alter table `core_chart_view`
    add flow_map_end_name longtext comment '流向地图终点名称field';
alter table `core_chart_view`
    add ext_color longtext comment '颜色维度field';

update visualization_outer_params_target_view_info tvi INNER JOIN core_chart_view ccv on tvi.target_view_id = ccv.id
    set tvi.target_ds_id = ccv.table_id;


DROP TABLE IF EXISTS `core_font`;
CREATE TABLE `core_font`
(
    `id`                bigint       NOT NULL COMMENT 'ID',
    `name`              varchar(255) NOT NULL COMMENT '字体名称',
    `file_name`         varchar(255) default NULL COMMENT '文件名称',
    `file_trans_name`   varchar(255) default NULL COMMENT '文件转换名称',
    `is_default`         tinyint(1)       default 0 COMMENT '是否默认',
    `update_time`        bigint     NOT NULL COMMENT '更新时间',
    `is_BuiltIn`         tinyint(1)       default 0 COMMENT '是否内置',
    PRIMARY KEY (`id`)
);
ALTER TABLE  `core_font` ADD COLUMN `size` DOUBLE NULL AFTER `is_BuiltIn`;
ALTER TABLE  `core_font` ADD COLUMN `size_type` varchar(255) NULL AFTER `size`;
INSERT INTO  `core_font` (`id`, `name`, `is_default`, `update_time`, `is_BuiltIn`) VALUES ('1', 'PingFang', '1', '0', '1');

