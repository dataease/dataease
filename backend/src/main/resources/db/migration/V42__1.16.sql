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
VALUES (203, 0, 0, 1, '应用市场', 'app-template-market', 'panel/appTemplateMarket/index', 6, 'dashboard',
        '/appTemplateMarket', 0, 0, 0, NULL, NULL, NULL, NULL, 1620444227389);
