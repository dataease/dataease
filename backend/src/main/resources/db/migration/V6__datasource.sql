CREATE TABLE `datasource` (
    `id`          varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
    `name`     varchar(50) NOT NULL COMMENT '名称',
    `desc`  varchar(50) NOT NULL COMMENT '描述',
    `type`  varchar(50) NOT NULL COMMENT '类型',
    `configuration`  longtext NOT NULL COMMENT '详细信息',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
