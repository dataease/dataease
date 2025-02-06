ALTER TABLE `core_dataset_table_field`
    ADD COLUMN `groups` longtext NULL COMMENT '分组设置' AFTER `field_short_name`;

ALTER TABLE `core_dataset_table_field`
    ADD COLUMN `other_group` longtext NULL COMMENT '未分组的值' AFTER `groups`;
