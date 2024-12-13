UPDATE `visualization_background` SET `name` = 'Board10' WHERE `id` = 'dark_1';
UPDATE `visualization_subject` SET `name` = 'chart.light_theme' WHERE `id` = '10001';
UPDATE `visualization_subject` SET `name` = 'chart.dark_theme' WHERE `id` = '10002';
ALTER TABLE core_chart_view ADD COLUMN sort_priority longtext null comment '字段排序优先级';