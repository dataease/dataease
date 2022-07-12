DROP TABLE IF EXISTS `dataset_row_permissions_tree`;
CREATE TABLE `dataset_row_permissions_tree`
(
    `id`               varchar(64) NOT NULL COMMENT 'ID',
    `enable`           bit(1)       DEFAULT NULL COMMENT '是否启用',
    `auth_target_type` varchar(255) DEFAULT NULL COMMENT '权限类型：dept/role/user',
    `auth_target_id`   bigint(20) DEFAULT NULL COMMENT '权限对象ID',
    `dataset_id`       varchar(64)  DEFAULT NULL COMMENT '数据集ID',
    `expression_tree`  longtext     DEFAULT NULL COMMENT '关系表达式',
    `white_list_user`  longtext     DEFAULT NULL COMMENT '用户白名单',
    `white_list_role`  longtext     DEFAULT NULL COMMENT '角色白名单',
    `white_list_dept`  longtext     DEFAULT NULL COMMENT '组织白名单',
    `update_time`      bigint(13) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `sys_startup_job`;
CREATE TABLE `sys_startup_job`
(
    `id`     varchar(64) NOT NULL COMMENT 'ID',
    `name`   varchar(255) DEFAULT NULL COMMENT '任务名称',
    `status` varchar(255) DEFAULT NULL COMMENT '任务状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

BEGIN;
INSERT INTO `sys_startup_job` VALUES ('rowPermissionsMerge', 'rowPermissionsMerge', 'ready');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `sys_user`
ADD COLUMN `phone_prefix` varchar(255) NULL COMMENT '手机号前缀' AFTER `sub`;
