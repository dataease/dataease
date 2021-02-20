CREATE TABLE IF NOT EXISTS `api_module` (
    `id`                     varchar(50) NOT NULL COMMENT 'Test case node ID',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Node name',
    `protocol`               varchar(64) NOT NULL COMMENT 'Node protocol',
    `parent_id`              varchar(50) DEFAULT NULL COMMENT 'Parent node ID',
    `level`                  int(10)  DEFAULT 1 COMMENT 'Node level',
    `pos`                    double DEFAULT NULL COMMENT 'Node order',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `api_definition` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `project_id` varchar(50)  NOT NULL COMMENT 'Project ID this test belongs to',
    `name` varchar(255)  NOT NULL COMMENT 'Test name',
    `method` varchar(64)  NOT NULL COMMENT 'method',
    `protocol` varchar(255)  NOT NULL COMMENT 'request protocol',
    `path` varchar(1000)  DEFAULT NULL COMMENT 'request path',
    `module_path` varchar(1000)  COMMENT 'module path',
    `description` longtext  DEFAULT NULL COMMENT 'Test description',
    `environment_id` varchar(50)  DEFAULT NULL COMMENT 'environment id',
    `request` longtext  COMMENT 'request (JSON format)',
    `response` longtext  COMMENT 'request (JSON format)',
    `schedule` varchar(255)  COMMENT 'Test schedule (cron list)',
    `status` varchar(64)  DEFAULT NULL COMMENT 'Status of this test',
    `module_id` varchar(50)  DEFAULT NULL COMMENT 'module_id of this module',
    `user_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `api_test_case` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `project_id` varchar(50)  NOT NULL COMMENT 'Project ID this test belongs to',
    `name` varchar(64)  NOT NULL COMMENT 'Test name',
    `priority` varchar(64)  NOT NULL COMMENT 'priority',
    `api_definition_id` varchar(50)  NOT NULL COMMENT 'api definition id',
    `description` longtext  DEFAULT NULL COMMENT 'Test description',
    `request` longtext  COMMENT 'request (JSON format)',
    `response` longtext  COMMENT 'response (JSON format)',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `api_definition_exec_result` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `name` varchar(64)  NOT NULL COMMENT 'Test name',
    `resource_id` varchar(50)  NOT NULL COMMENT 'api id or testcase id ',
    `content` longtext  COMMENT 'request (JSON format)',
    `status` varchar(50)  COMMENT 'execute status',
    `user_id` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `start_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `end_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    `create_time` bigint(13) NULL COMMENT 'Create time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `api_definition` ADD INDEX ( `user_id` );
ALTER TABLE `api_definition` ADD INDEX ( `project_id` );
ALTER TABLE `api_test_case` ADD INDEX ( `api_definition_id` );
ALTER TABLE `api_test_case` ADD INDEX ( `create_user_id` );
ALTER TABLE `api_test_case` ADD INDEX ( `update_user_id` );
ALTER TABLE `api_definition_exec_result` ADD INDEX ( `resource_id` );
