ALTER TABLE `dataset_table` ADD COLUMN `sync_status` VARCHAR(45) NULL AFTER `create_time`;
ALTER TABLE `dataset_table` ADD COLUMN `qrtz_instance` VARCHAR(1024) NULL AFTER `create_time`;
