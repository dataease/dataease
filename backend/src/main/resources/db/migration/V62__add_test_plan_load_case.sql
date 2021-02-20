CREATE TABLE IF NOT EXISTS `test_plan_load_case`
(
    `id`             varchar(50) NOT NULL COMMENT 'ID',
    `test_plan_id`   varchar(50) NOT NULL COMMENT 'Test plan ID',
    `load_case_id`    varchar(50) NOT NULL COMMENT 'Load test case ID',
    `status`         varchar(50) DEFAULT NULL COMMENT 'Load case status',
    `load_report_id` varchar(50) DEFAULT NULL COMMENT 'Load report id',
    `create_time`    bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`    bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`),
    UNIQUE KEY `plan_load_case_id` (`test_plan_id`, `load_case_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;