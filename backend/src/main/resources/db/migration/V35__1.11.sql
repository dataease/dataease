ALTER TABLE `panel_group`
ADD COLUMN `status` varchar(255) NULL DEFAULT 'publish' COMMENT '1.publish--发布 2.unpublished--未发布' AFTER `mobile_layout`;

CREATE TABLE `de_driver` (
     `id` varchar(50) NOT NULL COMMENT '主键',
     `name` varchar(50) NOT NULL COMMENT '用户ID',
     `create_time` bigint(13) NOT NULL COMMENT '创健时间',
     `type` varchar(255) DEFAULT NULL COMMENT '数据源类型',
     `driver_class` varchar(255) DEFAULT NULL COMMENT '驱动类',
     `desc` varchar(255) DEFAULT NULL COMMENT '描述',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='驱动';


CREATE TABLE `de_driver_details` (
     `id` varchar(50) NOT NULL COMMENT '主键',
     `de_driver_id` varchar(50) NOT NULL  COMMENT '驱动主键',
     `file_name` varchar(255) DEFAULT NULL COMMENT '名称',
     `version` varchar(255) DEFAULT NULL COMMENT '版本',
     `driver_class` longtext DEFAULT NULL COMMENT '驱动类',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='驱动详情';


UPDATE `sys_menu` SET `sub_count` = 1 WHERE `menu_id` = 40;
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (401, 40, 0, 2, '查看系统模板', NULL, NULL, 999, NULL, NULL, 0, 0, 0, 'sys-template:read', NULL, NULL, 1614930862373, 1614930862373);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '操作人',
  `login_name` varchar(255) NOT NULL COMMENT '登录账号',
  `nick_name` varchar(255) NOT NULL COMMENT '姓名',
  `ip` varchar(255) DEFAULT NULL COMMENT 'ip',
  `source_type` int(8) NOT NULL COMMENT '资源类型',
  `source_id` varchar(255) NOT NULL COMMENT '资源ID',
  `source_name` varchar(255) NOT NULL COMMENT '资源名称',
  `operate_type` int(8) NOT NULL COMMENT '操作类型',
  `position` longtext COMMENT '资源位置',
  `remark` longtext COMMENT '备注信息',
  `time` bigint(13) NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_menu` VALUES (618, 1, 1, 1, '日志管理', 'system-log', 'system/log/index', 15, 'log', 'log', b'0', b'0', b'0', 'log:read', NULL, NULL, NULL, 1620281952752);
INSERT INTO `sys_menu` VALUES (619, 618, 0, 2, '导出日志', NULL, NULL, 1, NULL, NULL, b'0', b'0', b'0', 'log:export', NULL, NULL, NULL, NULL);



ALTER TABLE `chart_view` ADD COLUMN `view_fields` LONGTEXT COMMENT '视图字段集合';
UPDATE `chart_view` SET `view_fields` = '[]';
ALTER TABLE `chart_view_cache` ADD COLUMN `view_fields` LONGTEXT COMMENT '视图字段集合';
UPDATE `chart_view_cache` SET `view_fields` = '[]';

INSERT INTO `my_plugin`(`plugin_id`, `name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES (11, '符号地图插件', 'default', 0, 10000, 'view', '请购买正式许可', '1.0-SNAPSHOT', NULL, 'DATAEASE', 0, NULL, 1652174790928, 'view-symbolmap-backend', NULL, NULL);


INSERT INTO `my_plugin` (`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `creator`, `load_mybatis`,
                         `install_time`, `module_name`, `ds_type`)
VALUES ('Presto 数据源插件', 'default', '0', '0', 'datasource', 'Presto 插件', '1.0-SNAPSHOT', 'DATAEASE', '0',
        '1650765903630', 'presto-backend', 'presto');