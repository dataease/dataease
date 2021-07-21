ALTER TABLE `chart_view` ADD COLUMN `ext_stack` LONGTEXT COMMENT '堆叠项' AFTER `y_axis`;

UPDATE `chart_view` SET `ext_stack` = '[]';
UPDATE `chart_view` SET `custom_filter` = '[]';