ALTER TABLE `chart_view` ADD COLUMN `x_axis_ext` LONGTEXT COMMENT 'table-row' AFTER `x_axis`;
UPDATE `chart_view` SET `x_axis_ext` = '[]';