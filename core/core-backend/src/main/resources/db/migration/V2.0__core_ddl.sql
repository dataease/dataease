-- ----------------------------
-- Table structure for datasource
-- ----------------------------
DROP TABLE IF EXISTS `core_datasource`;
CREATE TABLE `core_datasource`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
    `description`   varchar(50) DEFAULT NULL COMMENT '描述',
    `type`          varchar(50)                                           NOT NULL COMMENT '类型',
    `configuration` longtext                                              NOT NULL COMMENT '详细信息',
    `create_time`   bigint                                                NOT NULL COMMENT '创建时间',
    `update_time`   bigint                                                NOT NULL COMMENT '更新时间',
    `create_by`     varchar(50) DEFAULT NULL COMMENT '创建人ID',
    `status`        longtext COMMENT '状态',
    `qrtz_instance` longtext COMMENT '状态',
    `task_status`   varchar(50) DEFAULT NULL COMMENT '任务状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `core_driver`;
CREATE TABLE `core_driver`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
    `create_time`  bigint(13) NOT NULL COMMENT '创建时间',
    `type`         varchar(255) DEFAULT NULL COMMENT '数据源类型',
    `driver_class` varchar(255) DEFAULT NULL COMMENT '驱动类',
    `description`  varchar(255) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动';

DROP TABLE IF EXISTS `core_driver_jar`;
CREATE TABLE `core_driver_jar`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `de_driver_id`  varchar(50) NOT NULL COMMENT '驱动主键',
    `file_name`     varchar(255) DEFAULT NULL COMMENT '名称',
    `version`       varchar(255) DEFAULT NULL COMMENT '版本',
    `driver_class`  longtext COMMENT '驱动类',
    `trans_name`    varchar(255) DEFAULT NULL,
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
    `hidden`    tinyint(1) NOT NULL DEFAULT '0' COMMENT '隐藏',
    `in_layout` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否内部',
    `auth`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '参与授权',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `core_menu`
--

LOCK
TABLES `core_menu` WRITE;
INSERT INTO `core_menu`
VALUES (1, 0, 2, 'workbranch', 'workbranch', 1, NULL, '/workbranch', 0, 1, 1),
       (2, 0, 2, 'panel', 'visualized/view/panel', 2, NULL, '/panel', 0, 1, 1),
       (3, 0, 2, 'screen', 'visualized/view/screen', 3, NULL, '/screen', 0, 1, 1),
       (4, 0, 1, 'data', NULL, 4, NULL, '/data', 0, 1, 1),
       (5, 4, 2, 'dataset', 'visualized/data/dataset', 1, NULL, '/dataset', 0, 1, 1),
       (6, 4, 2, 'datasource', 'visualized/data/datasource', 2, NULL, '/datasource', 0, 1, 1),
       (7, 0, 1, 'system', NULL, 6, NULL, '/system', 1, 1, 1),
       (8, 7, 2, 'user', 'system/user', 1, 'peoples', '/user', 0, 1, 1),
       (9, 7, 2, 'org', 'system/org', 2, 'org', '/org', 0, 1, 1),
       (10, 7, 2, 'auth', 'system/auth', 3, 'auth', '/auth', 0, 1, 1),
       (11, 0, 2, 'dataset-form', 'visualized/data/dataset/form', 7, NULL, '/dataset-form', 1, 0, 0),
       (12, 0, 2, 'datasource-form', 'visualized/data/datasource/form', 7, NULL, '/ds-form', 1, 0, 0),
       (13, 0, 2, 'about', 'about', 7, NULL, '/about', 1, 1, 0);
UNLOCK
TABLES;

DROP TABLE IF EXISTS `core_dataset_group`;
CREATE TABLE `core_dataset_group`
(
    `id`               bigint      NOT NULL COMMENT 'ID',
    `name`             varchar(128)  DEFAULT NULL COMMENT '名称',
    `pid`              bigint        DEFAULT NULL COMMENT '父级ID',
    `level`            int(10) DEFAULT '0' COMMENT '当前分组处于第几级',
    `node_type`        varchar(50) NOT NULL COMMENT 'node类型：folder or dataset',
    `type`             varchar(50)   DEFAULT NULL COMMENT 'sql,union',
    `mode`             int           DEFAULT '0' COMMENT '连接模式：0-直连，1-同步(包括excel、api等数据存在de中的表)',
    `info`             longtext COMMENT '关联关系树',
    `create_by`        varchar(50)   DEFAULT NULL COMMENT '创建人ID',
    `create_time`      bigint        DEFAULT NULL COMMENT '创建时间',
    `qrtz_instance`    varchar(1024) DEFAULT NULL,
    `sync_status`      varchar(45)   DEFAULT NULL COMMENT '同步状态',
    `last_update_time` bigint        DEFAULT '0' COMMENT '最后同步时间',
    `union_sql`        longtext COMMENT '关联sql',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_dataset_table`;
CREATE TABLE `core_dataset_table`
(
    `id`                   bigint NOT NULL COMMENT 'ID',
    `name`                 varchar(128) DEFAULT NULL COMMENT '名称',
    `table_name`           varchar(128) DEFAULT NULL COMMENT '物理表名',
    `datasource_id`        bigint       DEFAULT NULL COMMENT '数据源ID',
    `dataset_group_id`     bigint NOT NULL COMMENT '数据集ID',
    `type`                 varchar(50)  DEFAULT NULL COMMENT 'db,sql,union,excel,api',
    `info`                 longtext COMMENT '表原始信息,表名,sql等',
    `sql_variable_details` longtext COMMENT 'SQL参数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_dataset_table_field`;
CREATE TABLE `core_dataset_table_field`
(
    `id`               bigint       NOT NULL COMMENT 'ID',
    `datasource_id`    bigint       DEFAULT NULL COMMENT '数据源ID',
    `dataset_table_id` bigint       DEFAULT NULL COMMENT '数据表ID',
    `dataset_group_id` bigint       DEFAULT NULL COMMENT '数据集ID',
    `origin_name`      longtext     NOT NULL COMMENT '原始字段名',
    `name`             longtext     DEFAULT NULL COMMENT '字段名用于展示',
    `description`      longtext     DEFAULT NULL COMMENT '描述',
    `dataease_name`    varchar(255) DEFAULT NULL COMMENT 'de字段名用作唯一标识',
    `field_short_name` varchar(255) DEFAULT NULL COMMENT 'de字段别名',
    `group_type`       varchar(50)  DEFAULT NULL COMMENT '维度/指标标识 d:维度，q:指标',
    `type`             varchar(255) NOT NULL COMMENT '原始字段类型',
    `size`             int          DEFAULT NULL,
    `de_type`          int          NOT NULL COMMENT 'dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制',
    `de_extract_type`  int          NOT NULL COMMENT 'de记录的原始类型',
    `ext_field`        int          DEFAULT NULL COMMENT '是否扩展字段 0原始 1复制 2计算字段...',
    `checked`          tinyint(1) DEFAULT NULL DEFAULT '1' COMMENT '是否选中',
    `column_index`     int          DEFAULT NULL COMMENT '列位置',
    `last_sync_time`   bigint       DEFAULT NULL COMMENT '同步时间',
    `accuracy`         int          DEFAULT '0' COMMENT '精度',
    `date_format`      varchar(255) DEFAULT NULL,
    `date_format_type` varchar(255) DEFAULT NULL COMMENT '时间格式类型',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

--
-- Table structure for table `core_rsa`
--
DROP TABLE IF EXISTS `core_rsa`;
CREATE TABLE `core_rsa`
(
    `id`          int    NOT NULL COMMENT '主键',
    `private_key` text   NOT NULL COMMENT '私钥',
    `public_key`  text   NOT NULL COMMENT '公钥',
    `create_time` bigint NOT NULL COMMENT '生成时间',
    `aes_key`     text   NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `data_visualization_info`;
CREATE TABLE `data_visualization_info`
(
    `id`                    varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
    `name`                  varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '名称',
    `pid`                   varchar(50) COLLATE utf8mb4_general_ci                        DEFAULT NULL COMMENT '父id',
    `org_id`                varchar(50) COLLATE utf8mb4_general_ci                        DEFAULT NULL COMMENT '所属组织id',
    `level`                 int                                                           DEFAULT NULL COMMENT '层级',
    `node_type`             varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
    `type`                  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '类型',
    `canvas_style_data`     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '样式数据',
    `component_data`        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '组件数据',
    `mobile_layout`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '移动端布局',
    `status`                int                                                           DEFAULT '1' COMMENT '状态 0-未发布 1-已发布',
    `self_watermark_status` int                                                           DEFAULT '0' COMMENT '是否单独打开水印 0-关闭 1-开启',
    `sort`                  int                                                           DEFAULT '0' COMMENT '排序',
    `create_time`           bigint                                                        DEFAULT NULL COMMENT '创建时间',
    `create_by`             varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '创建人',
    `update_time`           bigint                                                        DEFAULT NULL COMMENT '更新时间',
    `update_by`             varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '更新人',
    `remark`                varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '备注',
    `source`                varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '数据来源',
    `delete_flag`           tinyint(1) DEFAULT '0' COMMENT '删除标志',
    `delete_time`           bigint                                                        DEFAULT NULL COMMENT '删除时间',
    `delete_by`             varchar(255) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '删除人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


DROP TABLE IF EXISTS `core_datasource_task`;
CREATE TABLE `core_datasource_task`
(
    `id`                bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ds_id`             bigint       NOT NULL COMMENT '数据源ID',
    `name`              varchar(255) NOT NULL COMMENT '任务名称',
    `update_type`       varchar(50)  NOT NULL COMMENT '更新方式',
    `start_time`        bigint(13) DEFAULT NULL COMMENT '开始时间',
    `sync_rate`         varchar(50)  NOT NULL COMMENT '执行频率：0 一次性 1 cron',
    `cron`              varchar(255) DEFAULT NULL COMMENT 'cron表达式',
    `simple_cron_value` bigint(13) NOT NULL COMMENT '简单重复间隔',
    `simple_cron_type`  varchar(50)  NOT NULL COMMENT '简单重复类型：分、时、天',
    `end_limit`         varchar(50)  NOT NULL COMMENT '结束限制 0 无限制 1 设定结束时间',
    `end_time`          bigint(13) DEFAULT NULL COMMENT '结束时间',
    `create_time`       bigint(13) DEFAULT NULL COMMENT '创建时间',
    `last_exec_time`    bigint(13) DEFAULT NULL COMMENT '上次执行时间',
    `last_exec_status`  varchar(50)  DEFAULT NULL COMMENT '上次执行结果',
    `extra_data`        longtext,
    `status`            varchar(50)  DEFAULT NULL COMMENT '任务状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_datasource_task_log`;
CREATE TABLE `core_datasource_task_log`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ds_id`         bigint(13) NOT NULL COMMENT '数据源ID',
    `task_id`      bigint(13) DEFAULT NULL COMMENT '任务ID',
    `start_time`   bigint(13) DEFAULT NULL COMMENT '开始时间',
    `end_time`     bigint(13) DEFAULT NULL COMMENT '结束时间',
    `status`       varchar(50) NOT NULL COMMENT '执行状态',
    `info`         longtext COMMENT '错误信息',
    `create_time`  bigint(13) DEFAULT NULL COMMENT '创建时间',
    `trigger_type` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY            `idx_dataset_table_task_log_ds_id` (`ds_id`),
    KEY            `idx_dataset_table_task_log_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_de_engine`;
CREATE TABLE `core_de_engine`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          varchar(50) DEFAULT NULL COMMENT '名称',
    `description`   varchar(50) DEFAULT NULL COMMENT '描述',
    `type`          varchar(50) NOT NULL COMMENT '类型',
    `configuration` longtext    NOT NULL COMMENT '详细信息',
    `create_time`   bigint(13) DEFAULT NULL COMMENT 'Create timestamp',
    `update_time`   bigint(13) DEFAULT NULL COMMENT 'Update timestamp',
    `create_by`     varchar(50) DEFAULT NULL COMMENT '创建人ID',
    `status`        varchar(45) DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `core_chart_view`;
CREATE TABLE `core_chart_view`
(
    `id`                  bigint NOT NULL COMMENT 'ID',
    `title`               varchar(1024) DEFAULT NULL COMMENT '标题',
    `scene_id`            bigint NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
    `table_id`            bigint NOT NULL COMMENT '数据集表ID',
    `type`                varchar(50)   DEFAULT NULL COMMENT '图表类型',
    `render`              varchar(50)   DEFAULT NULL COMMENT '视图渲染方式',
    `result_count`        int           DEFAULT NULL COMMENT '展示结果',
    `result_mode`         varchar(50)   DEFAULT NULL COMMENT '展示模式',
    `x_axis`              longtext COMMENT '横轴field',
    `x_axis_ext`          longtext COMMENT 'table-row',
    `y_axis`              longtext COMMENT '纵轴field',
    `y_axis_ext`          longtext COMMENT '副轴',
    `ext_stack`           longtext COMMENT '堆叠项',
    `ext_bubble`          longtext COMMENT '气泡大小',
    `custom_attr`         longtext COMMENT '图形属性',
    `custom_style`        longtext COMMENT '组件样式',
    `custom_filter`       longtext COMMENT '结果过滤',
    `drill_fields`        longtext COMMENT '钻取字段',
    `senior`              longtext COMMENT '高级',
    `create_by`           varchar(50)   DEFAULT NULL COMMENT '创建人ID',
    `create_time`         bigint        DEFAULT NULL COMMENT '创建时间',
    `update_time`         bigint        DEFAULT NULL COMMENT '更新时间',
    `snapshot`            longtext COMMENT '缩略图 ',
    `style_priority`      varchar(255)  DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 视图',
    `chart_type`          varchar(255)  DEFAULT 'private' COMMENT '视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板',
    `is_plugin`           bit(1)        DEFAULT NULL COMMENT '是否插件',
    `data_from`           varchar(255)  DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据',
    `view_fields`         longtext COMMENT '视图字段集合',
    `refresh_view_enable` tinyint(1) DEFAULT 0 COMMENT '是否开启刷新',
    `refresh_unit`        varchar(255)  DEFAULT 'minute' COMMENT '刷新时间单位',
    `refresh_time`        int           DEFAULT 5 COMMENT '刷新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
