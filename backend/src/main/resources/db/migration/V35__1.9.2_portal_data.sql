CREATE TABLE `portal_data` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(10) DEFAULT NULL COMMENT '创建人id',
  `user_name` varchar(225) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '创建人名称',
  `update_by` varchar(225) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `position_json` text CHARACTER SET utf8mb4,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据门户'
