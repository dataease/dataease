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