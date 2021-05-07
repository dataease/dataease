-- chart start
DROP TABLE IF EXISTS `chart_group`;
CREATE TABLE IF NOT EXISTS `chart_group`
(
    `id`          varchar(50) NOT NULL COMMENT 'ID',
    `name`        varchar(64) NOT NULL COMMENT '名称',
    `pid`         varchar(50) COMMENT '父级ID',
    `level`       int(10) COMMENT '当前分组处于第几级',
    `type`        varchar(50) COMMENT 'group or scene',
    `create_by`   varchar(50) COMMENT '创建人ID',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
DROP TABLE IF EXISTS `chart_view`;
CREATE TABLE `chart_view` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `scene_id` varchar(50) NOT NULL COMMENT '场景ID',
  `table_id` varchar(50) NOT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `title` varchar(50) DEFAULT NULL COMMENT 'EChart标题',
  `x_axis` longtext COMMENT '横轴field',
  `y_axis` longtext COMMENT '纵轴field',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_style` longtext COMMENT '组件样式',
  `custom_filter` longtext COMMENT '结果过滤',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表盘 view 视图',
  PRIMARY KEY (`id`),
  KEY `IDX_TABLE_ID` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chart end
