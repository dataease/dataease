CREATE TABLE IF NOT EXISTS `test_plan_report` (
    `id` VARCHAR ( 50 ) NOT NULL COMMENT 'ID',
    `test_plan_id` VARCHAR ( 50 ) NOT NULL COMMENT 'Test plan ID',
    `create_time` BIGINT ( 13 ) NOT NULL COMMENT 'Create timestamp',
    `update_time` BIGINT ( 13 ) NOT NULL COMMENT 'Update timestamp',
    `name` VARCHAR ( 64 ) DEFAULT NULL COMMENT 'name',
    `status` VARCHAR ( 50 ) DEFAULT NULL COMMENT 'report status',
    `trigger_mode` VARCHAR ( 50 ) DEFAULT NULL COMMENT 'test plan execute triggerMode',
    `creator` VARCHAR ( 50 ) DEFAULT NULL COMMENT 'report creator',
    `start_time` BIGINT ( 13 ) DEFAULT NULL COMMENT 'report startTime',
    `end_time` BIGINT ( 13 ) DEFAULT NULL COMMENT 'report timestamp',
    `is_api_case_executing` TINYINT NOT NULL COMMENT 'is Api Case executing',
    `is_scenario_executing` TINYINT NOT NULL COMMENT 'is scenario Case executing',
    `is_performance_executing` TINYINT NOT NULL COMMENT 'is performance executing',
    `principal` VARCHAR ( 50 ) DEFAULT NULL COMMENT 'principal',
    PRIMARY KEY ( `id` ),
    UNIQUE KEY `executeInfoID` ( `test_plan_id`, `create_time` )
)
    ENGINE = INNODB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `test_plan_report_data` (
    `id` VARCHAR ( 50 ) NOT NULL COMMENT 'ID',
    `test_plan_report_id` VARCHAR ( 50 ) NOT NULL COMMENT 'Test plan ID',
    `execute_result` longtext  COMMENT 'executeResult (JSON format)',
    `failur_test_cases` longtext  COMMENT 'failurTestCases (JSON format)',
    `module_execute_result` longtext  COMMENT 'moduleExecuteResult (JSON format)',
    `api_case_info` longtext  COMMENT 'apiCaseID list (JSON format)',
    `scenario_info` longtext  COMMENT 'scenarioID list (JSON format)',
    `performance_info` longtext  COMMENT 'performanceID list (JSON format)',
    `issues_info` longtext  COMMENT 'issues (JSON format)',
    PRIMARY KEY ( `id` ),
    UNIQUE KEY `test_plan_report_id` ( `test_plan_report_id` )
)
    ENGINE = INNODB
    DEFAULT CHARSET = utf8mb4;