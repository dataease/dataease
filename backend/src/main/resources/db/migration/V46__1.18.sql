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
from sys_auth_detail
where auth_id in (select id from sys_auth where auth_target = 1 and auth_target_type = 'role');
delete
from sys_auth
where auth_target = 1
  and auth_target_type = 'role';

INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('00590a7c-8e7b-45f4-8428-55532be07602', '10', 'menu', '1', 'role', 1630482462199, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('060b5084-e937-4212-8e7d-5b27d28d3813', '619', 'menu', '1', 'role', 1672306656110, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('06ba0edb-143d-4b51-a864-8cfcf2b5d71e', '1', 'menu', '1', 'role', 1630482426344, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('0a08d6de-a7de-40d9-86c4-fffd4bf54e3a', '101', 'menu', '1', 'role', 1672306649767, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('0c045d89-85ea-4676-8b5e-4b3dae5a734d', '700', 'menu', '1', 'role', 1664521306828, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('1a18aa12-8daa-4f47-b5eb-999e473273df', '6', 'menu', '1', 'role', 1630482450994, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('1bf39d8d-7fe9-4832-8df3-f74b21a69288', '4', 'menu', '1', 'role', 1630482450458, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('2cada094-9543-4636-9b6f-b25c655399f0', '103', 'menu', '1', 'role', 1672306650137, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('359771bb-95b8-40ad-a6c5-b5c39c93cb10', '24', 'menu', '1', 'role', 1630482459156, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('37457802-97a6-4303-be89-cf82b4059db1', '910', 'menu', '1', 'role', 1664521307265, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', '203', 'menu', '1', 'role', 1666840141866, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('5960fd93-013c-4636-8f6b-2e6b49b7e869', '5', 'menu', '1', 'role', 1630482450626, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('5e0a9ad5-81ed-4f83-91f6-a74be724bda7', '23', 'menu', '1', 'role', 1630482452131, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('5ec5c9a7-04c0-4655-9b63-9ca5a439e2f3', '18', 'menu', '1', 'role', 1630482451166, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('60b3644b-2493-4805-8204-90880cfac9c6', '8', 'menu', '1', 'role', 1630482460538, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('6e22ad53-d737-447f-9686-5041e122b4dc', '205', 'menu', '1', 'role', 1666840141468, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('74ff225f-2a79-4221-9b32-c6eb9bcadd61', '19', 'menu', '1', 'role', 1630482451329, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('7669702f-1261-44a4-a1a6-4adc05c7edcd', '800', 'menu', '1', 'role', 1672306666561, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('7823499e-dbb6-42a9-a28f-22377de18a39', '40', 'menu', '1', 'role', 1630482427369, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('831449e3-cb7c-46a5-9ba2-4ef18af21985', '202', 'menu', '1', 'role', 1672306664677, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('88c778e6-ede3-4397-af4c-375e1feac8ef', '41', 'menu', '1', 'role', 1630482452340, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('9019e9e4-8ea6-47ea-9279-98d10be107fc', '710', 'menu', '1', 'role', 1664521307460, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('9037724f-b34c-45ca-9e73-59c4e7e72703', '64', 'menu', '1', 'role', 1672306653656, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('998e402b-6f15-48dc-ae4d-2cd04460a3f3', '15', 'menu', '1', 'role', 1630482426695, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('9f01c7ef-753b-4dea-97d4-c199f84c2c74', '17', 'menu', '1', 'role', 1630482427049, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('a2ece474-34d1-4d48-b7ff-4fcc6196d32b', '65', 'menu', '1', 'role', 1672306653103, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('a6837d68-80a6-4a26-a7c0-e84001dfc817', '34', 'menu', '1', 'role', 1630482458991, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('b319dc39-c499-423b-8e99-22e9a0caba6f', '58', 'menu', '1', 'role', 1630482427545, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('bdd3bed8-35d4-4aa8-84c2-85e2412d6cbb', '2', 'menu', '1', 'role', 1630482426513, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('c18b47e9-aa22-4f17-b12c-a3459ca4dd90', '28', 'menu', '1', 'role', 1630482427208, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d234696d-bf9e-4a58-9670-b2ff91884bbe', '102', 'menu', '1', 'role', 1672306649957, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d2368c49-33b0-46b2-894d-b182d1c03bd4', '810', 'menu', '1', 'role', 1664521307050, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d3d558e5-b0b1-4475-bb69-f20fa5c47f4f', '22', 'menu', '1', 'role', 1630482451962, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d400c00f-9c18-4eb6-a70f-9c8caf8dddfe', '21', 'menu', '1', 'role', 1630482451749, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d8d18115-c7b9-4e99-96a8-fd1bbfddf543', '16', 'menu', '1', 'role', 1630482426857, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('d956b3af-588d-477b-b404-20ded826593d', '401', 'menu', '1', 'role', 1672306682532, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('da17fcfe-7875-4aaf-983b-d750d71f36d2', '204', 'menu', '1', 'role', 1666840141658, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('daf90066-0299-4feb-b369-3fbb4a0e0821', '60', 'menu', '1', 'role', 1672306652922, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('e4d09516-79ef-41d1-8358-584224e07b73', '618', 'menu', '1', 'role', 1672306655930, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('e9f0837f-e717-49cd-9167-4bf503997a4a', '63', 'menu', '1', 'role', 1672306653282, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('ee9c43c9-f599-4d4e-bccc-c46f62119ed2', '61', 'menu', '1', 'role', 1672306653481, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('f17dc7f3-c97a-41b4-a2f4-1a04a857ae8a', '20', 'menu', '1', 'role', 1630482451497, NULL, 'admin', NULL, NULL,
        NULL);
INSERT INTO `sys_auth` (`id`, `auth_source`, `auth_source_type`, `auth_target`, `auth_target_type`,
                        `auth_time`, `auth_details`, `auth_user`, `update_time`, `copy_from`, `copy_id`)
VALUES ('fbb065cc-a5c7-47d7-bffb-4eda35928f34', '1100', 'menu', '1', 'role', 1672306662727, NULL, 'admin', NULL, NULL,
        NULL);

INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('69e22055-875c-11ed-bd88-0242ac130004', '0a08d6de-a7de-40d9-86c4-fffd4bf54e3a', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('69e22488-875c-11ed-bd88-0242ac130004', '0a08d6de-a7de-40d9-86c4-fffd4bf54e3a', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('69fdd519-875c-11ed-bd88-0242ac130004', 'd234696d-bf9e-4a58-9670-b2ff91884bbe', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('69fddc55-875c-11ed-bd88-0242ac130004', 'd234696d-bf9e-4a58-9670-b2ff91884bbe', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6a19b826-875c-11ed-bd88-0242ac130004', '2cada094-9543-4636-9b6f-b25c655399f0', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6a19bb9b-875c-11ed-bd88-0242ac130004', '2cada094-9543-4636-9b6f-b25c655399f0', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306650000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bc259de-875c-11ed-bd88-0242ac130004', 'daf90066-0299-4feb-b369-3fbb4a0e0821', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bc25c58-875c-11ed-bd88-0242ac130004', 'daf90066-0299-4feb-b369-3fbb4a0e0821', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bdd7239-875c-11ed-bd88-0242ac130004', 'a2ece474-34d1-4d48-b7ff-4fcc6196d32b', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bdd758b-875c-11ed-bd88-0242ac130004', 'a2ece474-34d1-4d48-b7ff-4fcc6196d32b', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bfc3df2-875c-11ed-bd88-0242ac130004', 'e9f0837f-e717-49cd-9167-4bf503997a4a', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6bfc43ec-875c-11ed-bd88-0242ac130004', 'e9f0837f-e717-49cd-9167-4bf503997a4a', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6c16b8f8-875c-11ed-bd88-0242ac130004', 'ee9c43c9-f599-4d4e-bccc-c46f62119ed2', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6c16bd6d-875c-11ed-bd88-0242ac130004', 'ee9c43c9-f599-4d4e-bccc-c46f62119ed2', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306653000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6c3217b4-875c-11ed-bd88-0242ac130004', '9037724f-b34c-45ca-9e73-59c4e7e72703', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306654000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6c321bdc-875c-11ed-bd88-0242ac130004', '9037724f-b34c-45ca-9e73-59c4e7e72703', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306654000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6d8d3c6f-875c-11ed-bd88-0242ac130004', 'e4d09516-79ef-41d1-8358-584224e07b73', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306656000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6d8d4031-875c-11ed-bd88-0242ac130004', 'e4d09516-79ef-41d1-8358-584224e07b73', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306656000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6daa85f3-875c-11ed-bd88-0242ac130004', '060b5084-e937-4212-8e7d-5b27d28d3813', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306656000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('6daa899a-875c-11ed-bd88-0242ac130004', '060b5084-e937-4212-8e7d-5b27d28d3813', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306656000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('7199b798-875c-11ed-bd88-0242ac130004', 'fbb065cc-a5c7-47d7-bffb-4eda35928f34', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306663000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('7199bc41-875c-11ed-bd88-0242ac130004', 'fbb065cc-a5c7-47d7-bffb-4eda35928f34', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306663000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('72c4a847-875c-11ed-bd88-0242ac130004', '831449e3-cb7c-46a5-9ba2-4ef18af21985', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306665000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('72c4adde-875c-11ed-bd88-0242ac130004', '831449e3-cb7c-46a5-9ba2-4ef18af21985', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306665000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('73e2fd98-875c-11ed-bd88-0242ac130004', '7669702f-1261-44a4-a1a6-4adc05c7edcd', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306666000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('73e301e6-875c-11ed-bd88-0242ac130004', '7669702f-1261-44a4-a1a6-4adc05c7edcd', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306666000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('7d68534e-875c-11ed-bd88-0242ac130004', 'd956b3af-588d-477b-b404-20ded826593d', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1672306682000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('7d6856d4-875c-11ed-bd88-0242ac130004', 'd956b3af-588d-477b-b404-20ded826593d', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1672306682000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b4fe2e52-55a4-11ed-bf84-0242ac130005', '6e22ad53-d737-447f-9686-5041e122b4dc', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b4fe3215-55a4-11ed-bf84-0242ac130005', '6e22ad53-d737-447f-9686-5041e122b4dc', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b51affbf-55a4-11ed-bf84-0242ac130005', 'da17fcfe-7875-4aaf-983b-d750d71f36d2', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b51b0473-55a4-11ed-bf84-0242ac130005', 'da17fcfe-7875-4aaf-983b-d750d71f36d2', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1666840141000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b53a1ad7-55a4-11ed-bf84-0242ac130005', '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1666840142000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('b53a1dfd-55a4-11ed-bf84-0242ac130005', '46e4e2cb-1349-40c3-a72d-7b0b30ab5d14', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1666840142000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('bfddec25-408d-11ed-8009-0242ac130005', '0c045d89-85ea-4676-8b5e-4b3dae5a734d', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('bfddf2e8-408d-11ed-8009-0242ac130005', '0c045d89-85ea-4676-8b5e-4b3dae5a734d', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('bfffbd09-408d-11ed-8009-0242ac130005', 'd2368c49-33b0-46b2-894d-b182d1c03bd4', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('bfffbf74-408d-11ed-8009-0242ac130005', 'd2368c49-33b0-46b2-894d-b182d1c03bd4', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('c01e6d09-408d-11ed-8009-0242ac130005', '37457802-97a6-4303-be89-cf82b4059db1', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('c01e6fb3-408d-11ed-8009-0242ac130005', '37457802-97a6-4303-be89-cf82b4059db1', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('c03c0af2-408d-11ed-8009-0242ac130005', '9019e9e4-8ea6-47ea-9279-98d10be107fc', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('c03c0dc3-408d-11ed-8009-0242ac130005', '9019e9e4-8ea6-47ea-9279-98d10be107fc', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1664521307000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf3c570c-0af8-11ec-a2b0-0242ac130003', '06ba0edb-143d-4b51-a864-8cfcf2b5d71e', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482428000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf3c59d1-0af8-11ec-a2b0-0242ac130003', '06ba0edb-143d-4b51-a864-8cfcf2b5d71e', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482428000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf573296-0af8-11ec-a2b0-0242ac130003', 'bdd3bed8-35d4-4aa8-84c2-85e2412d6cbb', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482428000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf57354a-0af8-11ec-a2b0-0242ac130003', 'bdd3bed8-35d4-4aa8-84c2-85e2412d6cbb', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482428000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf705f77-0af8-11ec-a2b0-0242ac130003', '998e402b-6f15-48dc-ae4d-2cd04460a3f3', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf7061d1-0af8-11ec-a2b0-0242ac130003', '998e402b-6f15-48dc-ae4d-2cd04460a3f3', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf8b490d-0af8-11ec-a2b0-0242ac130003', 'd8d18115-c7b9-4e99-96a8-fd1bbfddf543', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cf8b4b35-0af8-11ec-a2b0-0242ac130003', 'd8d18115-c7b9-4e99-96a8-fd1bbfddf543', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfa66a8d-0af8-11ec-a2b0-0242ac130003', '9f01c7ef-753b-4dea-97d4-c199f84c2c74', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfa66ca4-0af8-11ec-a2b0-0242ac130003', '9f01c7ef-753b-4dea-97d4-c199f84c2c74', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfbf0608-0af8-11ec-a2b0-0242ac130003', 'c18b47e9-aa22-4f17-b12c-a3459ca4dd90', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfbf0839-0af8-11ec-a2b0-0242ac130003', 'c18b47e9-aa22-4f17-b12c-a3459ca4dd90', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfd814bc-0af8-11ec-a2b0-0242ac130003', '7823499e-dbb6-42a9-a28f-22377de18a39', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cfd816d7-0af8-11ec-a2b0-0242ac130003', '7823499e-dbb6-42a9-a28f-22377de18a39', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cff28219-0af8-11ec-a2b0-0242ac130003', 'b319dc39-c499-423b-8e99-22e9a0caba6f', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('cff28507-0af8-11ec-a2b0-0242ac130003', 'b319dc39-c499-423b-8e99-22e9a0caba6f', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482429000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('dd9ba5c6-0af8-11ec-a2b0-0242ac130003', '1bf39d8d-7fe9-4832-8df3-f74b21a69288', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482452000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('dd9ba7e8-0af8-11ec-a2b0-0242ac130003', '1bf39d8d-7fe9-4832-8df3-f74b21a69288', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482452000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('ddb3094b-0af8-11ec-a2b0-0242ac130003', '5960fd93-013c-4636-8f6b-2e6b49b7e869', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482452000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('ddb30bd2-0af8-11ec-a2b0-0242ac130003', '5960fd93-013c-4636-8f6b-2e6b49b7e869', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482452000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('ddec6eab-0af8-11ec-a2b0-0242ac130003', '1a18aa12-8daa-4f47-b5eb-999e473273df', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('ddeccfa8-0af8-11ec-a2b0-0242ac130003', '1a18aa12-8daa-4f47-b5eb-999e473273df', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de0719cb-0af8-11ec-a2b0-0242ac130003', '5ec5c9a7-04c0-4655-9b63-9ca5a439e2f3', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de071bd9-0af8-11ec-a2b0-0242ac130003', '5ec5c9a7-04c0-4655-9b63-9ca5a439e2f3', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de202758-0af8-11ec-a2b0-0242ac130003', '74ff225f-2a79-4221-9b32-c6eb9bcadd61', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de202961-0af8-11ec-a2b0-0242ac130003', '74ff225f-2a79-4221-9b32-c6eb9bcadd61', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de42f5ec-0af8-11ec-a2b0-0242ac130003', 'f17dc7f3-c97a-41b4-a2f4-1a04a857ae8a', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de42f8b4-0af8-11ec-a2b0-0242ac130003', 'f17dc7f3-c97a-41b4-a2f4-1a04a857ae8a', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482453000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de65f6d9-0af8-11ec-a2b0-0242ac130003', 'd400c00f-9c18-4eb6-a70f-9c8caf8dddfe', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de65fa72-0af8-11ec-a2b0-0242ac130003', 'd400c00f-9c18-4eb6-a70f-9c8caf8dddfe', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de80ddc9-0af8-11ec-a2b0-0242ac130003', 'd3d558e5-b0b1-4475-bb69-f20fa5c47f4f', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de80e001-0af8-11ec-a2b0-0242ac130003', 'd3d558e5-b0b1-4475-bb69-f20fa5c47f4f', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de9f30cd-0af8-11ec-a2b0-0242ac130003', '5e0a9ad5-81ed-4f83-91f6-a74be724bda7', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('de9f32d3-0af8-11ec-a2b0-0242ac130003', '5e0a9ad5-81ed-4f83-91f6-a74be724bda7', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('dec15e1b-0af8-11ec-a2b0-0242ac130003', '88c778e6-ede3-4397-af4c-375e1feac8ef', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('dec16031-0af8-11ec-a2b0-0242ac130003', '88c778e6-ede3-4397-af4c-375e1feac8ef', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482454000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e2b029b9-0af8-11ec-a2b0-0242ac130003', 'a6837d68-80a6-4a26-a7c0-e84001dfc817', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482461000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e2b02bcb-0af8-11ec-a2b0-0242ac130003', 'a6837d68-80a6-4a26-a7c0-e84001dfc817', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482461000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e2c9aba7-0af8-11ec-a2b0-0242ac130003', '359771bb-95b8-40ad-a6c5-b5c39c93cb10', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482461000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e2c9adc8-0af8-11ec-a2b0-0242ac130003', '359771bb-95b8-40ad-a6c5-b5c39c93cb10', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482461000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e39cb3fe-0af8-11ec-a2b0-0242ac130003', '60b3644b-2493-4805-8204-90880cfac9c6', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482462000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e39cb619-0af8-11ec-a2b0-0242ac130003', '60b3644b-2493-4805-8204-90880cfac9c6', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482462000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e49bef48-0af8-11ec-a2b0-0242ac130003', '00590a7c-8e7b-45f4-8428-55532be07602', 'i18n_auth_grant', 15, 0,
        'grant', '基础权限-授权', 'admin', 1630482464000, NULL, NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`,
                               `copy_from`, `copy_id`)
VALUES ('e49bf17e-0af8-11ec-a2b0-0242ac130003', '00590a7c-8e7b-45f4-8428-55532be07602', 'i18n_auth_use', 1, 1, 'use',
        '基础权限-使用', 'admin', 1630482464000, NULL, NULL, NULL);
