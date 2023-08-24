-- ----------------------------
-- Records of sys_msg_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_msg_type` VALUES (7, 0, 'i18n_msg_type_ds_invalid', 'datasource', 'to-msg-ds');
INSERT INTO `sys_msg_type` VALUES (8, 7, 'i18n_msg_type_ds_invalid', 'datasource', 'to-msg-ds');
COMMIT;


-- ----------------------------
-- Table structure for panel_pdf_template
-- ----------------------------
CREATE TABLE `panel_pdf_template` (
  `id` varchar(50) NOT NULL COMMENT 'id',
  `name` varchar(255) DEFAULT NULL COMMENT '模板名称',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) DEFAULT NULL COMMENT '创建人',
  `template_content` longtext COMMENT '模板内容',
  `sort` int(8) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `panel_pdf_template` (`id`, `name`, `create_time`, `create_user`, `template_content`, `sort`) VALUES ('1', '默认模板(加参数样式)', NULL, NULL, '<div style=\"margin-top: 5px\">\n    <div contenteditable=\"true\"> \n		仪表板名称：$panelName$ </br>\n		导出时间：$yyyy-MM-dd hh:mm:ss$ </br>\n		导出人：$nickName$ </br>\n		这里可以输入其他内容\n		</div>\n    <div>\n      <img width=\"100%\" src=\"$snapshot$\">\n    </div>\n  </div>', 1);
INSERT INTO `panel_pdf_template` (`id`, `name`, `create_time`, `create_user`, `template_content`, `sort`) VALUES ('2', '默认模板(只截图)', NULL, NULL, '\n<div style=\"margin-top: 5px\">\n    <div>\n      <img width=\"100%\" src=\"$snapshot$\">\n    </div>\n  </div>', 2);

ALTER TABLE `chart_view` ADD COLUMN `y_axis_ext` LONGTEXT COMMENT '副轴' AFTER `y_axis`;
UPDATE `chart_view` SET `y_axis_ext` = '[]';


ALTER TABLE `sys_user` ADD COLUMN `from` int(4) NOT NULL COMMENT '来源' AFTER `language`;
-- ----------------------------
-- INSERT INTO `sys_menu` VALUES (60, 1, 0, 1, '导入LDAP用户', 'system-user-import', 'system/user/imp-ldap', 11, NULL, 'user-ldap', b'0', b'0', b'1', 'user:import', NULL, NULL, NULL, NULL);
-- ----------------------------
BEGIN;
INSERT INTO `system_parameter` VALUES ('ui.themeStr', 'light', 'text', 12);
INSERT INTO `system_parameter` VALUES ('ldap.url', NULL, 'text', 1);
INSERT INTO `system_parameter` VALUES ('ldap.dn', NULL, 'text', 2);
INSERT INTO `system_parameter` VALUES ('ldap.password', NULL, 'password', 3);
INSERT INTO `system_parameter` VALUES ('ldap.ou', NULL, 'text', 4);
INSERT INTO `system_parameter` VALUES ('ldap.mapping', NULL, 'text', 6);
INSERT INTO `system_parameter` VALUES ('ldap.open', NULL, 'text', 7);

INSERT INTO `system_parameter` VALUES ('oidc.authEndpoint', NULL, 'text', 1);
INSERT INTO `system_parameter` VALUES ('oidc.tokenEndpoint', NULL, 'text', 2);
INSERT INTO `system_parameter` VALUES ('oidc.userinfoEndpoint', NULL, 'text', 3);
INSERT INTO `system_parameter` VALUES ('oidc.logoutEndpoint', NULL, 'text', 4);
INSERT INTO `system_parameter` VALUES ('oidc.clientId', NULL, 'text', 5);
INSERT INTO `system_parameter` VALUES ('oidc.secret', NULL, 'password', 6);
INSERT INTO `system_parameter` VALUES ('oidc.scope', NULL, 'text', 7);
INSERT INTO `system_parameter` VALUES ('oidc.redirectUrl', NULL, 'text', 8);
INSERT INTO `system_parameter` VALUES ('oidc.open', NULL, 'text', 9);
COMMIT;

ALTER TABLE `sys_user` ADD COLUMN `sub` varchar(255)  COMMENT 'oidc用户ID' AFTER `from`;

CREATE INDEX dataset_table_task_log_index ON dataset_table_task_log (id,task_id,table_id);
CREATE INDEX dataset_table_task_index ON dataset_table_task (id);
CREATE INDEX dataset_table_index ON dataset_table (id);
