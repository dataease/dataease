ALTER TABLE test_plan ADD api_ids varchar(1000) NULL COMMENT 'Api id list';
ALTER TABLE test_plan ADD scenario_ids varchar(1000) NULL COMMENT 'Scenario id list';

CREATE TABLE `api_scenario` (
  `id` varchar(50) NOT NULL COMMENT 'Test ID',
  `project_id` varchar(50) NOT NULL COMMENT 'Project ID this test belongs to',
  `tag_id` varchar(800) DEFAULT NULL COMMENT 'tag id',
  `user_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
  `api_scenario_module_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
  `module_path` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL COMMENT 'api scenario name',
  `level` varchar(100) DEFAULT NULL COMMENT 'api scenario level ',
  `status` varchar(100) NOT NULL COMMENT 'api scenario status ',
  `principal` varchar(100) NOT NULL COMMENT 'api scenario principal ',
  `step_total` int(11) DEFAULT '0' COMMENT 'Step total ',
  `follow_people` varchar(100) DEFAULT NULL COMMENT 'api scenario Follow people ',
  `schedule` varchar(255) DEFAULT NULL COMMENT 'Test schedule (cron list)',
  `scenario_definition` longtext COMMENT 'Test scenario_definition json',
  `description` longtext DEFAULT NULL COMMENT 'api scenario description',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  `pass_rate` varchar(100) DEFAULT NULL,
  `last_result` varchar(100) DEFAULT NULL,
  `report_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `api_scenario_module` (
  `id` varchar(50) NOT NULL COMMENT 'Test case node ID',
  `project_id` varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
  `name` varchar(64) NOT NULL COMMENT 'Node name',
  `parent_id` varchar(50) DEFAULT NULL COMMENT 'Parent node ID',
  `level` int(10) DEFAULT '1' COMMENT 'Node level',
  `pos` double DEFAULT NULL COMMENT 'Node order',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `api_scenario_report` (
  `id` varchar(50) NOT NULL COMMENT 'Test report ID',
  `project_id` varchar(50) NOT NULL COMMENT 'scenario ID this test report belongs to',
  `name` varchar(64) NOT NULL COMMENT 'Test report name',
  `description` longtext DEFAULT NULL COMMENT 'Test report name',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  `status` varchar(64) NOT NULL COMMENT 'Status of this test run',
  `user_id` varchar(64) DEFAULT NULL,
  `trigger_mode` varchar(64) DEFAULT NULL,
  `execute_type` varchar(200) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `api_scenario_report_detail` (
  `report_id` varchar(64) NOT NULL COMMENT 'API Test Report ID',
  `project_id` varchar(64) NOT NULL COMMENT 'scenario ID',
  `content` longblob COMMENT 'Report Content',
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;