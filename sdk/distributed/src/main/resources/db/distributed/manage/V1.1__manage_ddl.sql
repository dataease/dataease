-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`
(
    `id`          bigint                                                     NOT NULL COMMENT '租户ID',
    `domain`      varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL COMMENT '二级域名',
    `account`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL COMMENT '管理员账号',
    `pwd`         varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL COMMENT '管理员密码',
    `company`     varchar(255) DEFAULT NULL COMMENT '租户公司',
    `create_time` bigint                                                     NOT NULL COMMENT '创建时间',
    `update_time` bigint       DEFAULT NULL COMMENT '修改时间',
    `enable`      tinyint(1) NOT NULL COMMENT '是否可用',
    `deleted`     tinyint(1) NOT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for tenant_db
-- ----------------------------
DROP TABLE IF EXISTS `tenant_db`;
CREATE TABLE `tenant_db`
(
    `id`        bigint       NOT NULL COMMENT '数据源ID',
    `tenant_id` bigint       NOT NULL COMMENT '租户ID',
    `url`       varchar(255) NOT NULL COMMENT 'jdbcurl',
    `username`  varchar(255) NOT NULL COMMENT 'db用户',
    `pwd`       varchar(255) NOT NULL COMMENT 'db密码',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;