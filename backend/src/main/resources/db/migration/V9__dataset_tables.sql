DROP TABLE IF EXISTS `dataset_table`;
CREATE TABLE IF NOT EXISTS `dataset_table`
(
    `id`             varchar(50) NOT NULL COMMENT 'ID',
    `name`           varchar(64) NOT NULL COMMENT '表名称',
    `scene_id`       varchar(50) NOT NULL COMMENT '场景ID',
    `data_source_id` varchar(50) COMMENT '数据源ID',
    `type`           varchar(50) COMMENT 'db,sql,excel,custom',
    `mode`           int(10) DEFAULT 0 COMMENT '连接模式：0-直连，1-定时同步',
    `info`           longtext COMMENT '表原始信息',
    `create_by`      varchar(50) COMMENT '创建人ID',
    `create_time`    bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_field`;
CREATE TABLE IF NOT EXISTS `dataset_table_field`
(
    `id`             varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`       varchar(50)  NOT NULL COMMENT '表ID',
    `origin_name`    varchar(255) NOT NULL COMMENT '原始名',
    `name`           varchar(255) NOT NULL COMMENT '字段名',
    `type`           varchar(50)  NOT NULL COMMENT '原始字段类型',
    `de_type`        int(10)      NOT NULL COMMENT 'dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值...',
    `checked`        tinyint(1)   NOT NULL DEFAULT true COMMENT '是否选中',
    `column_index`   int(10)      NOT NULL COMMENT '列位置',
    `last_sync_time` bigint(13) COMMENT '同步时间',
    PRIMARY KEY (`id`),
    KEY `IDX_TABLE_ID` (`table_id`),
    KEY `IDX_DE_TYPE` (`de_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_task`;
CREATE TABLE IF NOT EXISTS `dataset_table_task`
(
    `id`          varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`    varchar(50)  NOT NULL COMMENT '表ID',
    `name`        varchar(255) NOT NULL COMMENT '任务名称',
    `type`        varchar(50)  NOT NULL COMMENT '更新方式：0-全量更新 1-增量更新',
    `start_time`  bigint(13) COMMENT '开始时间',
    `rate`        varchar(50)  NOT NULL COMMENT '执行频率：0 一次性 1 cron',
    `cron`        varchar(255) COMMENT 'cron表达式',
    `end`         varchar(50)  NOT NULL COMMENT '结束限制 0 无限制 1 设定结束时间',
    `end_time`    bigint(13) COMMENT '结束时间',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `IDX_TABLE_ID` (`table_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_task_log`;
CREATE TABLE IF NOT EXISTS `dataset_table_task_log`
(
    `id`          varchar(50) NOT NULL COMMENT 'ID',
    `table_id`    varchar(50)  NOT NULL COMMENT '表ID',
    `task_id`     varchar(50) NOT NULL COMMENT '任务ID',
    `start_time`  bigint(13) COMMENT '开始时间',
    `end_time`    bigint(13) COMMENT '结束时间',
    `status`      varchar(50) NOT NULL COMMENT '执行状态',
    `info`        varchar(255) COMMENT '执行内容',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `IDX_TABLE_ID` (`task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
