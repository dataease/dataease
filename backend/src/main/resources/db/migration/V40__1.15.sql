
DROP TABLE IF EXISTS `sys_external_token`;
CREATE TABLE `sys_external_token` (
      `type` int(4) NOT NULL COMMENT '类型ID',
      `token` varchar(255) NOT NULL COMMENT 'token',
      `exp_time` bigint(13) NOT NULL COMMENT '过期时间',
      PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

UPDATE `sys_menu` set `component` = 'system/datasource/DsForm' where `component` = 'system/datasource/form';


INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (41, 1, 1, 1, '应用管理', 'system-template-app', 'panel/templateApp/index', 13, 'display-setting', 'panel/templateApp/index', 0, 0, 0, 'template:read', NULL, NULL, NULL, 1620444227389);

DROP TABLE IF EXISTS `panel_app_template`;
CREATE TABLE `panel_app_template` (
                                      `id` varchar(50) NOT NULL,
                                      `name` varchar(255) DEFAULT NULL COMMENT '名称',
                                      `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型',
                                      `level` int(8) DEFAULT NULL,
                                      `pid` varchar(255) DEFAULT NULL COMMENT '父级ID',
                                      `version` varchar(255) DEFAULT NULL COMMENT '版本',
                                      `application_info` longtext COMMENT '应用信息',
                                      `panel_info` longtext COMMENT '仪表板信息',
                                      `views_info` longtext COMMENT '视图信息',
                                      `dataset_info` longtext COMMENT '数据集信息',
                                      `dataset_fields_info` longtext COMMENT '数据集字段信息',
                                      `dataset_tasks_info` longtext COMMENT '数据集任务信息',
                                      `datasource_info` longtext COMMENT '数据源信息',
                                      `snapshot` longtext,
                                      `update_time` bigint(13) DEFAULT NULL,
                                      `update_user` varchar(255) DEFAULT NULL,
                                      `create_time` bigint(13) DEFAULT NULL,
                                      `create_user` varchar(255) DEFAULT NULL,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `sys_menu` VALUES (800, 0, 0, 1, '数据集表单', 'dataset-form', 'dataset/form', 999, NULL, '/dataset-form', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);
