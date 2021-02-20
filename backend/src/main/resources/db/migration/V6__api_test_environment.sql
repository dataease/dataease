CREATE TABLE IF NOT EXISTS `api_test_environment` (
  `id` varchar(50) NOT NULL COMMENT 'Api Test Environment ID',
  `name` varchar(64) NOT NULL COMMENT 'Api Test Environment Name',
  `project_id` varchar(50) NOT NULL COMMENT 'Project ID',
  `protocol` varchar(20) NOT NULL COMMENT 'Api Test Protocol',
  `socket` varchar(225) NOT NULL COMMENT 'Api Test Socket',
  `domain` varchar(225) NOT NULL COMMENT 'Api Test Domain',
  `port` int(10) DEFAULT NULL COMMENT 'Api Test Port',
  `variables` text DEFAULT NULL COMMENT 'Global ariables',
  `headers` text DEFAULT NULL COMMENT 'Global Heards',
  `custom_data` longtext COMMENT 'Custom Data (JSON format)',
  PRIMARY KEY (`id`),
  KEY `project_id` ( `project_id` )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
