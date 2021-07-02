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