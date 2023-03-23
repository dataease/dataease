-- ----------------------------
-- Table structure for datasource
-- ----------------------------
DROP TABLE IF EXISTS `core_datasource`;
CREATE TABLE `core_datasource`
(
    `id`            varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
    `name`          varchar(50) NOT NULL COMMENT '数据源名称',
    `desc`          varchar(50)          DEFAULT NULL COMMENT '描述',
    `type`          varchar(50) NOT NULL COMMENT '类型',
    `configuration` longtext    NOT NULL COMMENT '详细信息',
    `create_time`   bigint      NOT NULL COMMENT 'Create timestamp',
    `update_time`   bigint      NOT NULL COMMENT 'Update timestamp',
    `create_by`     varchar(50)          DEFAULT NULL COMMENT '创建人ID',
    `status`        longtext COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


--
-- Table structure for table `core_menu`
--

DROP TABLE IF EXISTS `core_menu`;
CREATE TABLE `core_menu`
(
    `id`        bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pid`       bigint unsigned NOT NULL COMMENT '父ID',
    `type`      int         DEFAULT NULL COMMENT '类型',
    `name`      varchar(45) DEFAULT NULL COMMENT '名称',
    `component` varchar(45) DEFAULT NULL COMMENT '组件',
    `menu_sort` int         DEFAULT NULL COMMENT '排序',
    `icon`      varchar(45) DEFAULT NULL COMMENT '图标',
    `path`      varchar(45) DEFAULT NULL COMMENT '路径',
    `hidden`    tinyint unsigned NOT NULL DEFAULT '0' COMMENT '隐藏',
    `in_layout` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否内部',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3;

--
-- Dumping data for table `core_menu`
--

LOCK
TABLES `core_menu` WRITE;
INSERT INTO `core_menu`
VALUES (1, 0, 2, 'home', 'home', 1, NULL, '/home', 0, 1),
       (2, 0, 2, 'workbranch', 'workbranch', 2, NULL, '/workbranch', 0, 1),
       (3, 0, 1, 'visualized', NULL, 3, NULL, '/visualized', 0, 1),
       (4, 0, 2, 'template', 'template', 4, NULL, '/template', 0, 1),
       (5, 0, 2, 'application', 'application', 5, NULL, '/application', 0, 1),
       (6, 0, 1, 'system', NULL, 6, NULL, '/system', 0, 1),
       (7, 3, 1, 'view', NULL, 1, NULL, '/view', 0, 1),
       (8, 3, 1, 'data', NULL, 2, NULL, '/data', 0, 1),
       (9, 7, 2, 'panel', 'visualized/view/panel', 1, NULL, '/panel', 0, 1),
       (10, 7, 2, 'screen', 'visualized/view/screen', 2, NULL, '/screen', 0, 1),
       (11, 8, 2, 'dataset', 'visualized/data/dataset', 1, NULL, '/dataset', 0, 1),
       (12, 8, 2, 'datasource', 'visualized/data/datasource', 2, NULL, '/datasource', 0, 1),
       (13, 6, 2, 'user', 'system/user', 1, 'peoples', '/user', 0, 1),
       (14, 6, 2, 'org', 'system/org', 2, 'org', '/org', 0, 1),
       (15, 6, 2, 'auth', 'system/auth', 3, 'auth', '/auth', 0, 1);
UNLOCK
TABLES;