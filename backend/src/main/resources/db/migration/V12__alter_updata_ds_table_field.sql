ALTER TABLE `dataset_table_field` ADD COLUMN `group_type` VARCHAR(50) COMMENT '维度/指标标识 d:维度，q:指标' AFTER `dataease_name`;
ALTER TABLE `dataset_table_field` ADD COLUMN `de_type_format` int(10) COMMENT '类型格式' AFTER `de_type`;
ALTER TABLE `dataset_table_field` ADD COLUMN `ext_field` int(10) COMMENT '是否扩展字段 0否 1是' AFTER `de_extract_type`;

UPDATE `dataset_table_field` SET group_type='d' where `de_type` IN (0,1);
UPDATE `dataset_table_field` SET group_type='q' where `de_type` IN (2,3);
UPDATE `dataset_table_field` SET ext_field=0;