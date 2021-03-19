DROP TABLE IF EXISTS `dataset_table_incremental_config`;
CREATE TABLE IF NOT EXISTS `dataset_table_incremental_config`
(
    `id`          varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`    varchar(50)  NOT NULL COMMENT '表ID',
    `incremental_delete`  longtext COMMENT '详细信息',
    `incremental_add`  longtext COMMENT '详细信息',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;