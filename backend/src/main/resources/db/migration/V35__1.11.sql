ALTER TABLE `chart_view` ADD COLUMN `custom_sort` LONGTEXT COMMENT '自定义排序';
UPDATE `chart_view` SET `custom_sort` = '[]';
ALTER TABLE `chart_view_cache` ADD COLUMN `custom_sort` LONGTEXT COMMENT '自定义排序';
UPDATE `chart_view_cache` SET `custom_sort` = '[]';
