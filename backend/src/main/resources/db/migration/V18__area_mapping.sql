ALTER TABLE `chart_view` ADD COLUMN `ext_stack` LONGTEXT COMMENT '堆叠项' AFTER `y_axis`;
UPDATE `chart_view` SET `ext_stack` = '[]';



ALTER TABLE `chart_view` ADD COLUMN `drill_fields` LONGTEXT COMMENT '钻取字段' AFTER `custom_filter`;
UPDATE `chart_view` SET `drill_fields` = '[]';

