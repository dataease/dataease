CREATE TABLE IF NOT EXISTS `quota`
(
    `id`              varchar(50)   NOT NULL,
    `api`             int(10)       DEFAULT NULL,
    `performance`     int(10)       DEFAULT NULL,
    `max_threads`     int(10)       DEFAULT NULL,
    `duration`        int(10)       DEFAULT NULL,
    `resource_pool`   varchar(1000) DEFAULT NULL,
    `organization_id` varchar(50)   DEFAULT NULL,
    `workspace_id`    varchar(50)   DEFAULT NULL,
    `use_default`     tinyint(1)    DEFAULT NULL,
    `update_time`     bigint(13)    DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4