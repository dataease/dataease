-- ----------------------------
-- Table structure for sys_msg
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg`;
CREATE TABLE `sys_msg` (
  `msg_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type_id` bigint(20) NOT NULL COMMENT '类型',
  `status` tinyint(1) NOT NULL COMMENT '状态',
  `param` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `create_time` bigint(13) NOT NULL COMMENT '发送时间',
  `read_time` bigint(13) DEFAULT NULL COMMENT '读取时间',
  `content` varchar(255) DEFAULT NULL COMMENT '消息内容',
  PRIMARY KEY (`msg_id`) USING BTREE,
  KEY `inx_msg_userid` (`user_id`) USING BTREE,
  KEY `inx_msg_type` (`type_id`) USING BTREE,
  KEY `inx_msg_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息通知表';

-- ----------------------------
-- Table structure for sys_msg_channel
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg_channel`;
CREATE TABLE `sys_msg_channel` (
  `msg_channel_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `channel_name` varchar(255) DEFAULT NULL COMMENT '渠道名称',
  PRIMARY KEY (`msg_channel_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='消息渠道表';

-- ----------------------------
-- Records of sys_msg_channel
-- ----------------------------
BEGIN;
INSERT INTO `sys_msg_channel` VALUES (1, 'webmsg.channel_inner_msg');
COMMIT;


-- ----------------------------
-- Table structure for sys_msg_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg_type`;
CREATE TABLE `sys_msg_type` (
  `msg_type_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` bigint(20) NOT NULL COMMENT '父类ID',
  `type_name` varchar(255) DEFAULT NULL COMMENT '类型名称',
  `router` varchar(255) DEFAULT NULL COMMENT '跳转路由',
  `callback` varchar(255) DEFAULT NULL COMMENT '回调方法',
  PRIMARY KEY (`msg_type_id`) USING BTREE,
  KEY `inx_msgtype_pid` (`pid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='消息类型表';

-- ----------------------------
-- Records of sys_msg_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_msg_type` VALUES (1, 0, 'i18n_msg_type_panel_share', 'panel', 'to-msg-share');
INSERT INTO `sys_msg_type` VALUES (2, 1, 'i18n_msg_type_panel_share', 'panel', 'to-msg-share');
INSERT INTO `sys_msg_type` VALUES (3, 1, 'i18n_msg_type_panel_share_cacnel', 'panel', 'to-msg-share');
INSERT INTO `sys_msg_type` VALUES (4, 0, 'i18n_msg_type_dataset_sync', 'sys-task-dataset', 'to-msg-dataset');
INSERT INTO `sys_msg_type` VALUES (5, 4, 'i18n_msg_type_dataset_sync_success', 'sys-task-dataset', 'to-msg-dataset');
INSERT INTO `sys_msg_type` VALUES (6, 4, 'i18n_msg_type_dataset_sync_faild', 'sys-task-dataset', 'to-msg-dataset');
COMMIT;

-- ----------------------------
-- Table structure for sys_msg_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg_setting`;
CREATE TABLE `sys_msg_setting` (
  `msg_setting_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type_id` bigint(20) NOT NULL COMMENT '类型ID',
  `channel_id` bigint(20) NOT NULL COMMENT '渠道ID',
  `enable` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  PRIMARY KEY (`msg_setting_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息设置表';

BEGIN;
INSERT INTO `sys_menu` VALUES (53, 1, 3, 1, '站内消息', 'sys-msg-web', 'msg/index', 1000, 'all-msg', 'system-msg-web', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (54, 53, 0, 1, '所有消息', 'sys-msg-web-all', 'msg/all', 1, 'web-msg', 'all', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (55, 53, 0, 1, '未读消息', 'sys-msg-web-unread', 'msg/unread', 2, 'unread-msg', 'unread', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (56, 53, 0, 1, '已读消息', 'sys-msg-web-readed', 'msg/readed', 3, 'readed-msg', 'readed', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (59, 53, 0, 1, '接收管理', 'sys-msg-setting', 'msg/setting', 4, 'msg-setting', 'setting', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
COMMIT;

BEGIN;
UPDATE `sys_menu` SET permission = null WHERE menu_id = 1;
COMMIT;

