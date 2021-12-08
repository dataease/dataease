ALTER TABLE `chart_view` ADD COLUMN `render` varchar(50)  COMMENT '视图渲染方式' AFTER `type`;
UPDATE `chart_view` SET `render` = 'echarts' WHERE `type` != 'liquid';
UPDATE `chart_view` SET `render` = 'antv' WHERE `type` = 'liquid';

ALTER TABLE `panel_link` ADD COLUMN `over_time` bigint(13) NULL DEFAULT NULL COMMENT '有效截止时间' AFTER `pwd`;

CREATE TABLE `panel_link_jump` (
  `id` varchar(50) NOT NULL,
  `source_panel_id` varchar(50) DEFAULT NULL COMMENT '源仪表板ID',
  `source_view_id` varchar(50) DEFAULT NULL COMMENT '源视图ID',
  `link_jump_info` varchar(4000) DEFAULT NULL COMMENT '跳转信息',
  `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `panel_link_jump_info` (
  `id` varchar(50) NOT NULL,
  `link_jump_id` varchar(50) DEFAULT NULL COMMENT 'link jump ID',
  `link_type` varchar(255) DEFAULT NULL COMMENT '关联类型 inner 内部仪表板，outer 外部链接',
  `jump_type` varchar(255) DEFAULT NULL COMMENT '跳转类型 _blank 新开页面 _self 当前窗口',
  `target_panel_id` varchar(255) DEFAULT NULL COMMENT '关联仪表板ID',
  `source_field_id` varchar(255) DEFAULT NULL COMMENT '字段ID',
  `content` varchar(4000) DEFAULT NULL COMMENT '内容 linkType = outer时使用',
  `checked` tinyint(1) DEFAULT NULL COMMENT '是否可用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `panel_link_jump_target_view_info` (
  `target_id` varchar(50) NOT NULL,
  `link_jump_info_id` varchar(50) DEFAULT NULL,
  `target_view_id` varchar(50) DEFAULT NULL,
  `target_field_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

BEGIN;
INSERT INTO `sys_menu` VALUES (6, 1, 0, 1, '系统参数', 'system-param', 'system/SysParam/index', 6, 'sys-tools', 'system-param', b'0', b'0', b'0', 'sysparam:read', NULL, NULL, NULL, NULL);
COMMIT;


ALTER TABLE `chart_view`
MODIFY COLUMN `name` varchar(1024) NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
MODIFY COLUMN `title` varchar(1024) NULL DEFAULT NULL COMMENT 'EChart标题' AFTER `name`;

ALTER TABLE `chart_view` ADD COLUMN `result_count` int(10)  COMMENT '展示结果' AFTER `render`;
ALTER TABLE `chart_view` ADD COLUMN `result_mode` varchar(50)  COMMENT '展示模式' AFTER `result_count`;
UPDATE `chart_view` SET `result_count` = 1000;
UPDATE `chart_view` SET `result_mode` = 'custom';

ALTER TABLE `dataset_table`
    MODIFY COLUMN `name` varchar(128) NULL DEFAULT NULL;

-- ----------------------------
-- Table structure for sys_theme
-- ----------------------------
CREATE TABLE `sys_theme` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主题id',
  `name` varchar(255) NOT NULL COMMENT '主题名称',
  `img_id` varchar(255) DEFAULT NULL COMMENT '文件ID',
  `img` varchar(255) DEFAULT NULL COMMENT '主题缩略图',
  `status` tinyint(1) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of sys_theme
-- ----------------------------
BEGIN;
INSERT INTO `sys_theme` VALUES (1, '默认主题', NULL, '', 1);
INSERT INTO `sys_theme` VALUES (2, '深色主题', NULL, '', 0);
INSERT INTO `sys_theme` VALUES (3, '自定义主题', NULL, '', 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;



-- ----------------------------
-- Table structure for sys_theme_item
-- ----------------------------
CREATE TABLE `sys_theme_item` (
  `theme_id` bigint(20) NOT NULL COMMENT '主题ID',
  `key` varchar(255) DEFAULT NULL COMMENT '样式key',
  `val` varchar(255) DEFAULT NULL COMMENT '样式val'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Records of sys_theme_item
-- ----------------------------
BEGIN;
INSERT INTO `sys_theme_item` VALUES (3, 'primary', '#C386F2');
INSERT INTO `sys_theme_item` VALUES (3, 'deSuccess', '#67C23A');
INSERT INTO `sys_theme_item` VALUES (3, 'deWarning', '#E6A23C');
INSERT INTO `sys_theme_item` VALUES (3, 'deDanger', '#F56C6C');
INSERT INTO `sys_theme_item` VALUES (3, 'deInfo', '#909399');
INSERT INTO `sys_theme_item` VALUES (3, 'deTextPrimary', '#303133');
INSERT INTO `sys_theme_item` VALUES (3, 'deTextRegular', '#606266');
INSERT INTO `sys_theme_item` VALUES (3, 'deTextSecondary', '#909399');
INSERT INTO `sys_theme_item` VALUES (3, 'deTextPlaceholder', '#C0C4CC');
INSERT INTO `sys_theme_item` VALUES (3, 'deBorderBase', '#DCDFE6');
INSERT INTO `sys_theme_item` VALUES (3, 'deBorderLight', '#E4E7ED');
INSERT INTO `sys_theme_item` VALUES (3, 'deBorderLighter', '#EBEEF5');
INSERT INTO `sys_theme_item` VALUES (3, 'deBorderExtraLight', '#F2F6FC');
INSERT INTO `sys_theme_item` VALUES (3, 'deWhite', '#FFFFFF');
INSERT INTO `sys_theme_item` VALUES (3, 'deBlack', '#000000');
INSERT INTO `sys_theme_item` VALUES (3, 'deBackgroundBase', '#F5F7FA');
INSERT INTO `sys_theme_item` VALUES (3, 'shade-1', 'rgb(176, 121, 218)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-1', 'rgb(201, 146, 243)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-2', 'rgb(207, 158, 245)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-3', 'rgb(213, 170, 246)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-4', 'rgb(219, 182, 247)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-5', 'rgb(225, 195, 249)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-6', 'rgb(231, 207, 250)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-7', 'rgb(237, 219, 251)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-8', 'rgb(243, 231, 252)');
INSERT INTO `sys_theme_item` VALUES (3, 'light-9', 'rgb(249, 243, 254)');
INSERT INTO `sys_theme_item` VALUES (2, 'primary', '#2681FF');
INSERT INTO `sys_theme_item` VALUES (2, 'deSuccess', '#05AA65');
INSERT INTO `sys_theme_item` VALUES (2, 'deWarning', '#FF9227');
INSERT INTO `sys_theme_item` VALUES (2, 'deDanger', '#F64346');
INSERT INTO `sys_theme_item` VALUES (2, 'deInfo', '#BABEC5');
INSERT INTO `sys_theme_item` VALUES (2, 'deTextPrimary', '#F2F6FC');
INSERT INTO `sys_theme_item` VALUES (2, 'deTextRegular', '#EBEEF5');
INSERT INTO `sys_theme_item` VALUES (2, 'deTextSecondary', '#E4E7ED');
INSERT INTO `sys_theme_item` VALUES (2, 'deTextPlaceholder', '#DCDFE6');
INSERT INTO `sys_theme_item` VALUES (2, 'deBorderBase', '#495865');
INSERT INTO `sys_theme_item` VALUES (2, 'deBorderLight', '#495865');
INSERT INTO `sys_theme_item` VALUES (2, 'deBorderLighter', '#495865');
INSERT INTO `sys_theme_item` VALUES (2, 'deBorderExtraLight', '#495865');
INSERT INTO `sys_theme_item` VALUES (2, 'deWhite', '#21333B');
INSERT INTO `sys_theme_item` VALUES (2, 'deBlack', '#FFFFFF');
INSERT INTO `sys_theme_item` VALUES (2, 'deBackgroundBase', '#171B22');
INSERT INTO `sys_theme_item` VALUES (2, 'shade-1', 'rgb(34, 116, 230)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-1', 'rgb(60, 142, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-2', 'rgb(81, 154, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-3', 'rgb(103, 167, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-4', 'rgb(125, 179, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-5', 'rgb(147, 192, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-6', 'rgb(168, 205, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-7', 'rgb(190, 217, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-8', 'rgb(212, 230, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'light-9', 'rgb(233, 242, 255)');
INSERT INTO `sys_theme_item` VALUES (2, 'customMainBG', '#171B22');
INSERT INTO `sys_theme_item` VALUES (2, 'customContentBG', '#1B2A32');
INSERT INTO `sys_theme_item` VALUES (2, 'customTextPrimary', '#F2F6FC');
INSERT INTO `sys_theme_item` VALUES (2, 'customTopBG', '#00364D');
INSERT INTO `sys_theme_item` VALUES (2, 'customTextActive', '#FFFFFF');
INSERT INTO `sys_theme_item` VALUES (2, 'customTopTextColor', '#FAFAFA');
INSERT INTO `sys_theme_item` VALUES (2, 'customMenuHovorBG', '#28404D');
INSERT INTO `sys_theme_item` VALUES (2, 'customMenuActiveBG', '#324F62');
INSERT INTO `sys_theme_item` VALUES (2, 'customSiderBG', '#17242B');
INSERT INTO `sys_theme_item` VALUES (2, 'customSiderTextColor', '#ACBAC3');
INSERT INTO `sys_theme_item` VALUES (2, 'customTableBG', '#21333B');
INSERT INTO `sys_theme_item` VALUES (2, 'customTableColor', '#ACBAC3');
INSERT INTO `sys_theme_item` VALUES (2, 'customTableBorderColor', '#495865');
INSERT INTO `sys_theme_item` VALUES (1, 'primary', '#409EFF');
INSERT INTO `sys_theme_item` VALUES (1, 'deSuccess', '#67C23A');
INSERT INTO `sys_theme_item` VALUES (1, 'deWarning', '#E6A23C');
INSERT INTO `sys_theme_item` VALUES (1, 'deDanger', '#F56C6C');
INSERT INTO `sys_theme_item` VALUES (1, 'deInfo', '#909399');
INSERT INTO `sys_theme_item` VALUES (1, 'deTextPrimary', '#303133');
INSERT INTO `sys_theme_item` VALUES (1, 'deTextRegular', '#606266');
INSERT INTO `sys_theme_item` VALUES (1, 'deTextSecondary', '#909399');
INSERT INTO `sys_theme_item` VALUES (1, 'deTextPlaceholder', '#C0C4CC');
INSERT INTO `sys_theme_item` VALUES (1, 'deBorderBase', '#DCDFE6');
INSERT INTO `sys_theme_item` VALUES (1, 'deBorderLight', '#E4E7ED');
INSERT INTO `sys_theme_item` VALUES (1, 'deBorderLighter', '#EBEEF5');
INSERT INTO `sys_theme_item` VALUES (1, 'deBorderExtraLight', '#F2F6FC');
INSERT INTO `sys_theme_item` VALUES (1, 'deWhite', '#FFFFFF');
INSERT INTO `sys_theme_item` VALUES (1, 'deBlack', '#000000');
INSERT INTO `sys_theme_item` VALUES (1, 'deBackgroundBase', '#F5F7FA');
INSERT INTO `sys_theme_item` VALUES (1, 'shade-1', 'rgb(58, 142, 230)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-1', 'rgb(83, 168, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-2', 'rgb(102, 177, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-3', 'rgb(121, 187, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-4', 'rgb(140, 197, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-5', 'rgb(160, 207, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-6', 'rgb(179, 216, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-7', 'rgb(198, 226, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-8', 'rgb(217, 236, 255)');
INSERT INTO `sys_theme_item` VALUES (1, 'light-9', 'rgb(236, 245, 255)');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;