DROP TABLE IF EXISTS `chart_view_field`;
CREATE TABLE `chart_view_field`
(
    `id`              varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`        varchar(50)  NOT NULL COMMENT '表ID',
    `chart_id`        varchar(50)  NOT NULL COMMENT '视图ID',
    `origin_name`     longtext,
    `name`            varchar(255) NOT NULL COMMENT '字段名',
    `dataease_name`   varchar(255) NOT NULL COMMENT '字段名',
    `group_type`      varchar(50) DEFAULT NULL COMMENT '维度/指标标识 d:维度，q:指标',
    `type`            varchar(50)  NOT NULL COMMENT '原始字段类型',
    `size`            int(11) DEFAULT NULL,
    `de_type`         int(10) NOT NULL COMMENT 'dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值...',
    `de_type_format`  int(10) DEFAULT NULL COMMENT '类型格式',
    `de_extract_type` int(10) NOT NULL,
    `ext_field`       int(10) DEFAULT NULL COMMENT '是否扩展字段 0否 1是',
    `checked`         tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中',
    `column_index`    int(10) NOT NULL COMMENT '列位置',
    `last_sync_time`  bigint(13) DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;
