CREATE TABLE `license` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  `license_code` longtext DEFAULT NULL COMMENT 'license_code',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;