ALTER TABLE `chart_view` ADD COLUMN `render` varchar(50)  COMMENT '视图渲染方式' AFTER `type`;
UPDATE `chart_view` SET `render` = 'echarts' WHERE `type` != 'liquid';
UPDATE `chart_view` SET `render` = 'antv' WHERE `type` = 'liquid';
