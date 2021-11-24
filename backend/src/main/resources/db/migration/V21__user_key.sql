-- ----------------------------
-- Table structure for user_key
-- ----------------------------
CREATE TABLE `user_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `access_key` varchar(50) NOT NULL COMMENT 'access_key',
  `secret_key` varchar(50) NOT NULL COMMENT 'secret key',
  `create_time` bigint(13) NOT NULL COMMENT '创建时间',
  `status` varchar(10) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `IDX_AK` (`access_key`),
  KEY `IDX_USER_K_ID` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=17 ROW_FORMAT=COMPACT COMMENT='用户KEY';
