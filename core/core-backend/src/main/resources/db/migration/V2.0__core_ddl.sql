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