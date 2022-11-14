ALTER TABLE `sys_log`
    CHANGE COLUMN `user_id` `user_id` BIGINT NULL COMMENT '操作人',
    CHANGE COLUMN `login_name` `login_name` VARCHAR (255) NULL COMMENT '登录账号',
    CHANGE COLUMN `nick_name` `nick_name` VARCHAR (255) NULL COMMENT '姓名';

ALTER TABLE `panel_group`
    ADD COLUMN `watermark_open` tinyint(1) NULL DEFAULT 1 COMMENT '是否单独打开水印' AFTER `update_time`;

INSERT INTO `panel_watermark` (`id`, `version`, `setting_content`, `create_by`, `create_time`)
VALUES ('system_default', '1.0',
        '{\"enable\":false,\"enablePanelCustom\":true,\"type\":\"custom\",\"content\":\"${time}-${nickName}\",\"watermark_color\":\"#999999\",\"watermark_x_space\":20,\"watermark_y_space\":100,\"watermark_fontsize\":20}',
        'admin', NULL);

CREATE TABLE `panel_watermark`
(
    `id`      varchar(50) NOT NULL,
    `name`    varchar(255) DEFAULT NULL,
    `version` varchar(255) DEFAULT NULL COMMENT '版本',
    `type`    varchar(255) DEFAULT NULL COMMENT '类型',
    `content` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

SET
FOREIGN_KEY_CHECKS = 1;

UPDATE `sys_menu` SET `component` = 'dataset/Form' WHERE (`menu_id` = '800');


UPDATE `sys_menu` SET `component` = 'msg/All' WHERE (`component` = 'msg/all');
UPDATE `sys_menu` SET `component` = 'msg/Setting' WHERE (`component` = 'msg/setting');
