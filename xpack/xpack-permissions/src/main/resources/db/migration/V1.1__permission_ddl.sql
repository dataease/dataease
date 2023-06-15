--
-- Table structure for table `per_auth_busi_role`
--

DROP TABLE IF EXISTS `per_auth_busi_role`;
CREATE TABLE `per_auth_busi_role`
(
    `id`            bigint NOT NULL COMMENT '授权ID',
    `rid`           bigint NOT NULL COMMENT '目标ID',
    `resource_id`   bigint NOT NULL COMMENT '资源ID',
    `resource_type` int    NOT NULL COMMENT '资源类型',
    `weight`        int    NOT NULL DEFAULT '0' COMMENT '权重',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Table structure for table `per_auth_busi_user`
--

DROP TABLE IF EXISTS `per_auth_busi_user`;
CREATE TABLE `per_auth_busi_user`
(
    `id`            bigint NOT NULL COMMENT '授权ID',
    `uid`           bigint NOT NULL COMMENT '目标ID',
    `resource_id`   bigint NOT NULL COMMENT '资源ID',
    `resource_type` int    NOT NULL COMMENT '资源类型',
    `weight`        int    NOT NULL DEFAULT '0' COMMENT '权重',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Table structure for table `per_auth_menu`
--

DROP TABLE IF EXISTS `per_auth_menu`;

CREATE TABLE `per_auth_menu`
(
    `id`          bigint NOT NULL COMMENT '授权ID',
    `rid`         bigint NOT NULL COMMENT '角色ID',
    `resource_id` bigint NOT NULL COMMENT '资源ID',
    `weight`      int    NOT NULL DEFAULT '0' COMMENT '权重0无1查看2授权',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



--
-- Table structure for table `per_busi_resource`
--

DROP TABLE IF EXISTS `per_busi_resource`;

CREATE TABLE `per_busi_resource`
(
    `id`       bigint      NOT NULL COMMENT '资源ID',
    `name`     varchar(30) NOT NULL COMMENT '名称',
    `rt_id`    int         NOT NULL COMMENT '类型ID',
    `org_id`   bigint               DEFAULT NULL COMMENT '所属组织ID',
    `pid`      bigint      NOT NULL COMMENT '上级资源ID',
    `root_way` varchar(255)         DEFAULT NULL COMMENT '寻根路径',
    `leaf`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '叶子结点',
    `extra_flag`     int         NOT NULL DEFAULT '0' COMMENT '拓展标识',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



--
-- Table structure for table `per_menu_resource`
--

DROP TABLE IF EXISTS `per_menu_resource`;

CREATE TABLE `per_menu_resource`
(
    `id`       bigint      NOT NULL COMMENT '资源ID',
    `name`     varchar(30) NOT NULL COMMENT '名称',
    `pid`      bigint      NOT NULL COMMENT '上级资源ID',
    `root_way` varchar(255) DEFAULT NULL COMMENT '寻根路径',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `per_menu_resource`
--

LOCK
TABLES `per_menu_resource` WRITE;
INSERT INTO `per_menu_resource`
VALUES (1, 'workbranch', 0, NULL),
       (2, 'panel', 0, NULL),
       (3, 'screen', 0, NULL),
       (4, 'data', 0, NULL),
       (5, 'dataset', 4, '4'),
       (6, 'datasource', 4, '4'),
       (7, 'system', 0, NULL),
       (8, 'user', 7, '7'),
       (9, 'org', 7, '7'),
       (10, 'auth', 7, '7');
UNLOCK
TABLES;

--
-- Table structure for table `per_org`
--

DROP TABLE IF EXISTS `per_org`;

CREATE TABLE `per_org`
(
    `id`          bigint       NOT NULL COMMENT '组织ID',
    `name`        varchar(100) NOT NULL COMMENT '名称',
    `pid`         bigint       NOT NULL COMMENT '上级组织',
    `root_way`    varchar(255) DEFAULT NULL COMMENT '寻根路径',
    `create_time` bigint       DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `per_org`
--

LOCK
TABLES `per_org` WRITE;
INSERT INTO `per_org`
VALUES (1, '默认组织', 0, NULL, 1680839960000);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `per_role`
--

LOCK
TABLES `per_role` WRITE;
INSERT INTO `per_role`
VALUES (1, '系统管理员', '此间我最大', 1, 0, NULL, 0),
       (2, '组织管理员', '掌管默认组织', 2, 0, 1, 0),
       (3, '普通用户', '默认组织查看用户', 2, 1, 1, 0);
UNLOCK
TABLES;

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
    `origin`       int         DEFAULT NULL COMMENT '来源',
    `creator_id`   bigint      DEFAULT NULL COMMENT '创建人ID',
    `create_time`  bigint      DEFAULT NULL COMMENT '创建时间',
    `language`     varchar(15) DEFAULT NULL COMMENT '语言',
    `default_oid`  bigint      DEFAULT NULL COMMENT '默认组织ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `per_user`
--

LOCK
TABLES `per_user` WRITE;
INSERT INTO `per_user`
VALUES (1, 'admin', '83d923c9f1d8fcaa46cae0ed2aaa81b5', '系统管理员', 'dataease@fit2cloud.com', '+86', NULL, 1, 0, 1,
        1677671694000, 'zh-CN', 1);
UNLOCK
TABLES;

--
-- Table structure for table `per_user_role`
--

DROP TABLE IF EXISTS `per_user_role`;

CREATE TABLE `per_user_role`
(
    `id`          bigint NOT NULL COMMENT '关联ID',
    `uid`         bigint NOT NULL COMMENT '用户ID',
    `rid`         bigint NOT NULL COMMENT '角色ID',
    `oid`         bigint NOT NULL COMMENT '所属组织ID',
    `create_time` bigint NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `per_user_role`
--

LOCK
TABLES `per_user_role` WRITE;
INSERT INTO `per_user_role`
VALUES (1, 1, 1, 1, 1681268906000);
UNLOCK
TABLES;

