 ALTER TABLE `datasource`
    ADD COLUMN `version` varchar(255) NULL COMMENT '版本' AFTER `status`;

