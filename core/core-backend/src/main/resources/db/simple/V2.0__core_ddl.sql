DROP TABLE IF EXISTS `core_datasource`;
CREATE TABLE `core_datasource`
(
    `id`            varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
    `name`          varchar(50) NOT NULL COMMENT '数据源名称',
    `desc`          varchar(50)          DEFAULT NULL COMMENT '描述',
    `type`          varchar(50) NOT NULL COMMENT '类型',
    `configuration` longtext    NOT NULL COMMENT '详细信息',
    `create_time`   bigint      NOT NULL COMMENT 'Create timestamp',
    `update_time`   bigint      NOT NULL COMMENT 'Update timestamp',
    `create_by`     varchar(50)          DEFAULT NULL COMMENT '创建人ID',
    `status`        longtext COMMENT '状态',
    PRIMARY KEY (`id`)
);