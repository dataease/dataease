-- dataset start
CREATE TABLE IF NOT EXISTS `dataset_group` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `pid` varchar(50) COMMENT '父级ID',
  `level` int(10) COMMENT '当前分组处于第几级',
  `type` varchar(50) COMMENT 'group or scene',
  `create_by` varchar(50)  COMMENT '创建人ID',
  `create_time` bigint(13) COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- dataset end
