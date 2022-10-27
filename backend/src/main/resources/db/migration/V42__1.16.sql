UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'SysParam', 'sysParam')
where (`component` like '%SysParam%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'privateForm', 'PrivateForm')
where (`component` like '%privateForm%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'personPwd', 'PersonPwd')
where (`component` like '%personPwd%');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'dataset', 'Dataset')
where (`component` = 'system/task/dataset');

UPDATE `sys_menu`
set `component` = REPLACE(`component`, 'form', 'Form')
where (`component` = 'system/task/form');

ALTER TABLE `dataset_table_field`
    ADD COLUMN `date_format` VARCHAR(255) NULL AFTER `accuracy`;

ALTER TABLE `sys_task_email`
    ADD COLUMN `view_data_range` VARCHAR(255) NULL DEFAULT 'view' AFTER `reci_users`;


UPDATE `sys_msg_type`
set `type_name` = 'i18n_msg_type_dataset_sync_failed'
WHERE (`msg_type_id` = 6);

ALTER TABLE `sys_user_assist`
    ADD COLUMN `larksuite_id` VARCHAR(255) NULL DEFAULT NULL AFTER `lark_id`;

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`,
                        `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`,
                        `update_time`)
VALUES (41, 1, 1, 1, '应用管理', 'system-app-template', 'panel/appTemplate/index', 13, 'sys-param',
        'panel/appTemplate/index', 0, 0, 0, NULL, NULL, NULL, NULL, 1620444227389);
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`,
                        `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`,
                        `update_time`)
VALUES (203, 0, 0, 1, '应用', 'app-template-market', 'panel/appTemplateMarket/index', 6, 'dashboard',
        '/appTemplateMarket', 0, 0, 0, NULL, NULL, NULL, NULL, 1620444227389);

ALTER TABLE `dataset_table_field` CHANGE COLUMN `type` `type` VARCHAR(255) NOT NULL COMMENT '原始字段类型' ;

INSERT INTO `my_plugin` (`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `creator`, `load_mybatis`,
                         `install_time`, `module_name`, `ds_type`)
VALUES ('Apache Kylin 数据源插件', 'default', '0', '0', 'datasource', 'Apache Kylin 数据源插件', '1.0-SNAPSHOT', 'DATAEASE', '0',
        '1650765903630', 'kylin-backend', 'kylin');



INSERT INTO `sys_msg_channel` (`msg_channel_id`, `channel_name`, `service_name`) VALUES ('6', 'webmsg.channel_larksuite_msg', 'sendLarksuite');

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (204, 203, 0, 2, '删除记录', NULL, NULL, 999, NULL, NULL, 0, 0, 0, 'appLog:del', NULL, NULL, 1614930903502, 1614930903502);
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (205, 203, 0, 2, '编辑记录', NULL, NULL, 999, NULL, NULL, 0, 0, 0, 'appLog:edit', NULL, NULL, 1614930935529, 1614930935529);

INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`) VALUES ('46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', '203', 'menu', '1', 'role', 1666840141866, NULL, 'admin', NULL, NULL, NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`) VALUES ('6e22ad53-d737-447f-9686-5041e122b4dc', '205', 'menu', '1', 'role', 1666840141468, NULL, 'admin', NULL, NULL, NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`) VALUES ('da17fcfe-7875-4aaf-983b-d750d71f36d2', '204', 'menu', '1', 'role', 1666840141658, NULL, 'admin', NULL, NULL, NULL);

INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b4fe2e52-55a4-11ed-bf84-0242ac130005', '6e22ad53-d737-447f-9686-5041e122b4dc', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b4fe3215-55a4-11ed-bf84-0242ac130005', '6e22ad53-d737-447f-9686-5041e122b4dc', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b51affbf-55a4-11ed-bf84-0242ac130005', 'da17fcfe-7875-4aaf-983b-d750d71f36d2', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b51b0473-55a4-11ed-bf84-0242ac130005', 'da17fcfe-7875-4aaf-983b-d750d71f36d2', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b53a1ad7-55a4-11ed-bf84-0242ac130005', '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1666840142000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('b53a1dfd-55a4-11ed-bf84-0242ac130005', '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1666840142000, NULL, NULL, NULL);
