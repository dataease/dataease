DROP TABLE IF EXISTS `visualization_template`;
CREATE TABLE `visualization_template`
(
    `id`             varchar(50) NOT NULL COMMENT '主键',
    `name`           varchar(255) DEFAULT NULL COMMENT '名称',
    `pid`            varchar(255) DEFAULT NULL COMMENT '父级id',
    `level`          int          DEFAULT NULL COMMENT '层级',
    `dv_type`        varchar(255) DEFAULT NULL COMMENT '模板种类  dataV or dashboard 目录或者文件夹',
    `node_type`      varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
    `create_by`      varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time`    bigint       DEFAULT NULL COMMENT '创建时间',
    `snapshot`       longtext COMMENT '缩略图',
    `template_type`  varchar(255) DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建 ',
    `template_style` longtext COMMENT 'template 样式',
    `template_data`  longtext COMMENT 'template 数据',
    `dynamic_data`   longtext COMMENT '预存数据',
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for visualization_template_category
-- ----------------------------
DROP TABLE IF EXISTS `visualization_template_category`;
CREATE TABLE `visualization_template_category`
(
    `id`            varchar(50) NOT NULL COMMENT '主键',
    `name`          varchar(255) DEFAULT NULL COMMENT '名称',
    `pid`           varchar(255) DEFAULT NULL COMMENT '父级id',
    `level`         int          DEFAULT NULL COMMENT '层级',
    `dv_type`       varchar(255) DEFAULT NULL COMMENT '模板种类  dataV or dashboard 目录或者文件夹',
    `node_type`     varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
    `create_by`     varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time`   bigint       DEFAULT NULL COMMENT '创建时间',
    `snapshot`      longtext COMMENT '缩略图',
    `template_type` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for visualization_template_category_map
-- ----------------------------
DROP TABLE IF EXISTS `visualization_template_category_map`;
CREATE TABLE `visualization_template_category_map`
(
    `id`          varchar(50) NOT NULL COMMENT '主键',
    `category_id` varchar(255) DEFAULT NULL COMMENT '名称',
    `template_id` varchar(255) DEFAULT NULL COMMENT '父级id',
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for visualization_template_extend_data
-- ----------------------------
DROP TABLE IF EXISTS `visualization_template_extend_data`;
CREATE TABLE `visualization_template_extend_data`
(
    `id`           bigint NOT NULL,
    `dv_id`        bigint       DEFAULT NULL,
    `view_id`      bigint       DEFAULT NULL,
    `view_details` longtext,
    `copy_from`    varchar(255) DEFAULT NULL,
    `copy_id`      varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO `core_menu`
VALUES (19, 0, 2, 'template-market', 'template-market', 4, NULL, '/template-market', 1, 1, 0);
INSERT INTO `core_menu`
VALUES (30, 0, 1, 'toolbox', null, 7, 'icon_template', '/toolbox', 1, 1, 0);
INSERT INTO `core_menu`
VALUES (31, 30, 2, 'template-setting', 'toolbox/template-setting', 1, 'icon_template', '/template-setting', 0, 1, 1);

ALTER TABLE core_opt_recent
    ADD `resource_name` varchar(255) NULL COMMENT '资源名称';

DROP TABLE IF EXISTS `core_area_custom`;
CREATE TABLE `core_area_custom`
(
    `id`   varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    `pid`  varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO `core_sys_setting`
VALUES (1, 'basic.dsIntervalTime', '6', 'text', 2);
INSERT INTO `core_sys_setting`
VALUES (2, 'basic.dsExecuteTime', 'minute', 'text', 3);
INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
VALUES (7, 'template.url', 'https://templates.dataease.cn', 'text', 0);
INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
VALUES (8, 'template.accessKey', 'dataease', 'text', 1);
