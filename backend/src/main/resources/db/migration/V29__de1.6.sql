CREATE TABLE `dataset_row_permissions` (
   `id`         varchar(64) NOT NULL COMMENT 'File ID',
   `auth_target_type`  varchar(255) DEFAULT NULL COMMENT '权限类型：组织/角色/用户',
   `auth_target_id`    bigint(20) DEFAULT NULL COMMENT '权限对象ID',
   `dataset_id`    varchar(64) DEFAULT NULL COMMENT '数据集ID',
   `dataset_field_id`    varchar(64) DEFAULT NULL COMMENT '数据集字段ID',
   `logic`    varchar(64) DEFAULT NULL COMMENT '与/或',
   `filter`  longtext DEFAULT NULL COMMENT '数值',
   `update_time` bigint(13) NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;


ALTER TABLE `panel_link` ADD COLUMN `user_id` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `panel_link_mapping` ADD COLUMN `user_id` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `panel_link`CHANGE COLUMN `user_id` `user_id` BIGINT(20) NOT NULL ,DROP PRIMARY KEY;

ALTER TABLE `panel_group`
ADD COLUMN `mobile_layout` tinyint(1) NULL DEFAULT 0 COMMENT '启用移动端布局' AFTER `remark`;

