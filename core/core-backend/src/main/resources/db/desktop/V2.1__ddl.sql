DROP TABLE IF EXISTS `visualization_template`;
CREATE TABLE `visualization_template` (
                                          `id` varchar(50) NOT NULL COMMENT '主键',
                                          `name` varchar(255) DEFAULT NULL COMMENT '名称',
                                          `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
                                          `level` int(10) DEFAULT NULL COMMENT '层级',
                                          `dv_type` varchar(255) DEFAULT NULL COMMENT '模板种类  dataV or dashboard 目录或者文件夹',
                                          `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
                                          `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
                                          `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
                                          `snapshot` longtext COMMENT '缩略图',
                                          `template_type` varchar(255) DEFAULT NULL COMMENT '模板类型 system 系统内置 self 用户自建 ',
                                          `template_style` longtext COMMENT 'template 样式',
                                          `template_data` longtext COMMENT 'template 数据',
                                          `dynamic_data` longtext COMMENT '预存数据',
                                          PRIMARY KEY (`id`)
);

BEGIN;
INSERT INTO `core_menu`
VALUES (19, 0, 2, 'template-market', 'template-market', 4, NULL, '/template-market', 1, 1, 0);
INSERT INTO `core_menu`
VALUES (20, 15, 2, 'template-setting', 'system/template-setting', 4, 'icon_template', '/template-setting', 0, 1, 1);
COMMIT;

DROP TABLE IF EXISTS `visualization_template_extend_data`;
CREATE TABLE `visualization_template_extend_data` (
                                                      `id` bigint NOT NULL,
                                                      `dv_id` bigint DEFAULT NULL,
                                                      `view_id` bigint DEFAULT NULL,
                                                      `view_details` longtext,
                                                      `copy_from` varchar(255) DEFAULT NULL,
                                                      `copy_id` varchar(255) DEFAULT NULL,
                                                      PRIMARY KEY (`id`)
);

ALTER TABLE `core_opt_recent`
    MODIFY COLUMN `resource_id` bigint NULL COMMENT '资源ID' AFTER `id`,
    ADD COLUMN `resource_name` varchar(255) NULL COMMENT '资源名称' AFTER `resource_id`;