ALTER TABLE `chart_view` ADD COLUMN `render` varchar(50)  COMMENT '视图渲染方式' AFTER `type`;
UPDATE `chart_view` SET `render` = 'echarts' WHERE `type` != 'liquid';
UPDATE `chart_view` SET `render` = 'antv' WHERE `type` = 'liquid';



ALTER TABLE `panel_link` ADD COLUMN `over_time` bigint(13) NULL DEFAULT NULL COMMENT '有效截止时间' AFTER `pwd`;

BEGIN;
INSERT INTO `sys_menu` VALUES (6, 1, 0, 1, '系统参数', 'system-param', 'system/SysParam/index', 6, 'sys-tools', 'system-param', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);
COMMIT;