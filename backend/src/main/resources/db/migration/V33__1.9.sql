ALTER TABLE `chart_view` ADD COLUMN `senior` LONGTEXT COMMENT '高级' AFTER `drill_fields`;
UPDATE `chart_view` SET `senior` = '{}';

CREATE TABLE `chart_view_cache` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(1024) DEFAULT NULL COMMENT '名称',
  `title` varchar(1024) DEFAULT NULL COMMENT 'EChart标题',
  `scene_id` varchar(50) NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
  `table_id` varchar(50) NOT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `render` varchar(50) DEFAULT NULL COMMENT '视图渲染方式',
  `result_count` int(10) DEFAULT NULL COMMENT '展示结果',
  `result_mode` varchar(50) DEFAULT NULL COMMENT '展示模式',
  `x_axis` longtext COMMENT '横轴field',
  `x_axis_ext` longtext COMMENT 'table-row',
  `y_axis` longtext COMMENT '纵轴field',
  `y_axis_ext` longtext COMMENT '副轴',
  `ext_stack` longtext COMMENT '堆叠项',
  `ext_bubble` longtext COMMENT '气泡大小',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_style` longtext COMMENT '组件样式',
  `custom_filter` longtext COMMENT '结果过滤',
  `drill_fields` longtext COMMENT '钻取字段',
  `senior` longtext COMMENT '高级',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 视图',
  `chart_type` varchar(255) DEFAULT 'private' COMMENT '视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板',
  `is_plugin` bit(1) DEFAULT NULL COMMENT '是否插件',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
