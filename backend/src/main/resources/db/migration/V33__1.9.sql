ALTER TABLE `chart_view` ADD COLUMN `senior` LONGTEXT COMMENT '高级' AFTER `drill_fields`;
UPDATE `chart_view` SET `senior` = '{}';
