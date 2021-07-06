-- ----------------------------
-- Table structure for sys_msg
-- ----------------------------
DROP TABLE IF EXISTS `sys_msg`;
CREATE TABLE `sys_msg` (
  `msg_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` int(4) NOT NULL COMMENT '类型',
  `status` tinyint(1) NOT NULL COMMENT '状态',
  `router` varchar(255) DEFAULT NULL COMMENT '跳转路由',
  `param` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `create_time` bigint(13) NOT NULL COMMENT '发送时间',
  `read_time` bigint(13) DEFAULT NULL COMMENT '读取时间',
  `content` varchar(255) DEFAULT NULL COMMENT '消息内容',
  PRIMARY KEY (`msg_id`) USING BTREE,
  KEY `inx_msg_userid` (`user_id`) USING BTREE,
  KEY `inx_msg_type` (`type`) USING BTREE,
  KEY `inx_msg_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='消息通知表';

BEGIN;
INSERT INTO `sys_menu` VALUES (53, 1, 3, 1, '站内消息', 'sys-msg-web', 'msg/index', 1000, 'all-msg', 'system-msg-web', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (54, 53, 0, 1, '所有消息', 'sys-msg-web-all', 'msg/all', 1, 'web-msg', 'all', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (55, 53, 0, 1, '未读消息', 'sys-msg-web-unread', 'msg/unread', 2, 'unread-msg', 'unread', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (56, 53, 0, 1, '已读消息', 'sys-msg-web-readed', 'msg/readed', 3, 'readed-msg', 'readed', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
COMMIT;

BEGIN;
UPDATE `sys_menu` SET permission = null WHERE menu_id = 1;
COMMIT;

