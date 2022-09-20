
DROP TABLE IF EXISTS `sys_external_token`;
CREATE TABLE `sys_external_token` (
      `type` int(4) NOT NULL COMMENT '类型ID',
      `token` varchar(255) NOT NULL COMMENT 'token',
      `exp_time` bigint(13) NOT NULL COMMENT '过期时间',
      PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

UPDATE `sys_menu` set `component` = 'system/datasource/DsForm' where `component` = 'system/datasource/form';

INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.dsCheckInterval', 30, 'text', 1);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.dsCheckIntervalType', 'minute', 'text', 1);

CREATE TABLE `task_instance` (
     `task_id` VARCHAR(128) NOT NULL COMMENT '任务ID',
     `execute_time` bigint(13) DEFAULT NULL COMMENT '执行时间',
     `finish_time` bigint(13) DEFAULT NULL COMMENT '完成时间',
     `status` VARCHAR(128) DEFAULT NULL COMMENT '状态',
     `info` longtext COMMENT '执行信息',
     `qrtz_instance` VARCHAR(128) DEFAULT NULL COMMENT '任务实例ID',
     PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `task_instance` (`task_id`) VALUES ('Datasource_check_status');


update sys_menu set menu_sort=10 where menu_id=1;
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (41, 1, 1, 1, '应用管理', 'system-app-template', 'panel/appTemplate/index', 13, 'sys-param', 'panel/appTemplate/index', 0, 0, 0, NULL, NULL, NULL, NULL, 1620444227389);
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (203, 0, 0, 1, '应用市场', 'app-template-market', 'panel/appTemplateMarket/index', 6, 'dashboard', '/appTemplateMarket', 0, 0, 0, NULL, NULL, NULL, NULL, 1620444227389);
-- ----------------------------
-- Table structure for panel_app_template
-- ----------------------------
DROP TABLE IF EXISTS `panel_app_template`;
CREATE TABLE `panel_app_template` (
                                      `id` varchar(50) NOT NULL,
                                      `name` varchar(255) DEFAULT NULL COMMENT '名称',
                                      `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型',
                                      `level` int(8) DEFAULT NULL,
                                      `pid` varchar(255) DEFAULT NULL COMMENT '父级ID',
                                      `version` varchar(255) DEFAULT NULL COMMENT '版本',
                                      `icon` varchar(1000) DEFAULT NULL,
                                      `application_info` longtext COMMENT '应用信息',
                                      `panel_info` longtext COMMENT '仪表板信息',
                                      `panel_views_info` longtext COMMENT '仪表板视图信息',
                                      `chart_views_info` longtext COMMENT '视图信息',
                                      `chart_view_fields_info` longtext COMMENT '视图计算字段信息',
                                      `dataset_tables_info` longtext COMMENT '数据集信息',
                                      `dataset_table_fields_info` longtext COMMENT '数据集字段信息',
                                      `dataset_tasks_info` longtext COMMENT '数据集任务信息',
                                      `datasource_info` longtext COMMENT '数据源信息',
                                      `snapshot` longtext,
                                      `update_time` bigint(13) DEFAULT NULL,
                                      `update_user` varchar(255) DEFAULT NULL,
                                      `create_time` bigint(13) DEFAULT NULL,
                                      `create_user` varchar(255) DEFAULT NULL,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for panel_app_template_log
-- ----------------------------
DROP TABLE IF EXISTS `panel_app_template_log`;
CREATE TABLE `panel_app_template_log` (
                                          `id` varchar(50) NOT NULL,
                                          `app_template_id` varchar(50) DEFAULT NULL COMMENT '应用模板id',
                                          `app_template_name` varchar(255) DEFAULT NULL COMMENT '原仪表板名称',
                                          `datasource_id` varchar(50) DEFAULT NULL COMMENT '数据源ID',
                                          `source_datasource_name` varchar(255) DEFAULT NULL COMMENT '原数据源名称',
                                          `dataset_group_id` varchar(50) DEFAULT NULL COMMENT '数据集分组ID',
                                          `source_dataset_group_name` varchar(255) DEFAULT NULL COMMENT '原数据集分组名称',
                                          `panel_id` varchar(50) DEFAULT NULL COMMENT '仪表板ID',
                                          `source_panel_name` varchar(255) DEFAULT NULL COMMENT '原仪表板名称',
                                          `apply_time` bigint(13) DEFAULT NULL COMMENT '应用时间',
                                          `apply_persion` varchar(255) DEFAULT NULL COMMENT '应用人',
                                          `is_success` tinyint(1) DEFAULT '1' COMMENT '是否成功',
                                          `remark` varchar(255) DEFAULT NULL,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `sys_menu` VALUES (800, 0, 0, 1, '数据集表单', 'dataset-form', 'dataset/form', 999, NULL, '/dataset-form', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);

INSERT INTO `sys_msg_channel` VALUES (3, 'webmsg.channel_wecom_msg', 'sendWecom');
INSERT INTO `sys_msg_channel` VALUES (4, 'webmsg.channel_dingtalk_msg', 'sendDingtalk');
INSERT INTO `sys_msg_channel` VALUES (5, 'webmsg.channel_lark_msg', 'sendLark');

UPDATE `dataset_table_function` SET `func` = 'CONCAT(s1,s2,...)' WHERE `id` = 29;
UPDATE `dataset_table_function` SET `func` = 'CONCAT(s1,s2,...)' WHERE `id` = 78;



ALTER TABLE `sys_task_email`
    ADD COLUMN `recisetting` varchar(255) NULL COMMENT '消息渠道' AFTER `view_ids`,
    ADD COLUMN `conditions` longtext NULL COMMENT '仪表板条件' AFTER `recisetting`;



ALTER TABLE `sys_task_email`
    ADD COLUMN `reci_users` varchar(255) NULL COMMENT '接收人账号' AFTER `conditions`;

UPDATE `sys_menu` SET `pid` = 0, `sub_count` = 0, `type` = 1, `title` = '模板市场', `name` = 'template-market', `component` = 'panel/templateMarket/index', `menu_sort` = 5, `icon` = 'dashboard', `path` = '/templateMarket', `i_frame` = 0, `cache` = 0, `hidden` = 0, `permission` = 'template-market:read', `create_by` = NULL, `update_by` = NULL, `create_time` = NULL, `update_time` = 1620444227389 WHERE `menu_id` = 202;

INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`) VALUES ('e6b2cebf-02d8-4d46-833c-56fb07febb0f', '202', 'menu', '2', 'user', 1663661210626, NULL, 'admin', NULL, NULL, NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`) VALUES ('f4e07708-26f1-4f42-9a4a-8e6dae63353c', '202', 'menu', '2', 'role', 1663661388831, NULL, 'admin', NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('2e9f56e5-38bb-11ed-8383-0242ac130005', 'e6b2cebf-02d8-4d46-833c-56fb07febb0f', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1663661211000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('2e9f5ca1-38bb-11ed-8383-0242ac130005', 'e6b2cebf-02d8-4d46-833c-56fb07febb0f', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'admin', 1663661211000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('98d77463-38bb-11ed-8383-0242ac130005', 'f4e07708-26f1-4f42-9a4a-8e6dae63353c', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1663661389000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('98d77856-38bb-11ed-8383-0242ac130005', 'f4e07708-26f1-4f42-9a4a-8e6dae63353c', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1663661389000, NULL, NULL, NULL);
