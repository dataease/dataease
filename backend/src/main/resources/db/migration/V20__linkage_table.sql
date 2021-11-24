SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for panel_view_linkage
-- ----------------------------
CREATE TABLE `panel_view_linkage` (
  `id` varchar(50) NOT NULL,
  `panel_id` varchar(50) DEFAULT NULL,
  `source_view_id` varchar(50) DEFAULT NULL COMMENT '源视图id',
  `target_view_id` varchar(50) DEFAULT NULL COMMENT '联动视图id',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  `update_people` varchar(255) DEFAULT NULL COMMENT '更新人',
  `ext1` varchar(2000) DEFAULT NULL,
  `ext2` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for panel_view_linkage_field
-- ----------------------------
CREATE TABLE `panel_view_linkage_field` (
  `id` varchar(50) NOT NULL,
  `linkage_id` varchar(50) DEFAULT NULL COMMENT '联动ID',
  `source_field` varchar(255) DEFAULT NULL COMMENT '源视图字段',
  `target_field` varchar(255) DEFAULT NULL COMMENT '目标视图字段',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
