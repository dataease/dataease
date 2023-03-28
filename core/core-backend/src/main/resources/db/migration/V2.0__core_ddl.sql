-- ----------------------------
-- Table structure for datasource
-- ----------------------------
DROP TABLE IF EXISTS `core_datasource`;
CREATE TABLE `core_datasource`
(
    `id`            varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
    `desc`          varchar(50)          DEFAULT NULL COMMENT '描述',
    `type`          varchar(50) NOT NULL COMMENT '类型',
    `configuration` longtext    NOT NULL COMMENT '详细信息',
    `create_time`   bigint      NOT NULL COMMENT '创健时间',
    `update_time`   bigint      NOT NULL COMMENT '更新时间',
    `create_by`     varchar(50)          DEFAULT NULL COMMENT '创建人ID',
    `status`        longtext COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `core_driver`;
CREATE TABLE `core_driver` (
     `id`           varchar(50) NOT NULL COMMENT '主键',
     `name`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
     `create_time`  bigint(13) NOT NULL COMMENT '创健时间',
     `type`         varchar(255) DEFAULT NULL COMMENT '数据源类型',
     `driver_class` varchar(255) DEFAULT NULL COMMENT '驱动类',
     `desc`         varchar(255) DEFAULT NULL COMMENT '描述',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动';

DROP TABLE IF EXISTS `core_driver_jar`;
CREATE TABLE `core_driver_jar` (
     `id`           varchar(50) NOT NULL COMMENT '主键',
     `de_driver_id` varchar(50) NOT NULL COMMENT '驱动主键',
     `file_name`    varchar(255) DEFAULT NULL COMMENT '名称',
     `version`      varchar(255) DEFAULT NULL COMMENT '版本',
     `driver_class` longtext COMMENT '驱动类',
     `trans_name`   varchar(255) DEFAULT NULL,
     `is_trans_name` tinyint(1) DEFAULT NULL,
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动详情';


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
    `hidden`    tinyint(1)  NOT NULL DEFAULT '0' COMMENT '隐藏',
    `in_layout` tinyint(1)  NOT NULL DEFAULT '1' COMMENT '是否内部',
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

DROP TABLE IF EXISTS `core_dataset_group`;
CREATE TABLE `core_dataset_group`
(
    `id`               varchar(50) NOT NULL COMMENT 'ID',
    `name`             varchar(128)  DEFAULT NULL COMMENT '名称',
    `pid`              varchar(50)   DEFAULT NULL COMMENT '父级ID',
    `level`            int(10) DEFAULT '0' COMMENT '当前分组处于第几级',
    `node_type`        varchar(50) NOT NULL COMMENT 'node类型：folder or dataset',
    `mode`             int           DEFAULT '0' COMMENT '连接模式：0-直连，1-同步(包括excel、api等数据存在de中的表)',
    `info`             longtext COMMENT '关联关系树',
    `create_by`        varchar(50)   DEFAULT NULL COMMENT '创建人ID',
    `create_time`      bigint        DEFAULT NULL COMMENT '创建时间',
    `qrtz_instance`    varchar(1024) DEFAULT NULL,
    `sync_status`      varchar(45)   DEFAULT NULL COMMENT '同步状态',
    `last_update_time` bigint        DEFAULT '0' COMMENT '最后同步时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_dataset_table`;
CREATE TABLE `core_dataset_table`
(
    `id`                   varchar(50) NOT NULL COMMENT 'ID',
    `name`                 varchar(128) DEFAULT NULL COMMENT '名称',
    `table_name`           varchar(128) DEFAULT NULL COMMENT '物理表名',
    `datasource_id`        varchar(50)  DEFAULT NULL COMMENT '数据源ID',
    `dataset_group_id`     varchar(50) NOT NULL COMMENT '数据集ID',
    `type`                 varchar(50)  DEFAULT NULL COMMENT 'db,sql,union,excel,api',
    `info`                 longtext COMMENT '表原始信息,表名,sql等',
    `sql_variable_details` longtext COMMENT 'SQL参数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_dataset_table_field`;
CREATE TABLE `core_dataset_table_field`
(
    `id`               varchar(50)  NOT NULL COMMENT 'ID',
    `datasource_id`    varchar(50)  DEFAULT NULL COMMENT '数据源ID',
    `dataset_table_id` varchar(50)  NOT NULL COMMENT '数据表ID',
    `dataset_group_id` varchar(50)  NOT NULL COMMENT '数据集ID',
    `origin_name`      longtext     NOT NULL COMMENT '原始字段名',
    `name`             longtext     DEFAULT NULL COMMENT '字段名用于展示',
    `description`      longtext     DEFAULT NULL COMMENT '描述',
    `dataease_name`    varchar(255) NOT NULL COMMENT 'de字段名用作唯一标识',
    `group_type`       varchar(50)  DEFAULT NULL COMMENT '维度/指标标识 d:维度，q:指标',
    `type`             varchar(255) NOT NULL COMMENT '原始字段类型',
    `size`             int          DEFAULT NULL,
    `de_type`          int          NOT NULL COMMENT 'dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制',
    `de_extract_type`  int          NOT NULL COMMENT 'de记录的原始类型',
    `ext_field`        int          DEFAULT NULL COMMENT '是否扩展字段 0原始 1复制 2计算字段...',
    `checked`          tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中',
    `column_index`     int          DEFAULT NULL COMMENT '列位置',
    `last_sync_time`   bigint       DEFAULT NULL COMMENT '同步时间',
    `accuracy`         int          DEFAULT '0' COMMENT '精度',
    `date_format`      varchar(255) DEFAULT NULL,
    `date_format_type` varchar(255) DEFAULT NULL COMMENT '时间格式类型',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

