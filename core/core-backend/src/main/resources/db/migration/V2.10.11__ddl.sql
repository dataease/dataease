DROP TABLE IF EXISTS `core_export_download_task`;
CREATE TABLE `core_export_download_task`
(
    `id`          varchar(255)  NOT NULL,
    `create_time` bigint(20)    DEFAULT NULL,
    `valid_time`   bigint(20)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) COMMENT='下载任务列表';
