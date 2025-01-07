UPDATE `visualization_background`
SET `name` = 'Board10'
WHERE `id` = 'dark_1';
UPDATE `visualization_subject`
SET `name` = 'chart.light_theme'
WHERE `id` = '10001';
UPDATE `visualization_subject`
SET `name` = 'chart.dark_theme'
WHERE `id` = '10002';
CREATE INDEX idx_dataset_table_task_log_A ON core_datasource_task_log (ds_id, table_name, start_time);
ALTER TABLE core_chart_view
    ADD COLUMN sort_priority longtext null comment '字段排序优先级';
DELETE
FROM area
where id = '156320571';

ALTER TABLE `core_api_traffic`
    COMMENT = 'API并发限流阈值设置表';

ALTER TABLE `core_copilot_config`
    MODIFY COLUMN `copilot_url` varchar(255) NULL DEFAULT NULL COMMENT 'copilot服务端地址',
    MODIFY COLUMN `username` varchar(255) NULL DEFAULT NULL COMMENT '用户名',
    MODIFY COLUMN `pwd` varchar(255) NULL DEFAULT NULL COMMENT '密码',
    COMMENT = 'copilot配置信息表';

ALTER TABLE `core_copilot_msg`
    COMMENT = 'copilot问答信息表';

ALTER TABLE `core_copilot_token`
    MODIFY COLUMN `token` longtext NULL COMMENT 'token值',
    MODIFY COLUMN `update_time` bigint(0) NULL DEFAULT NULL COMMENT '更新时间',
    COMMENT = 'copilot token记录表';

ALTER TABLE `core_font`
    MODIFY COLUMN `size` double NULL DEFAULT NULL COMMENT '文件大小',
    MODIFY COLUMN `size_type` varchar(255) NULL DEFAULT NULL COMMENT '存储单位',
    COMMENT = '字体表';

ALTER TABLE `de_template_version`
    MODIFY COLUMN `installed_rank` int(0) NOT NULL COMMENT '主键',
    MODIFY COLUMN `version` varchar(50) NULL DEFAULT NULL COMMENT '版本',
    MODIFY COLUMN `description` varchar(200) NULL DEFAULT NULL COMMENT '描述',
    MODIFY COLUMN `type` varchar(20) NULL DEFAULT NULL COMMENT '类型',
    MODIFY COLUMN `script` varchar(1000) NOT NULL COMMENT '脚本',
    MODIFY COLUMN `checksum` int(0) NULL DEFAULT NULL COMMENT 'CheckSum校验码',
    MODIFY COLUMN `installed_by` varchar(100) NULL DEFAULT NULL COMMENT '安装人',
    MODIFY COLUMN `installed_on` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '安装时间',
    MODIFY COLUMN `execution_time` int(0) NULL DEFAULT NULL COMMENT '执行时间',
    MODIFY COLUMN `success` tinyint(1) NOT NULL COMMENT '执行状态',
    COMMENT = 'dataease模板配置版本记录表';

ALTER TABLE `demo_tea_material`
    COMMENT = '连锁茶饮销售看板demo数据';

ALTER TABLE `demo_tea_order`
    COMMENT = '连锁茶饮销售看板demo数据';


ALTER TABLE `visualization_report_filter`
    COMMENT = '定时报告过自定义过滤组件信息';

ALTER TABLE `visualization_subject`
    MODIFY COLUMN `id` varchar(50) NOT NULL COMMENT '主键';

ALTER TABLE `xpack_platform_token`
    MODIFY COLUMN `id` int(0) NOT NULL COMMENT '主键',
    MODIFY COLUMN `token` varchar(255) NOT NULL COMMENT '认证token',
    MODIFY COLUMN `create_time` bigint(0) NOT NULL COMMENT '创建时间',
    MODIFY COLUMN `exp_time` bigint(0) NOT NULL COMMENT '过期时间',
    COMMENT = '认证token信息表';

ALTER TABLE `xpack_setting_authentication`
    COMMENT = '认证设置';

ALTER TABLE `xpack_share`
    COMMENT = '公共链接';

ALTER TABLE `xpack_threshold_info`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键',
    COMMENT = '告警信息表';

ALTER TABLE `xpack_threshold_instance`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键',
    COMMENT = '告警实例表';

ALTER TABLE `core_area_custom`
    MODIFY COLUMN `id` varchar(255) NOT NULL COMMENT '主键',
    MODIFY COLUMN `name` varchar(255) NOT NULL COMMENT '自定义区域名称',
    MODIFY COLUMN `pid` varchar(255) NOT NULL COMMENT '父级ID';

ALTER TABLE `core_export_task`
    MODIFY COLUMN `id` varchar(255) NOT NULL COMMENT '主键',
    MODIFY COLUMN `user_id` bigint(0) NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `file_name` varchar(2048) NULL DEFAULT NULL COMMENT '文件名称',
    MODIFY COLUMN `file_size` double NULL DEFAULT NULL COMMENT '文件大小',
    MODIFY COLUMN `file_size_unit` varchar(255) NULL DEFAULT NULL COMMENT '单位',
    MODIFY COLUMN `export_from` varchar(255) NULL DEFAULT NULL COMMENT '导出来源ID',
    MODIFY COLUMN `export_status` varchar(255) NULL DEFAULT NULL COMMENT '导出状态',
    MODIFY COLUMN `export_from_type` varchar(255) NULL DEFAULT NULL COMMENT '导出来源类型',
    MODIFY COLUMN `export_time` bigint(0) NULL DEFAULT NULL COMMENT '导出时间',
    MODIFY COLUMN `export_progress` varchar(255) NULL DEFAULT NULL COMMENT '导出进度',
    MODIFY COLUMN `export_machine_name` varchar(512) NULL DEFAULT NULL COMMENT '导出机器名称';

ALTER TABLE `core_ds_finish_page`
    COMMENT = '是否显示完成页面记录表';

DROP TABLE IF EXISTS `xpack_webhook`;
CREATE TABLE `xpack_webhook`
(
    `id`           bigint       NOT NULL COMMENT 'ID',
    `name`         varchar(255) NOT NULL COMMENT '名称',
    `url`          varchar(255) NOT NULL COMMENT 'url',
    `content_type` varchar(255) NOT NULL COMMENT 'content_type',
    `secret`       varchar(255)          DEFAULT NULL COMMENT '密钥',
    `ssl`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '开启ssl',
    `oid`          bigint       NOT NULL COMMENT '组织ID',
    `create_time`  bigint       NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
);

delete  from visualization_background;
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_1', '1', 'default', '', NULL, NULL, NULL, 'img/board', 'board/board_1.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_2', '2', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_2.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_3', '3', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_3.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_4', '4', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_4.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_5', '5', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_5.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_6', '6', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_6.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_7', '7', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_7.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_8', '8', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_8.svg');
INSERT INTO `visualization_background` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('board_9', '9', 'default', NULL, NULL, NULL, NULL, 'img/board', 'board/board_9.svg');

DELETE FROM area WHERE id='156500200';
UPDATE area SET pid='156500100' WHERE pid='156500200';
UPDATE area SET name='万州区' WHERE id='156500101';