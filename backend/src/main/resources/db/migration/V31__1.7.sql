CREATE TABLE `dataset_column_permissions` (
   `id`         varchar(64) NOT NULL COMMENT 'File ID',
   `auth_target_type`  varchar(255) DEFAULT NULL COMMENT '权限类型：组织/角色/用户',
   `auth_target_id`    bigint(20) DEFAULT NULL COMMENT '权限对象ID',
   `dataset_id`    varchar(64) DEFAULT NULL COMMENT '数据集ID',
   `permissions`  longtext DEFAULT NULL COMMENT '权限',
   `update_time` bigint(13) NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (61, 0, 0, 1, '首页', 'wizard', 'wizard/index', 0, '', '/wizard', b'1', b'0', b'0', NULL, NULL, NULL, NULL, 1614915491036);
INSERT INTO `system_parameter` (`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.openHomePage', 'true', 'boolean', 13);

UPDATE `dataset_table_function` SET `desc` = '如果expr等于某个vn，则返回对应位置THEN后面的结果，如果与所有值都不相等，则返回ELSE后面的rn' WHERE `id` = 47;
UPDATE `dataset_table_function` SET `desc` = '如果expr等于某个vn，则返回对应位置THEN后面的结果，如果与所有值都不相等，则返回ELSE后面的rn' WHERE `id` = 96;

UPDATE `chart_view` SET `name` = '2021年GDP同比增长率' WHERE `id` = 'a0058881-b29f-4b5c-911f-7f1480b07eb0';
UPDATE `chart_view` SET `name` = '2021年GDP总值' WHERE `id` = 'f257452d-6fc1-4499-bdce-bd10b3e1c520';


