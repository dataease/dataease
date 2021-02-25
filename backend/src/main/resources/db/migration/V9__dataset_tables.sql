CREATE TABLE IF NOT EXISTS `dataset_table`
(
    `id`             varchar(50) NOT NULL COMMENT 'ID',
    `name`           varchar(64) NOT NULL COMMENT '表名称',
    `scene_id`       varchar(50) NOT NULL COMMENT '场景ID',
    `data_source_id` varchar(50) NOT NULL COMMENT '数据源ID',
    `type`           varchar(50) COMMENT 'db,sql,excel,custom',
    `info`           longtext COMMENT '表原始信息',
    `create_by`      varchar(50) COMMENT '创建人ID',
    `create_time`    bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `dataset_table_field`
(
    `id`             varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`       varchar(50)  NOT NULL COMMENT '表ID',
    `origin_name`    varchar(255) NOT NULL COMMENT '原始名',
    `name`           varchar(255) NOT NULL COMMENT '字段名',
    `type`           varchar(50)  NOT NULL COMMENT '字段类型',
    `checked`        tinyint(1)   NOT NULL DEFAULT true COMMENT '是否选中',
    `column_index`   int(10)      NOT NULL COMMENT '列位置',
    `last_sync_time` bigint(13) COMMENT '同步时间',
    PRIMARY KEY (`id`),
    KEY `IDX_TABLE_ID` (`table_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

