UPDATE `my_plugin`
SET `version` = '1.18.1'
where `plugin_id` > 0 and `version` = '1.18.0';

ALTER TABLE `dataset_table_field`
    CHANGE COLUMN `origin_name` `origin_name` LONGTEXT BINARY NOT NULL COMMENT '原始字段名' ;

ALTER TABLE `dataset_table_field`
    CHANGE COLUMN `name` `name` LONGTEXT  BINARY NOT NULL COMMENT '字段名名' ;

ALTER TABLE `datasource`
    CHANGE COLUMN `name` `name` VARCHAR(50) BINARY NOT NULL COMMENT '数据源名称' ;
