UPDATE `visualization_background` SET `name` = 'Board10' WHERE `id` = 'dark_1';
UPDATE `visualization_subject` SET `name` = 'chart.light_theme' WHERE `id` = '10001';
UPDATE `visualization_subject` SET `name` = 'chart.dark_theme' WHERE `id` = '10002';
ALTER TABLE core_chart_view ADD COLUMN sort_priority longtext null comment '字段排序优先级';
DELETE FROM area where id = '156320571';
CREATE INDEX idx_dataset_table_task_log_A ON core_datasource_task_log(ds_id, table_name, start_time);

delete  from visualization_background;
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_1', '1', 'default', '', NULL, NULL, NULL, 'img/board', 'board/board_1.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_2', '2', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_2.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_3', '3', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_3.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_4', '4', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_4.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_5', '5', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_5.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_6', '6', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_6.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_7', '7', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_7.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_8', '8', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_8.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_9', '9', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_9.svg');

DELETE FROM area WHERE id='156500200';
UPDATE area SET pid='156500100' WHERE pid='156500200';
UPDATE area SET name='万州区' WHERE id='156500101';