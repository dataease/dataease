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
    `rate_value`       int          NOT NULL COMMENT '频率值',
    `rate_time`        bigint       NOT NULL COMMENT '发送时间',
    `resource_type`    varchar(50)  NOT NULL COMMENT '资源类型',
    `resource_id`      bigint       NOT NULL COMMENT '资源ID',
    `chart_type`       varchar(255) NOT NULL COMMENT '图表类型',
    `chart_id`         bigint       NOT NULL COMMENT '图表ID',
    `threshold_rules`  longtext     NOT NULL COMMENT '告警规则',
    `recisetting`      varchar(50)  NOT NULL DEFAULT '0' COMMENT '消息渠道',
    `reci_users`       longtext COMMENT '接收人',
    `reci_roles`       longtext COMMENT '接收角色',
    `reci_emails`      longtext COMMENT '接收邮箱',
    `reci_lark_groups` longtext COMMENT '飞书群聊',
    `reci_webhooks`    longtext COMMENT 'Web hooks',
    `msg_title`        varchar(255) NOT NULL COMMENT '消息标题',
    `msg_type`         int          NOT NULL DEFAULT '0' COMMENT '消息类型',
    `msg_content`      longtext     NOT NULL COMMENT '消息内容',
    `repeat`           tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否重复发送',
    `status`           tinyint(1)   NOT NULL DEFAULT '0' COMMENT '数据状态',
    `creator`          bigint       NOT NULL COMMENT '创建者ID',
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


