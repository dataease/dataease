DROP TABLE IF EXISTS `visualization_watermark`;
CREATE TABLE `visualization_watermark`
(
    `id`              varchar(50) NOT NULL COMMENT '主键',
    `version`         varchar(255) DEFAULT NULL COMMENT '版本号',
    `setting_content` longtext COMMENT '设置内容',
    `create_by`       varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time`     bigint(13)   DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT ='仪表板水印设置表';

INSERT INTO `visualization_watermark` (`id`, `version`, `setting_content`, `create_by`, `create_time`)
VALUES ('system_default', '1.0',
        '{\"enable\":false,\"enablePanelCustom\":true,\"type\":\"custom\",\"content\":\"水印\",\"watermark_color\":\"#DD1010\",\"watermark_x_space\":12,\"watermark_y_space\":36,\"watermark_fontsize\":15}',
        'admin', NULL);


INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
VALUES (9, 'basic.frontTimeOut', '60', 'text', 1);

ALTER TABLE `visualization_template`
    ADD COLUMN `use_count` int NULL DEFAULT 0 COMMENT '使用次数';

update visualization_template set use_count = 0;

