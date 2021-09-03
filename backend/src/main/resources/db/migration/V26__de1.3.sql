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
