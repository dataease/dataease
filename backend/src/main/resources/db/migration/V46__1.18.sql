ALTER TABLE `panel_app_template_log`
    ADD COLUMN `datasource_from` varchar(255) NULL DEFAULT 'new' COMMENT '数据源来源' AFTER `datasource_id`;

UPDATE `panel_subject`
SET `details` = '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":true,\"openCommonStyle\":true,\"panel\":{\"themeColor\":\"light\",\"color\":\"#F1F3F5\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\",\"resultMode\":\"all\",\"resultCount\":1000},\"aidedDesign\":{\"showGrid\":false,\"matrixBase\":4},\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"themeId\":\"251a25d0-7ac5-11ed-9e50-5f9360ac1250\",\"chartInfo\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#000000\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true},\"chartColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#6D9A49\",\"tableItemBgColor\":\"#FFFFFF\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#4E81BB\",\"tableBorderColor\":\"#E6E7E4\",\"seriesColors\":[],\"tableHeaderFontColor\":\"#000000\"},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"color\":\"#FFFFFF\",\"alpha\":100,\"borderRadius\":5,\"innerPadding\":0},\"filterStyle\":{\"horizontal\":\"left\",\"vertical\":\"top\",\"color\":\"#000000\",\"brColor\":\"#DCDFE6\",\"wordColor\":\"#606266\",\"innerBgColor\":\"#FFFFFF\"}}}'
WHERE `id` = 'system_1';


ALTER TABLE `sys_task`
    ADD COLUMN `status` tinyint(1) NULL DEFAULT 1 COMMENT '运行状态' AFTER `create_time`;


INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`,
                        `path`, `i_frame`, `cache`, `hidden`, `permission`)
VALUES (1100, 1, 0, 1, '血缘关系', 'sys-relationship', 'system/relationship/index', 1002, 'sys-relationship',
        'relationship', 0, 0, 0, 'relationship:read');

UPDATE `sys_menu`
SET `menu_sort` = 1003
WHERE (`menu_id` = 101);

UPDATE `my_plugin`
SET `version` = '1.18.0'
where `plugin_id` > 0;

ALTER TABLE `chart_view`
    ADD COLUMN `refresh_view_enable` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启刷新' AFTER `view_fields`,
ADD COLUMN `refresh_unit` varchar(255) NULL DEFAULT 'minute' COMMENT '刷新时间单位' AFTER `refresh_view_enable`,
ADD COLUMN `refresh_time` int(13) NULL DEFAULT 5 COMMENT '刷新时间' AFTER `refresh_unit`;

ALTER TABLE `chart_view_cache`
    ADD COLUMN `refresh_view_enable` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启刷新' AFTER `view_fields`,
ADD COLUMN `refresh_unit` varchar(255) NULL DEFAULT 'minute' COMMENT '刷新时间单位' AFTER `refresh_view_enable`,
ADD COLUMN `refresh_time` int(13) NULL DEFAULT 5 COMMENT '刷新时间' AFTER `refresh_unit`;

delete
from sys_menu
where menu_id = 41;
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`,
                        `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`,
                        `update_time`)
VALUES (45, 1, 1, 1, '应用管理', 'system-app-template', 'panel/appTemplate/index', 13, 'sys-param',
        'panel/appTemplate/index', b'0', b'0', b'0', 'app-template:read', NULL, NULL, NULL, 1620444227389);


delete
FROM sys_auth_detail
WHERE auth_id IN (SELECT id
                  FROM sys_auth
                  WHERE auth_source_type = 'menu'
                    AND auth_target = 1
                    AND auth_target_type = 'role'
                    AND id NOT IN (
                                   '00590a7c-8e7b-45f4-8428-55532be07602',
                                   '06ba0edb-143d-4b51-a864-8cfcf2b5d71e',
                                   '0c045d89-85ea-4676-8b5e-4b3dae5a734d',
                                   '1a18aa12-8daa-4f47-b5eb-999e473273df',
                                   '1bf39d8d-7fe9-4832-8df3-f74b21a69288',
                                   '359771bb-95b8-40ad-a6c5-b5c39c93cb10',
                                   '37457802-97a6-4303-be89-cf82b4059db1',
                                   '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14',
                                   '5960fd93-013c-4636-8f6b-2e6b49b7e869',
                                   '5e0a9ad5-81ed-4f83-91f6-a74be724bda7',
                                   '5ec5c9a7-04c0-4655-9b63-9ca5a439e2f3',
                                   '60b3644b-2493-4805-8204-90880cfac9c6',
                                   '6e22ad53-d737-447f-9686-5041e122b4dc',
                                   '74ff225f-2a79-4221-9b32-c6eb9bcadd61',
                                   '7823499e-dbb6-42a9-a28f-22377de18a39',
                                   '88c778e6-ede3-4397-af4c-375e1feac8ef',
                                   '9019e9e4-8ea6-47ea-9279-98d10be107fc',
                                   '998e402b-6f15-48dc-ae4d-2cd04460a3f3',
                                   '9f01c7ef-753b-4dea-97d4-c199f84c2c74',
                                   'a6837d68-80a6-4a26-a7c0-e84001dfc817',
                                   'b319dc39-c499-423b-8e99-22e9a0caba6f',
                                   'bdd3bed8-35d4-4aa8-84c2-85e2412d6cbb',
                                   'c18b47e9-aa22-4f17-b12c-a3459ca4dd90',
                                   'd2368c49-33b0-46b2-894d-b182d1c03bd4',
                                   'd3d558e5-b0b1-4475-bb69-f20fa5c47f4f',
                                   'd400c00f-9c18-4eb6-a70f-9c8caf8dddfe',
                                   'd8d18115-c7b9-4e99-96a8-fd1bbfddf543',
                                   'da17fcfe-7875-4aaf-983b-d750d71f36d2',
                                   'f17dc7f3-c97a-41b4-a2f4-1a04a857ae8a'
                      ));



delete
from sys_auth
where auth_source_type = 'menu'
  and auth_target = 1
  and auth_target_type = 'role'
  and id not in (
                 '00590a7c-8e7b-45f4-8428-55532be07602',
                 '06ba0edb-143d-4b51-a864-8cfcf2b5d71e',
                 '0c045d89-85ea-4676-8b5e-4b3dae5a734d',
                 '1a18aa12-8daa-4f47-b5eb-999e473273df',
                 '1bf39d8d-7fe9-4832-8df3-f74b21a69288',
                 '359771bb-95b8-40ad-a6c5-b5c39c93cb10',
                 '37457802-97a6-4303-be89-cf82b4059db1',
                 '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14',
                 '5960fd93-013c-4636-8f6b-2e6b49b7e869',
                 '5e0a9ad5-81ed-4f83-91f6-a74be724bda7',
                 '5ec5c9a7-04c0-4655-9b63-9ca5a439e2f3',
                 '60b3644b-2493-4805-8204-90880cfac9c6',
                 '6e22ad53-d737-447f-9686-5041e122b4dc',
                 '74ff225f-2a79-4221-9b32-c6eb9bcadd61',
                 '7823499e-dbb6-42a9-a28f-22377de18a39',
                 '88c778e6-ede3-4397-af4c-375e1feac8ef',
                 '9019e9e4-8ea6-47ea-9279-98d10be107fc',
                 '998e402b-6f15-48dc-ae4d-2cd04460a3f3',
                 '9f01c7ef-753b-4dea-97d4-c199f84c2c74',
                 'a6837d68-80a6-4a26-a7c0-e84001dfc817',
                 'b319dc39-c499-423b-8e99-22e9a0caba6f',
                 'bdd3bed8-35d4-4aa8-84c2-85e2412d6cbb',
                 'c18b47e9-aa22-4f17-b12c-a3459ca4dd90',
                 'd2368c49-33b0-46b2-894d-b182d1c03bd4',
                 'd3d558e5-b0b1-4475-bb69-f20fa5c47f4f',
                 'd400c00f-9c18-4eb6-a70f-9c8caf8dddfe',
                 'd8d18115-c7b9-4e99-96a8-fd1bbfddf543',
                 'da17fcfe-7875-4aaf-983b-d750d71f36d2',
                 'f17dc7f3-c97a-41b4-a2f4-1a04a857ae8a'
    );

INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('11849638-ccd8-4049-9761-bd4e1acfcd3e', '61', 'menu', '1', 'role', 1673261995502, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('17a685de-e5af-4fc0-80e6-6eceff1e3ad3', '800', 'menu', '1', 'role', 1673262012225, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('20f0010a-7694-49a0-a504-fca3ee276bea', '60', 'menu', '1', 'role', 1673261994954, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('4a6e2b26-8030-471f-91ed-a8f2b621b6ea', '619', 'menu', '1', 'role', 1673262000181, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('5d6828fa-8c5a-47d6-b8cc-f3457b06b33d', '1100', 'menu', '1', 'role', 1673262007415, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('66c7372f-02f4-4a18-9dbe-ece5a08972ad', '202', 'menu', '1', 'role', 1673262009466, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('6d189cdf-6d9f-451a-a96f-80a0ff696d5e', '101', 'menu', '1', 'role', 1673261986693, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('6fa42a15-5111-427c-8fa0-ef9c73500bea', '618', 'menu', '1', 'role', 1673261999994, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('a3b53606-c887-424a-b3d0-834eebdbbd6b', '64', 'menu', '1', 'role', 1673261995689, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('a57fed94-b2d3-445f-b965-859b7925d978', '63', 'menu', '1', 'role', 1673261995321, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('af6ed70a-a6fa-4d29-9942-0c5d1b2f9139', '102', 'menu', '1', 'role', 1673261986901, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('afb93259-1344-4724-80b9-28833c6ceff7', '65', 'menu', '1', 'role', 1673261995141, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('c47bbcb6-65c5-4b3f-b070-4482d04b5dd0', '45', 'menu', '1', 'role', 1673262003475, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`, `auth_time`,
                        `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('e11aff50-a167-44e1-8221-53290ee98b68', '103', 'menu', '1', 'role', 1673261987096, NULL, 'admin', NULL, NULL,
        NULL);


INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb055790-900c-11ed-bd88-0242ac130004', '6d189cdf-6d9f-451a-a96f-80a0ff696d5e', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb055bc3-900c-11ed-bd88-0242ac130004', '6d189cdf-6d9f-451a-a96f-80a0ff696d5e', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb2529a4-900c-11ed-bd88-0242ac130004', 'af6ed70a-a6fa-4d29-9942-0c5d1b2f9139', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb252f3e-900c-11ed-bd88-0242ac130004', 'af6ed70a-a6fa-4d29-9942-0c5d1b2f9139', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb42448f-900c-11ed-bd88-0242ac130004', 'e11aff50-a167-44e1-8221-53290ee98b68', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bb424988-900c-11ed-bd88-0242ac130004', 'e11aff50-a167-44e1-8221-53290ee98b68', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261987000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bff08bb5-900c-11ed-bd88-0242ac130004', '20f0010a-7694-49a0-a504-fca3ee276bea', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('bff08ee8-900c-11ed-bd88-0242ac130004', '20f0010a-7694-49a0-a504-fca3ee276bea', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c00c77b2-900c-11ed-bd88-0242ac130004', 'afb93259-1344-4724-80b9-28833c6ceff7', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c00c79ec-900c-11ed-bd88-0242ac130004', 'afb93259-1344-4724-80b9-28833c6ceff7', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c0280a9e-900c-11ed-bd88-0242ac130004', 'a57fed94-b2d3-445f-b965-859b7925d978', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c0280d39-900c-11ed-bd88-0242ac130004', 'a57fed94-b2d3-445f-b965-859b7925d978', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c0445adc-900c-11ed-bd88-0242ac130004', '11849638-ccd8-4049-9761-bd4e1acfcd3e', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c0445d4f-900c-11ed-bd88-0242ac130004', '11849638-ccd8-4049-9761-bd4e1acfcd3e', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c060d271-900c-11ed-bd88-0242ac130004', 'a3b53606-c887-424a-b3d0-834eebdbbd6b', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c060d50d-900c-11ed-bd88-0242ac130004', 'a3b53606-c887-424a-b3d0-834eebdbbd6b', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673261995000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c2f16fa3-900c-11ed-bd88-0242ac130004', '6fa42a15-5111-427c-8fa0-ef9c73500bea', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262000000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c2f1754e-900c-11ed-bd88-0242ac130004', '6fa42a15-5111-427c-8fa0-ef9c73500bea', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262000000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c30dbae6-900c-11ed-bd88-0242ac130004', '4a6e2b26-8030-471f-91ed-a8f2b621b6ea', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262000000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c30dc057-900c-11ed-bd88-0242ac130004', '4a6e2b26-8030-471f-91ed-a8f2b621b6ea', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262000000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c504bf90-900c-11ed-bd88-0242ac130004', 'c47bbcb6-65c5-4b3f-b070-4482d04b5dd0', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262003000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c504c229-900c-11ed-bd88-0242ac130004', 'c47bbcb6-65c5-4b3f-b070-4482d04b5dd0', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262003000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c75e9179-900c-11ed-bd88-0242ac130004', '5d6828fa-8c5a-47d6-b8cc-f3457b06b33d', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262007000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c75e9478-900c-11ed-bd88-0242ac130004', '5d6828fa-8c5a-47d6-b8cc-f3457b06b33d', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262007000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c895eaaf-900c-11ed-bd88-0242ac130004', '66c7372f-02f4-4a18-9dbe-ece5a08972ad', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262009000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('c895efa5-900c-11ed-bd88-0242ac130004', '66c7372f-02f4-4a18-9dbe-ece5a08972ad', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262009000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('ca3baed4-900c-11ed-bd88-0242ac130004', '17a685de-e5af-4fc0-80e6-6eceff1e3ad3', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1673262012000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('ca3bb3ff-900c-11ed-bd88-0242ac130004', '17a685de-e5af-4fc0-80e6-6eceff1e3ad3', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1673262012000, NULL, NULL, NULL);

UPDATE `sys_menu`
SET `permission` = 'app-template:read'
WHERE `menu_id` = 45;
UPDATE `sys_menu`
SET `permission` = 'app-template-market:read'
WHERE `menu_id` = 203;

ALTER TABLE `panel_subject`
    ADD COLUMN `create_num` int(13) DEFAULT 0 COMMENT '创建序号' AFTER `details`;
