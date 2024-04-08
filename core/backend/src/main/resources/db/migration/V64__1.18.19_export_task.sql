DROP TABLE IF EXISTS `export_task`;
CREATE TABLE `export_task` (
           `id`            varchar(255)   NOT NULL,
           `user_id`     bigint(20)       NOT NULL ,
           `file_name` varchar(2048) DEFAULT NULL,
           `file_size` DOUBLE DEFAULT NULL,
           `file_size_unit` varchar(255)  DEFAULT NULL,
           `export_from` varchar(255) DEFAULT NULL,
           `export_status` varchar(255) DEFAULT NULL,
           `export_from_type` varchar(255) DEFAULT NULL,
           `export_time` bigint(20) DEFAULT NULL,
           `export_pogress` varchar(255)  DEFAULT NULL,
           `export_machine_name` varchar(512)  DEFAULT NULL,
           `params`  longtext NOT NULL COMMENT '过滤参数',
           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;