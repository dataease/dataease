-- MySQL dump 10.13  Distrib 8.0.29, for macos12 (x86_64)
-- Server version	8.0.29

--
-- Table structure for table `per_user`
--

DROP TABLE IF EXISTS `per_user`;
CREATE TABLE `per_user`
(
    `id`           bigint       NOT NULL COMMENT '用户ID',
    `account`      varchar(50)  NOT NULL COMMENT '账号',
    `pwd`          varchar(255) NOT NULL,
    `name`         varchar(100) NOT NULL COMMENT '名称',
    `email`        varchar(100) NOT NULL COMMENT '邮箱',
    `phone_prefix` varchar(15) DEFAULT NULL COMMENT '手机前缀',
    `phone`        varchar(20) DEFAULT NULL COMMENT '手机号',
    `enable`       tinyint(1) NOT NULL COMMENT '启用',
    `from`         int         DEFAULT NULL COMMENT '来源',
    `creator_id`   bigint      DEFAULT NULL COMMENT '创建人ID',
    `create_time`  bigint      DEFAULT NULL COMMENT '创建时间',
    `language`     varchar(15) DEFAULT NULL COMMENT '语言',
    `default_oid`  bigint      DEFAULT NULL COMMENT '默认组织ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `per_user`
--

LOCK
TABLES `per_user` WRITE;
INSERT INTO `per_user`
VALUES (1, 'admin', '7bc4b3f2d99c56c2d906ba588f589b5f', '系统管理员', 'admin@fit2cloud.com', '+86', NULL, 1, 0, 1, 1677671694000, 'zh-CN', 1);
UNLOCK
TABLES;

--
-- Table structure for table `per_role`
--

DROP TABLE IF EXISTS `per_role`;
CREATE TABLE `per_role`
(
    `id`       bigint      NOT NULL COMMENT '角色ID',
    `name`     varchar(20) NOT NULL COMMENT '名称',
    `desc`     varchar(255)         DEFAULT NULL COMMENT '描述',
    `level`    int         NOT NULL DEFAULT '2' COMMENT '级别1系统级2组织级',
    `readonly` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否只读',
    `org_id`   bigint               DEFAULT NULL COMMENT '所属组织',
    `pid`      bigint               DEFAULT NULL COMMENT '继承角色ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `per_role`
--

LOCK
TABLES `per_role` WRITE;
INSERT INTO `per_role`
VALUES (1, '系统管理员', '此间我最大', 1, 0, NULL, 0),
       (2, '默认组织管理员', '掌管默认组织', 2, 0, 1, 0),
       (3, '全局查看', '可看全局', 2, 0, NULL, 0);
UNLOCK
TABLES;

--
-- Table structure for table `per_user_role`
--

DROP TABLE IF EXISTS `per_user_role`;
CREATE TABLE `per_user_role`
(
    `id`  bigint NOT NULL COMMENT '关联ID',
    `uid` bigint NOT NULL COMMENT '用户ID',
    `rid` bigint NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `per_user_role`
--

LOCK
TABLES `per_user_role` WRITE;
INSERT INTO `per_user_role`
VALUES (1, 1, 1);
UNLOCK
TABLES;

--
-- Table structure for table `per_org`
--

DROP TABLE IF EXISTS `per_org`;
CREATE TABLE `per_org`
(
    `id`   bigint       NOT NULL COMMENT '组织ID',
    `name` varchar(100) NOT NULL COMMENT '名称',
    `pid`  bigint       NOT NULL COMMENT '上级组织',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `per_org`
--

LOCK
TABLES `per_org` WRITE;
INSERT INTO `per_org`
VALUES (1, '默认组织', 0);
UNLOCK
TABLES;

--
-- Table structure for table `per_user_org`
--

-- DROP TABLE IF EXISTS `per_user_org`;
-- CREATE TABLE `per_user_org`
-- (
--     `id`  bigint NOT NULL COMMENT '关联ID',
--     `uid` bigint NOT NULL COMMENT '用户ID',
--     `oid` bigint NOT NULL COMMENT '组织ID',
--     PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `per_user_org`
--

-- LOCK
-- TABLES `per_user_org` WRITE;
-- INSERT INTO `per_user_org`
-- VALUES (1, 1, 1);
-- UNLOCK
-- TABLES;


--
-- Table structure for table `per_resource`
--

DROP TABLE IF EXISTS `per_busi_resource`;
CREATE TABLE `per_busi_resource`
(
    `id`       bigint      NOT NULL COMMENT '资源ID',
    `name`     varchar(30) NOT NULL COMMENT '名称',
    `rt_id`    bigint      NOT NULL COMMENT '类型ID',
    `org_id`   bigint       DEFAULT NULL COMMENT '所属组织ID',
    `pid`      bigint      NOT NULL COMMENT '上级资源ID',
    `root_way` varchar(255) DEFAULT NULL COMMENT '寻根路径',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `per_resource`
--

DROP TABLE IF EXISTS `per_menu_resource`;
CREATE TABLE `per_menu_resource`
(
    `id`       bigint      NOT NULL COMMENT '资源ID',
    `name`     varchar(30) NOT NULL COMMENT '名称',
    `pid`      bigint      NOT NULL COMMENT '上级资源ID',
    `root_way` varchar(255) DEFAULT NULL COMMENT '寻根路径',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `per_auth_menu`
--
DROP TABLE IF EXISTS `per_auth_menu`;
CREATE TABLE `per_auth_menu`
(
    `id`          bigint NOT NULL COMMENT '授权ID',
    `resource_id` bigint NOT NULL COMMENT '资源ID',
    `read`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '可读',
    `manage`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '可编辑',
    `export`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '导出',
    `auth`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '授权',
    `ext_opt`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '预留操作',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `per_auth_busi_user`
--

DROP TABLE IF EXISTS `per_auth_busi_user`;
CREATE TABLE `per_auth_busi_user`
(
    `id`            bigint NOT NULL COMMENT '授权ID',
    `u_id`          bigint NOT NULL COMMENT '目标ID',
    `resource_id`   bigint NOT NULL COMMENT '资源ID',
    `resource_type` int    NOT NULL COMMENT '资源类型',
    `read`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '可读',
    `manage`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '可编辑',
    `export`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '导出',
    `auth`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '授权',
    `ext_opt`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '预留操作',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `per_auth_busi_role`
--

DROP TABLE IF EXISTS `per_auth_busi_role`;
CREATE TABLE `per_auth_busi_role`
(
    `id`            bigint NOT NULL COMMENT '授权ID',
    `r_id`          bigint NOT NULL COMMENT '目标ID',
    `resource_id`   bigint NOT NULL COMMENT '资源ID',
    `resource_type` int    NOT NULL COMMENT '资源类型',
    `read`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '可读',
    `manage`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '可编辑',
    `export`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '导出',
    `auth`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '授权',
    `ext_opt`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '预留操作',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;












