CREATE TABLE IF NOT EXISTS `test_plan_api_case` (
    `id` varchar(50)  NOT NULL COMMENT 'ID',
    `test_plan_id` varchar(50)   NOT NULL COMMENT 'Test plan ID',
    `api_case_id` varchar(50)  NOT NULL COMMENT 'Api test case ID',
    `status` varchar(50) DEFAULT NULL COMMENT 'Api case status',
    `environment_id` varchar(50) NULL COMMENT 'Relevance environment_id',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`),
    UNIQUE KEY `plan_id_case_id` (`test_plan_id`, `api_case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `test_plan_api_scenario` (
    `id` varchar(50)  NOT NULL COMMENT 'ID',
    `test_plan_id` varchar(50)   NOT NULL COMMENT 'Test plan ID',
    `api_scenario_id` varchar(50)  NOT NULL COMMENT 'Api scenario case ID',
    `status` varchar(50) DEFAULT NULL COMMENT 'Scenario case status',
    `environment_id` varchar(50) NULL COMMENT 'Relevance environment_id',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    `pass_rate` varchar(100) DEFAULT NULL,
    `last_result` varchar(100) DEFAULT NULL,
    `report_id` varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `plan_id_scenario_id` (`test_plan_id`, `api_scenario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE api_definition_exec_result ADD `type` varchar(20) NULL;
