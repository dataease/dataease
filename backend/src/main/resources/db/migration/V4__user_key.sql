CREATE TABLE `user_key` (
    `id`          varchar(50) NOT NULL DEFAULT '' COMMENT 'user_key ID',
    `user_id`     varchar(50) NOT NULL COMMENT '用户ID',
    `access_key`  varchar(50) NOT NULL COMMENT 'access_key',
    `secret_key`  varchar(50) NOT NULL COMMENT 'secret key',
    `create_time` bigint(13)  NOT NULL COMMENT '创建时间',
    `status`      varchar(10)          DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (`id`),
    UNIQUE KEY `IDX_AK` (`access_key`),
    KEY `IDX_USER_ID` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

