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

INSERT INTO `my_plugin` (`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `creator`, `load_mybatis`,
                         `install_time`, `module_name`, `ds_type`)
VALUES ('Mongo 数据源插件', 'default', '0', '0', 'datasource', 'Mongo 数据源插件', '1.0-SNAPSHOT', 'DATAEASE', '0',
        '1650765903630', 'mongo-backend', 'mongobi');


INSERT INTO `sys_menu` (
    `menu_id`,
    `pid`,
    `sub_count`,
    `type`,
    `title`,
    `name`,
    `component`,
    `menu_sort`,
    `icon`,
    `path`,
    `i_frame`,
    `cache`,
    `hidden`,
    `permission`,
    `create_by`,
    `update_by`,
    `create_time`,
    `update_time`
)
VALUES
    (
        41,
        1,
        1,
        1,
        '模板市场',
        'template-market',
        'panel/templateMarket/index',
        13,
        'dashboard',
        'panel/templateMarket/index',
        0,
        0,
        0,
        '',
        NULL,
        NULL,
        NULL,
        1620444227389
    );

INSERT INTO `system_parameter` (`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.templateAccessKey', 'dataease', 'text', NULL);
INSERT INTO `system_parameter` (`param_key`, `param_value`, `type`, `sort`) VALUES ('basic.templateMarketUlr', 'https://dataease.io/templates', 'text', 4);
