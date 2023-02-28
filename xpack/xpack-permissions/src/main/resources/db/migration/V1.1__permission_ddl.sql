DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`           bigint                                                     NOT NULL COMMENT '用户ID',
    `account`      varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_general_ci  NOT NULL COMMENT '账号',
    `name`         varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_general_ci  NOT NULL COMMENT '姓名',
    `phone`        varchar(20) DEFAULT NULL COMMENT '手机号',
    `phone_prefix` varchar(15) DEFAULT NULL COMMENT '手机号前缀',
    `email`        varchar(120) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
    `pwd`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8_general_ci  NOT NULL COMMENT '密码',
    `language`     varchar(10) CHARACTER SET utf8mb3 COLLATE utf8_general_ci  NOT NULL COMMENT '语言',
    `from`         int                                                        NOT NULL COMMENT '来源',
    `enable`       tinyint(1) NOT NULL COMMENT '启用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

BEGIN;
INSERT INTO `sys_user` (`id`, `account`, `name`, `phone`, `phone_prefix`, `email`, `pwd`, `language`, `from`, `enable`) VALUES (1, 'cyw', '陈亚文', '15316082650', '+86', 'yawen.chen@fit2cloud.com', '1', 'en', 0, 1);
COMMIT;