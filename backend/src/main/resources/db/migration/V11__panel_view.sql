DROP TABLE IF EXISTS `panel_view`;
CREATE TABLE `panel_view` (
  `id` varchar(50) NOT NULL,
  `panel_id` varchar(50) DEFAULT NULL COMMENT 'panel_id',
  `chart_view_id` varchar(50) DEFAULT NULL COMMENT 'chart_view_id',
  `content` blob COMMENT '内容',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;
