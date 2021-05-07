DROP TABLE IF EXISTS `dataset_table_union`;
CREATE TABLE IF NOT EXISTS `dataset_table_union`
(
    `id`                    varchar(50) NOT NULL COMMENT 'ID',
    `source_table_id`       varchar(50) COMMENT '关联表ID',
    `source_table_field_id` varchar(50) COMMENT '关联字段ID',
    `source_union_relation` varchar(50) COMMENT '关联关系,1:1、1:N、N:1',
    `target_table_id`       varchar(50) COMMENT '被关联表ID',
    `target_table_field_id` varchar(50) COMMENT '被关联字段ID',
    `target_union_relation` varchar(50) COMMENT '被关联关系,1:1、1:N、N:1',
    `create_by`             varchar(50) COMMENT '创建人ID',
    `create_time`           bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;