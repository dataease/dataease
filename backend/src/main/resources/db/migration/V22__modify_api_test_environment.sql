ALTER TABLE api_test_environment MODIFY COLUMN protocol varchar(20) NULL COMMENT 'Api Test Protocol';
ALTER TABLE api_test_environment MODIFY COLUMN socket varchar(225) NULL COMMENT 'Api Test Socket';
ALTER TABLE api_test_environment MODIFY COLUMN `domain` varchar(225) NULL COMMENT 'Api Test Domain';
ALTER TABLE api_test_environment CHANGE custom_data `config` longtext COMMENT 'Config Data (JSON format)';
