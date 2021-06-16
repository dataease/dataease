ALTER TABLE `dataset_table` ADD COLUMN `last_update_time` BIGINT(13) NULL DEFAULT 0 AFTER `sync_status`;
