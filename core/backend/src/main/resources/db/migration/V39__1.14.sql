INSERT INTO `sys_menu` VALUES (700, 1, 2, 1, '系统配置', 'sys-settings', 'system/settings/index', 12, 'sys-tools', 'system-settings', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);

INSERT INTO `sys_menu` VALUES (730, 1, 0, 1, '数据同步表单', 'sys-task-ds-form', 'system/task/form', 11, NULL, '/task-ds-form', b'1', b'0', b'1', NULL, NULL, NULL, NULL, NULL);

UPDATE `sys_menu` set pid = 700, menu_sort = 1 where menu_id = 6 and `name` = 'system-param';



ALTER TABLE `sys_theme`
DROP COLUMN `img`,
DROP COLUMN `img_id`,
ADD COLUMN `senior` TINYINT(1) NULL DEFAULT NULL AFTER `status`;


update sys_background_image set classification ='商务';
update sys_background_image set name ='边框10' where id ='dark_1';


INSERT INTO `sys_menu` VALUES (750, 2, 0, 2, '导入用户', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:import', NULL, NULL, 1614930935529, 1614930935529);


update system_parameter set sort  = (sort + 1) where sort > 3;

update system_parameter set sort = 4 where param_key = 'ui.favicon';

INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.showFoot', NULL, 'text', 18);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.footContent', NULL, 'blob', 19);

CREATE TABLE IF NOT EXISTS `sys_param_assist` (
    `id` BIGINT(21) NOT NULL AUTO_INCREMENT,
    `content` MEDIUMBLOB COMMENT '内容',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `sys_task_email`
MODIFY COLUMN `view_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '关联视图' AFTER `task_id`;

BEGIN;
update `sys_menu` set icon = 'plugins-new' where `menu_id` = 101;
update `sys_menu` set icon = 'sys-setting' where `menu_id` = 700;
update `sys_menu` set icon = 'sys-param' where `menu_id` = 6;
update `sys_menu` set icon = 'display-setting' where `menu_id` = 710;
COMMIT;

ALTER TABLE `de_driver_details` ADD COLUMN `is_trans_name` TINYINT(1) NULL AFTER `driver_class`;
ALTER TABLE `de_driver_details` ADD COLUMN `trans_name` VARCHAR(255) NULL AFTER `driver_class`;

BEGIN;
UPDATE `sys_theme_item` set `val` = '#3370FF' where `theme_id` = 1 and `key` = 'primary' and `val` = '#409EFF';
UPDATE `sys_theme_item` set `val` = '#34C724' where `theme_id` = 1 and `key` = 'deSuccess' and `val` = '#67C23A';
UPDATE `sys_theme_item` set `val` = '#FF8800' where `theme_id` = 1 and `key` = 'deWarning' and `val` = '#E6A23C';
UPDATE `sys_theme_item` set `val` = '#F54A45' where `theme_id` = 1 and `key` = 'deDanger' and `val` = '#F56C6C';

UPDATE `sys_theme_item` set `val` = '#1F2329' where `theme_id` = 1 and `key` = 'deTextPrimary' and `val` = '#303133';
UPDATE `sys_theme_item` set `val` = '#646A73' where `theme_id` = 1 and `key` = 'deTextSecondary' and `val` = '#909399';
UPDATE `sys_theme_item` set `val` = '#8F959E' where `theme_id` = 1 and `key` = 'deTextPlaceholder' and `val` = '#C0C4CC';
INSERT INTO `sys_theme_item` VALUES (1, 'deTextDisable', '#BBBFC4');

INSERT INTO `sys_theme_item` VALUES (1, 'deComBorderColor', '#BBBFC4');
INSERT INTO `sys_theme_item` VALUES (1, 'deCardStrokeColor', '#DEE0E3');
UPDATE `sys_theme_item` set `val` = '#1F2329' where `theme_id` = 1 and `key` = 'deBlack' and `val` = '#000000';
UPDATE `sys_theme_item` set `val` = '#F5F6F7' where `theme_id` = 1 and `key` = 'deBackgroundBase' and `val` = '#F5F7FA';
INSERT INTO `sys_theme_item` VALUES (1, 'deInputDisableBackground', '#EFF0F1');

COMMIT;


ALTER TABLE `sys_theme` ADD COLUMN `origin_id` bigint(20) NULL COMMENT '源主题Id' AFTER `senior`;

BEGIN;
UPDATE `sys_theme` set `origin_id` = 1 where `id` = 1;
UPDATE `sys_theme` set `origin_id` = 2 where `id` = 2;
UPDATE `sys_theme` set `origin_id` = 1 where origin_id is null;
COMMIT;


UPDATE `panel_subject`
SET
    `details` = '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":true,\"openCommonStyle\":true,\"panel\":{\"themeColor\":\"dark\",\"color\":\"#030B2E\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\",\"resultMode\":\"all\",\"resultCount\":1000},\"aidedDesign\":{\"showGrid\":false,\"matrixBase\":4},\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"themeId\":\"c9d63e10-2827-11ed-afd9-69d86fea45a0\",\"chartInfo\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#FFFFFF\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true,\"remarkShow\":false,\"remark\":\"\",\"remarkBackgroundColor\":\"#ffffffff\",\"fontFamily\":\"Microsoft YaHei\",\"letterSpace\":\"0\",\"fontShadow\":false},\"chartColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4E81BB\",\"tableItemBgColor\":\"#131E42\",\"tableFontColor\":\"#ffffff\",\"tableStripe\":true,\"dimensionColor\":\"#ffffff\",\"quotaColor\":\"#4E81BB\",\"tableBorderColor\":\"#CCCCCC\",\"seriesColors\":[],\"areaBorderColor\":\"#EBEEF5\"},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"color\":\"#131E42\",\"alpha\":100,\"borderRadius\":5,\"innerPadding\":0},\"filterStyle\":{\"horizontal\":\"left\",\"vertical\":\"top\",\"color\":\"#FFFFFF\",\"brColor\":\"#4E4B4B\",\"wordColor\":\"#FFFFFF\",\"innerBgColor\":\"#131E42\"},\"tabStyle\":{\"headFontColor\":\"#FFFFFF\",\"headFontActiveColor\":\"#FFFFFF\",\"headBorderColor\":\"\",\"headBorderActiveColor\":\"\"}}}'
WHERE
        `id` = 'system_2';

BEGIN;
DELETE from `sys_menu` where `menu_id` = 55;
DELETE from `sys_menu` where `menu_id` = 56;
COMMIT;

BEGIN;
UPDATE `my_plugin` set cost = '60000', `creator` = 'DATAEASE' WHERE `module_name` = 'deplugin-xpack-backend';
UPDATE `my_plugin` set cost = '0', `creator` = 'DATAEASE' WHERE `module_name` = 'view-bubblemap-backend';
UPDATE `my_plugin` set cost = '0', `creator` = 'DATAEASE' WHERE `module_name` = 'view-symbolmap-backend';
COMMIT;