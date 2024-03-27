-- ----------------------------
-- Table structure for panel_link_ticket
-- ----------------------------
DROP TABLE IF EXISTS `panel_link_ticket`;
CREATE TABLE `panel_link_ticket`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `uuid`        varchar(255) NOT NULL,
    `ticket`      varchar(255) NOT NULL,
    `exp`         bigint       DEFAULT NULL,
    `args`        varchar(255) DEFAULT NULL,
    `access_time` bigint       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1;

ALTER TABLE `panel_link_mapping`
    ADD COLUMN `require_ticket` tinyint(1) NOT NULL DEFAULT 0 AFTER `uuid`;

INSERT INTO `system_parameter` (`param_key`, `param_value`, `type`, `sort`) VALUES ('ai.baseUrl', 'https://maxkb.fit2cloud.com/ui/chat/5baa787163381fa2', 'text', 100);


DROP TABLE IF EXISTS `export_task`;
CREATE TABLE `export_task` (
   `id`            varchar(255)   NOT NULL,
   `user_id`     bigint(20)       NOT NULL ,
   `file_name` varchar(2048) DEFAULT NULL,
   `file_size` DOUBLE DEFAULT NULL,
   `export_from` varchar(255) DEFAULT NULL,
   `export_status` varchar(255) DEFAULT NULL,
   `export_from_type` varchar(255) DEFAULT NULL,
   `export_time` bigint(20) DEFAULT NULL,
   `export_pogress` varchar(255)  DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

