-- ----------------------------
-- Table structure for datasource
-- ----------------------------
DROP TABLE IF EXISTS `datasource`;
CREATE TABLE `datasource`
(
    `id`            varchar(50) COLLATE utf8mb4_general_ci                NOT NULL DEFAULT '' COMMENT 'ID',
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '数据源名称',
    `desc`          varchar(50) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '描述',
    `type`          varchar(50) COLLATE utf8mb4_general_ci                NOT NULL COMMENT '类型',
    `configuration` longtext COLLATE utf8mb4_general_ci                   NOT NULL COMMENT '详细信息',
    `create_time`   bigint                                                NOT NULL COMMENT 'Create timestamp',
    `update_time`   bigint                                                NOT NULL COMMENT 'Update timestamp',
    `create_by`     varchar(50) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '创建人ID',
    `status`        longtext COLLATE utf8mb4_general_ci COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;