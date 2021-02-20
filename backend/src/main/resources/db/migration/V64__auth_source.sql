CREATE TABLE IF NOT EXISTS `auth_source` (
    `id`            varchar(50) NOT NULL,
    `configuration` text        NOT NULL,
    `status`        varchar(64) NOT NULL,
    `create_time`   bigint(13)  NOT NULL,
    `update_time`   bigint(13)  NOT NULL,
    `description`   varchar(255) DEFAULT NULL,
    `name`          varchar(60)  DEFAULT NULL,
    `type`          varchar(30)  DEFAULT NULL,
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

