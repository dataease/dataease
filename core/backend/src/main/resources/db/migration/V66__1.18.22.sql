ALTER TABLE `export_task` ADD COLUMN `msg` LONGTEXT NULL COMMENT '错误信息' AFTER `params`;
