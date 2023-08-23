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

CREATE TABLE `dataset_sql_log` (
       `id` varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
       `dataset_id` varchar(50) NOT NULL DEFAULT '' COMMENT '数据集ID',
       `start_time` bigint(13) DEFAULT NULL COMMENT '开始时间',
       `end_time` bigint(13) DEFAULT NULL COMMENT '结束时间',
       `spend` bigint(13) DEFAULT NULL COMMENT '耗时(毫秒)',
       `sql` longtext NOT NULL COMMENT '详细信息',
       `status` varchar(45) DEFAULT NULL COMMENT '状态',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

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


DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS SELECT
                                                                             `sys_user`.`user_id` AS `id`,
                                                                             concat( `sys_user`.`nick_name`, '（', `sys_user`.`username`, '）' ) AS `name`,
                                                                             `sys_user`.`username` AS `label`,
                                                                             '0' AS `pid`,
                                                                             'leaf' AS `node_type`,
                                                                             'user' AS `model_type`,
                                                                             'user' AS `model_inner_type`,
                                                                             'target' AS `auth_type`,
                                                                             `sys_user`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `sys_user`
                                                                         WHERE
                                                                             ( `sys_user`.`is_admin` <> 1 ) UNION ALL
                                                                         SELECT
                                                                             `sys_role`.`role_id` AS `id`,
                                                                             `sys_role`.`name` AS `name`,
                                                                             `sys_role`.`name` AS `label`,
                                                                             '0' AS `pid`,
                                                                             'leaf' AS `node_type`,
                                                                             'role' AS `model_type`,
                                                                             'role' AS `model_inner_type`,
                                                                             'target' AS `auth_type`,
                                                                             `sys_role`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `sys_role` UNION ALL
                                                                         SELECT
                                                                             `sys_dept`.`dept_id` AS `id`,
                                                                             `sys_dept`.`name` AS `name`,
                                                                             `sys_dept`.`name` AS `label`,(
                                                                                     cast( `sys_dept`.`pid` AS CHAR charset utf8mb4 ) COLLATE utf8mb4_general_ci
                                                                                 ) AS `pid`,
                                                                             IF
                                                                                 (( `sys_dept`.`sub_count` = 0 ), 'leaf', 'spine' ) AS `node_type`,
                                                                             'dept' AS `model_type`,
                                                                             'dept' AS `model_inner_type`,
                                                                             'target' AS `auth_type`,
                                                                             `sys_dept`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `sys_dept` UNION ALL
                                                                         SELECT
                                                                             `datasource`.`id` AS `id`,
                                                                             `datasource`.`name` AS `NAME`,
                                                                             `datasource`.`name` AS `label`,
                                                                             '0' AS `pid`,
                                                                             'leaf' AS `node_type`,
                                                                             'link' AS `model_type`,
                                                                             `datasource`.`type` AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `datasource`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `datasource` UNION ALL
                                                                         SELECT
                                                                             `dataset_group`.`id` AS `id`,
                                                                             `dataset_group`.`name` AS `NAME`,
                                                                             `dataset_group`.`name` AS `label`,
                                                                             IF
                                                                                 ( isnull( `dataset_group`.`pid` ), '0', `dataset_group`.`pid` ) AS `pid`,
                                                                             'spine' AS `node_type`,
                                                                             'dataset' AS `model_type`,
                                                                             `dataset_group`.`type` AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `dataset_group`.`create_by` AS `create_by`,
                                                                             `dataset_group`.`level` AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `dataset_group` UNION ALL
                                                                         SELECT
                                                                             `dataset_table`.`id` AS `id`,
                                                                             `dataset_table`.`name` AS `NAME`,
                                                                             `dataset_table`.`name` AS `label`,
                                                                             `dataset_table`.`scene_id` AS `pid`,
                                                                             'leaf' AS `node_type`,
                                                                             'dataset' AS `model_type`,
                                                                             `dataset_table`.`type` AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `dataset_table`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             `dataset_table`.`mode` AS `mode`,
                                                                             `dataset_table`.`data_source_id` AS `data_source_id`
                                                                         FROM
                                                                             `dataset_table` UNION ALL
                                                                         SELECT
                                                                             `panel_group`.`id` AS `id`,
                                                                             `panel_group`.`name` AS `NAME`,
                                                                             `panel_group`.`name` AS `label`,(
                                                                                 CASE
                                                                                     `panel_group`.`id`
                                                                                     WHEN 'panel_list' THEN
                                                                                         '0'
                                                                                     WHEN 'default_panel' THEN
                                                                                         '0' ELSE `panel_group`.`pid`
                                                                                     END
                                                                                 ) AS `pid`,
                                                                             IF
                                                                                 (( `panel_group`.`node_type` = 'folder' ), 'spine', 'leaf' ) AS `node_type`,
                                                                             'panel' AS `model_type`,
                                                                             `panel_group`.`panel_type` AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `panel_group`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `panel_group` UNION ALL
                                                                         SELECT
                                                                             `sys_menu`.`menu_id` AS `menu_id`,
                                                                             `sys_menu`.`title` AS `name`,
                                                                             `sys_menu`.`title` AS `label`,
                                                                             `sys_menu`.`pid` AS `pid`,
                                                                             IF
                                                                                 (( `sys_menu`.`sub_count` > 0 ), 'spine', 'leaf' ) AS `node_type`,
                                                                             'menu' AS `model_type`,(
                                                                                 CASE
                                                                                     `sys_menu`.`type`
                                                                                     WHEN 0 THEN
                                                                                         'folder'
                                                                                     WHEN 1 THEN
                                                                                         'menu'
                                                                                     WHEN 2 THEN
                                                                                         'button'
                                                                                     END
                                                                                 ) AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `sys_menu`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `sys_menu`
                                                                         WHERE
                                                                             ((
                                                                                      `sys_menu`.`i_frame` <> 1
                                                                                  )
                                                                                 OR isnull( `sys_menu`.`i_frame` )) UNION ALL
                                                                         SELECT
                                                                             `plugin_sys_menu`.`menu_id` AS `menu_id`,
                                                                             `plugin_sys_menu`.`title` AS `name`,
                                                                             `plugin_sys_menu`.`title` AS `label`,
                                                                             `plugin_sys_menu`.`pid` AS `pid`,
                                                                             IF
                                                                                 (( `plugin_sys_menu`.`sub_count` > 0 ), 'spine', 'leaf' ) AS `node_type`,
                                                                             'menu' AS `model_type`,(
                                                                                 CASE
                                                                                     `plugin_sys_menu`.`type`
                                                                                     WHEN 0 THEN
                                                                                         'folder'
                                                                                     WHEN 1 THEN
                                                                                         'menu'
                                                                                     WHEN 2 THEN
                                                                                         'button'
                                                                                     END
                                                                                 ) AS `model_inner_type`,
                                                                             'source' AS `auth_type`,
                                                                             `plugin_sys_menu`.`create_by` AS `create_by`,
                                                                             0 AS `level`,
                                                                             0 AS `mode`,
                                                                             '0' AS `data_source_id`
                                                                         FROM
                                                                             `plugin_sys_menu`
                                                                         WHERE
                                                                             ((
                                                                                      `plugin_sys_menu`.`i_frame` <> 1
                                                                                  )
                                                                                 OR isnull( `plugin_sys_menu`.`i_frame` ));

-- ----------------------------
-- Function structure for delete_auth_target
-- ----------------------------
DROP FUNCTION IF EXISTS `delete_auth_target`;
delimiter ;;
CREATE FUNCTION `delete_auth_target`(authTarget varchar(255),authTargetType varchar(255))
    RETURNS varchar(255) CHARSET utf8mb4
  READS SQL DATA
BEGIN

delete from sys_auth_detail where auth_id in (
    select id from  sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType
);

delete from sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType;

RETURN 'success';

END
;;
delimiter ;

UPDATE `panel_subject`
SET `details` = '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":true,\"openCommonStyle\":true,\"panel\":{\"themeColor\":\"dark\",\"color\":\"#030B2E\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\",\"resultMode\":\"all\",\"resultCount\":1000},\"aidedDesign\":{\"showGrid\":false,\"matrixBase\":4},\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"themeId\":\"f9f46a50-58f5-11ed-889b-91ab7371e877\",\"chartInfo\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#FFFFFF\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true,\"remarkShow\":false,\"remark\":\"\",\"remarkBackgroundColor\":\"#5A5C62\",\"fontFamily\":\"Microsoft YaHei\",\"letterSpace\":\"0\",\"fontShadow\":false},\"chartColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#5470C6\",\"tableItemBgColor\":\"#131E42\",\"tableFontColor\":\"#ffffff\",\"tableStripe\":true,\"dimensionColor\":\"#ffffff\",\"quotaColor\":\"#5470C6\",\"tableBorderColor\":\"#CCCCCC\",\"seriesColors\":[],\"areaBorderColor\":\"#EBEEF5\",\"tableHeaderFontColor\":\"#ffffff\",\"modifyName\":\"colors\"},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"color\":\"#131E42\",\"alpha\":100,\"borderRadius\":5,\"innerPadding\":0},\"filterStyle\":{\"horizontal\":\"left\",\"vertical\":\"top\",\"color\":\"#FFFFFF\",\"brColor\":\"#4E4B4B\",\"wordColor\":\"#FFFFFF\",\"innerBgColor\":\"#131E42\"},\"tabStyle\":{\"headFontColor\":\"#FFFFFF\",\"headFontActiveColor\":\"#FFFFFF\",\"headBorderColor\":\"#FFFFFF\",\"headBorderActiveColor\":\"#FFFFFF\",\"headPosition\":\"left\"}}}'
WHERE `id` = 'system_2';

UPDATE `dataset_table_function` SET `func` = 'CASE expr WHEN v1 THEN r1 [WHEN v2 THEN r2] [ELSE rn] END' WHERE `id` = 47;
UPDATE `dataset_table_function` SET `func` = 'CASE expr WHEN v1 THEN r1 [WHEN v2 THEN r2] [ELSE rn] END' WHERE `id` = 96;
