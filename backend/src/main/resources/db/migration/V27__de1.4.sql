ALTER TABLE `chart_view` ADD COLUMN `render` varchar(50)  COMMENT '视图渲染方式' AFTER `type`;
UPDATE `chart_view` SET `render` = 'echarts' WHERE `type` != 'liquid';
UPDATE `chart_view` SET `render` = 'antv' WHERE `type` = 'liquid';



ALTER TABLE `panel_link` ADD COLUMN `over_time` bigint(13) NULL DEFAULT NULL COMMENT '有效截止时间' AFTER `pwd`;


CREATE TABLE `panel_link_jump` (
  `id` varchar(50) NOT NULL,
  `source_panel_id` varchar(50) DEFAULT NULL COMMENT '源仪表板ID',
  `source_view_id` varchar(50) DEFAULT NULL COMMENT '源视图ID',
  `link_jump_info` varchar(4000) DEFAULT NULL COMMENT '跳转信息',
  `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `panel_link_jump_info` (
  `id` varchar(50) NOT NULL,
  `link_jump_id` varchar(50) DEFAULT NULL COMMENT 'link jump ID',
  `link_type` varchar(255) DEFAULT NULL COMMENT '关联类型 inner 内部仪表板，outer 外部链接',
  `jump_type` varchar(255) DEFAULT NULL COMMENT '跳转类型 _blank 新开页面 _self 当前窗口',
  `target_panel_id` varchar(255) DEFAULT NULL COMMENT '关联仪表板ID',
  `source_field_id` varchar(255) DEFAULT NULL COMMENT '字段ID',
  `content` varchar(4000) DEFAULT NULL COMMENT '内容 linkType = outer时使用',
  `checked` tinyint(1) DEFAULT NULL COMMENT '是否可用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `panel_link_jump_target_view_info` (
  `target_id` varchar(50) NOT NULL,
  `link_jump_info_id` varchar(50) DEFAULT NULL,
  `target_view_id` varchar(50) DEFAULT NULL,
  `target_field_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`target_id`) USING BTREE
) ENGINE=InnoDB ;

BEGIN;
INSERT INTO `sys_menu` VALUES (6, 1, 0, 1, '系统参数', 'system-param', 'system/SysParam/index', 6, 'sys-tools', 'system-param', b'0', b'0', b'0', 'sysparam:read', NULL, NULL, NULL, NULL);
COMMIT;
