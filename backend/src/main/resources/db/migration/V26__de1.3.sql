-- ----------------------------
-- Records of sys_msg_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_msg_type` VALUES (7, 0, 'i18n_msg_type_ds_invalid', 'datasource', 'to-msg-ds');
INSERT INTO `sys_msg_type` VALUES (8, 7, 'i18n_msg_type_ds_invalid', 'datasource', 'to-msg-ds');
COMMIT;

-- ----------------------------
-- Table structure for system_parameter
-- ----------------------------
DROP TABLE IF EXISTS `system_parameter`;
CREATE TABLE `system_parameter` (
  `param_key` varchar(64) NOT NULL COMMENT 'Parameter name',
  `param_value` varchar(255) DEFAULT NULL COMMENT 'Parameter value',
  `type` varchar(100) NOT NULL DEFAULT 'text' COMMENT 'Parameter type',
  `sort` int(5) DEFAULT NULL COMMENT 'Sort',
  PRIMARY KEY (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of system_parameter
-- ----------------------------
BEGIN;
INSERT INTO `system_parameter` VALUES ('', NULL, 'text', NULL);
INSERT INTO `system_parameter` VALUES ('default.language', 'zh_CN', 'text', -1);
INSERT INTO `system_parameter` VALUES ('ui.favicon', NULL, 'file', -1);
INSERT INTO `system_parameter` VALUES ('ui.loginImage', NULL, 'file', 4);
INSERT INTO `system_parameter` VALUES ('ui.loginLogo', NULL, 'file', 5);
INSERT INTO `system_parameter` VALUES ('ui.loginTitle', NULL, 'text', 3);
INSERT INTO `system_parameter` VALUES ('ui.logo', NULL, 'file', 6);
INSERT INTO `system_parameter` VALUES ('ui.theme', NULL, 'text', 2);
INSERT INTO `system_parameter` VALUES ('ui.title', NULL, 'text', 1);
INSERT INTO `system_parameter` VALUES ('ui.topMenuActiveColor', NULL, 'text', 8);
INSERT INTO `system_parameter` VALUES ('ui.topMenuColor', NULL, 'text', 7);
INSERT INTO `system_parameter` VALUES ('ui.topMenuTextActiveColor', NULL, 'text', 10);
INSERT INTO `system_parameter` VALUES ('ui.topMenuTextColor', NULL, 'text', 9);
COMMIT;

-- ----------------------------
-- Table structure for panel_pdf_template
-- ----------------------------
DROP TABLE IF EXISTS `panel_pdf_template`;
CREATE TABLE `panel_pdf_template` (
  `id` varchar(50) NOT NULL COMMENT 'id',
  `name` varchar(255) DEFAULT NULL COMMENT '模板名称',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) DEFAULT NULL COMMENT '创建人',
  `template_content` longtext COMMENT '模板内容',
  `sort` int(8) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

