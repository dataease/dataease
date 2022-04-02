ALTER TABLE `chart_view` ADD COLUMN `senior` LONGTEXT COMMENT '高级' AFTER `drill_fields`;
UPDATE `chart_view` SET `senior` = '{}';

CREATE TABLE `chart_view_cache` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(1024) DEFAULT NULL COMMENT '名称',
  `title` varchar(1024) DEFAULT NULL COMMENT 'EChart标题',
  `scene_id` varchar(50) NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
  `table_id` varchar(50) NOT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `render` varchar(50) DEFAULT NULL COMMENT '视图渲染方式',
  `result_count` int(10) DEFAULT NULL COMMENT '展示结果',
  `result_mode` varchar(50) DEFAULT NULL COMMENT '展示模式',
  `x_axis` longtext COMMENT '横轴field',
  `x_axis_ext` longtext COMMENT 'table-row',
  `y_axis` longtext COMMENT '纵轴field',
  `y_axis_ext` longtext COMMENT '副轴',
  `ext_stack` longtext COMMENT '堆叠项',
  `ext_bubble` longtext COMMENT '气泡大小',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_style` longtext COMMENT '组件样式',
  `custom_filter` longtext COMMENT '结果过滤',
  `drill_fields` longtext COMMENT '钻取字段',
  `senior` longtext COMMENT '高级',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 视图',
  `chart_type` varchar(255) DEFAULT 'private' COMMENT '视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板',
  `is_plugin` bit(1) DEFAULT NULL COMMENT '是否插件',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `chart_view`
ADD COLUMN `data_from` varchar(255) NULL DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据' AFTER `is_plugin`;

ALTER TABLE `chart_view_cache`
ADD COLUMN `data_from` varchar(255) NULL DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据' AFTER `is_plugin`;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for panel_group_extend
-- ----------------------------
DROP TABLE IF EXISTS `panel_group_extend`;
CREATE TABLE `panel_group_extend` (
  `id` varchar(50) NOT NULL,
  `panel_id` varchar(50) DEFAULT NULL,
  `template_id` varchar(50) DEFAULT NULL COMMENT '模板来源id',
  `template_dynamic_data` longtext COMMENT '模板动态数据',
  `template_version` varchar(255) DEFAULT NULL COMMENT '模板版本号(预留)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for panel_group_extend_data
-- ----------------------------
DROP TABLE IF EXISTS `panel_group_extend_data`;
CREATE TABLE `panel_group_extend_data` (
  `id` varchar(50) NOT NULL,
  `panel_id` varchar(50) DEFAULT NULL,
  `view_id` varchar(50) DEFAULT NULL,
  `view_details` longtext,
  `copy_from` varchar(255) DEFAULT NULL,
  `copy_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;


SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE `de_engine` (
 `id` varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
 `name` varchar(50) DEFAULT NULL COMMENT '名称',
 `desc` varchar(50) DEFAULT NULL COMMENT '描述',
 `type` varchar(50) NOT NULL COMMENT '类型',
 `configuration` longtext NOT NULL COMMENT '详细信息',
 `create_time` bigint(13) DEFAULT NULL COMMENT 'Create timestamp',
 `update_time` bigint(13) DEFAULT NULL COMMENT 'Update timestamp',
 `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
 `status` varchar(45) DEFAULT NULL COMMENT '状态',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.mobileBG', NULL, 'file', 14);

UPDATE `demo_gdp_district_top100` set `province` = '新疆维吾尔自治区' WHERE `province` = '新疆维吾尔族自治区';

ALTER TABLE `sys_auth`
ADD COLUMN `copy_from` varchar(255) NULL COMMENT '复制来源' AFTER `update_time`,
ADD COLUMN `copy_id` varchar(255) NULL COMMENT '复制ID' AFTER `copy_from`;

ALTER TABLE `sys_auth_detail`
ADD COLUMN `copy_from` varchar(255) NULL COMMENT '复制来源' AFTER `update_time`,
ADD COLUMN `copy_id` varchar(255) NULL COMMENT '复制ID' AFTER `copy_from`;



DROP FUNCTION IF EXISTS `copy_auth`;
delimiter ;;
CREATE FUNCTION `copy_auth`(authSource varchar(255),authSourceType varchar(255),authUser varchar(255))
 RETURNS varchar(255) CHARSET utf8mb4
  READS SQL DATA
BEGIN

DECLARE authId varchar(255);

DECLARE userId  varchar(255);

DECLARE copyId  varchar(255);

select uuid() into authId;

select uuid() into copyId;

select max(sys_user.user_id) into userId from sys_user where username= authUser;

delete from sys_auth_detail where auth_id in (
select id from  sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType
);

delete from sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType;

INSERT INTO sys_auth (
 id,
 auth_source,
 auth_source_type,
 auth_target,
 auth_target_type,
 auth_time,
 auth_user
)
VALUES
 (
  authId,
  authSource,
  authSourceType,
  userId,
  'user',
 unix_timestamp(
 now())* 1000,'auto');

 INSERT INTO  sys_auth_detail (
            id,
            auth_id,
            privilege_name,
            privilege_type,
            privilege_value,
            privilege_extend,
            remark,
            create_user,
            create_time
        ) SELECT
        uuid() AS id,
        authId AS auth_id,
        sys_auth_detail.privilege_name,
        sys_auth_detail.privilege_type,
        1,
        sys_auth_detail.privilege_extend,
        sys_auth_detail.remark,
        'auto' AS create_user,
        unix_timestamp(now())* 1000 AS create_time
        FROM
            sys_auth_detail where auth_id =authSourceType;

/**继承第一父级权限**/

insert into sys_auth(
id,
 auth_source,
 auth_source_type,
 auth_target,
 auth_target_type,
 auth_time,
 auth_user,
 copy_from,
 copy_id
)
SELECT
 uuid() as id,
 authSource as auth_source,
 authSourceType as auth_source_type,
 auth_target,
 auth_target_type,
 NOW()* 1000 as auth_time,
 'auto' as auth_user,
 id as copy_from,
 copyId as copy_id
FROM
 sys_auth
WHERE
 auth_source = (
 SELECT
  pid
 FROM
  v_auth_model
 WHERE
  id = authSource
  AND model_type = authSourceType
 )
 AND auth_source_type = authSourceType
 and  concat(auth_target,'-',auth_target_type) !=CONCAT(userId,'-','user');

INSERT INTO sys_auth_detail (
 id,
 auth_id,
 privilege_name,
 privilege_type,
 privilege_value,
 privilege_extend,
 remark,
 create_user,
 create_time,
 copy_from,
 copy_id
) SELECT
uuid() AS id,
sa_copy.t_id AS auth_id,
sys_auth_detail.privilege_name,
sys_auth_detail.privilege_type,
sys_auth_detail.privilege_value,
sys_auth_detail.privilege_extend,
sys_auth_detail.remark,
'auto' AS create_user,
unix_timestamp(
now())* 1000 AS create_time,
id AS copy_from,
copyId AS copy_id
FROM
 sys_auth_detail
 INNER JOIN (
 SELECT
  id AS t_id,
  copy_from AS s_id
 FROM
  sys_auth
 WHERE
  copy_id = copyId
 ) sa_copy ON sys_auth_detail.auth_id = sa_copy.s_id;

RETURN 'success';

END
;;
delimiter ;

-- ----------------------------
-- Function structure for delete_auth_source
-- ----------------------------
DROP FUNCTION IF EXISTS `delete_auth_source`;
delimiter ;;
CREATE FUNCTION `delete_auth_source`(authSource varchar(255),authSourceType varchar(255))
 RETURNS varchar(255) CHARSET utf8mb4
  READS SQL DATA
BEGIN

delete from sys_auth_detail where auth_id in (
select id from  sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType
);

delete from sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType;

RETURN 'success';

END
;;
delimiter ;

INSERT INTO `my_plugin`(`plugin_id`, `name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`) VALUES (3, '选项卡插件', 'default', 0, 20000, 'panel', '选项卡插件', '1.0-SNAPSHOT', NULL, 'fit2cloud-chenyw', 0, NULL, NULL, 'dataease-extensions-tabs-backend', NULL);

ALTER TABLE `panel_link_jump_info`
ADD COLUMN `attach_params` tinyint(1) NULL COMMENT '是否附加点击参数' AFTER `checked`;


update `sys_menu` set menu_id = 100 where title = '首页';

INSERT INTO `sys_menu` VALUES (101, 1, 4, 1, '插件管理', 'system-plugin', 'system/plugin/index', 1002, 'plugins', 'plugin', b'0', b'0', b'0', 'plugin:read', NULL, NULL, NULL, 1620281952752);

DROP FUNCTION IF EXISTS `GET_CHART_VIEW_COPY_NAME`;
delimiter ;;
CREATE FUNCTION `GET_CHART_VIEW_COPY_NAME`(chartId varchar(255),pid varchar(255))
 RETURNS varchar(255) CHARSET utf8mb4
  READS SQL DATA
BEGIN

DECLARE chartName varchar(255);

DECLARE regexpInfo varchar(255);

DECLARE chartNameCount INTEGER;

select `name`  into chartName from chart_view where id =chartId;
/**
因为名称存在（）等特殊字符，所以不能直接用REGEXP进行查找，qrtz_locks
1.用like 'chartName%' 过滤可能的数据项
2.REPLACE(name,chartName,'') REGEXP '-copy\\(([0-9])+\\)$' 过滤去掉chartName后的字符以 -copy(/d) 结尾的数据
3.(LENGTH(REPLACE(name,chartName,''))-LENGTH(replace(REPLACE(name,chartName,''),'-',''))=1) 确定只出现一次 ‘-’ 防止多次copy
**/
select (count(1)+1) into chartNameCount from chart_view
where (LENGTH(REPLACE(name,chartName,''))-LENGTH(replace(REPLACE(name,chartName,''),'-',''))=1)
and REPLACE(name,chartName,'') REGEXP '-copy\\(([0-9])+\\)$' and name like CONCAT(chartName,'%') and chart_view.scene_id=pid ;

RETURN concat(chartName,'-copy(',chartNameCount,')');

END
;;
delimiter ;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for panel_outer_params
-- ----------------------------
CREATE TABLE `panel_outer_params` (
  `params_id` varchar(50) NOT NULL,
  `panel_id` varchar(50) DEFAULT NULL,
  `checked` tinyint(1) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `copy_from` varchar(50) DEFAULT NULL,
  `copy_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`params_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for panel_outer_params_info
-- ----------------------------
CREATE TABLE `panel_outer_params_info` (
  `params_info_id` varchar(50) NOT NULL,
  `params_id` varchar(50) DEFAULT NULL,
  `param_name` varchar(255) DEFAULT NULL COMMENT '参数名',
  `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `copy_from` varchar(255) DEFAULT NULL,
  `copy_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`params_info_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for panel_outer_params_target_view_info
-- ----------------------------
CREATE TABLE `panel_outer_params_target_view_info` (
  `target_id` varchar(50) NOT NULL,
  `params_info_id` varchar(50) DEFAULT NULL,
  `target_view_id` varchar(50) DEFAULT NULL,
  `target_field_id` varchar(50) DEFAULT NULL,
  `copy_from` varchar(255) DEFAULT NULL,
  `copy_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
update `my_plugin` set `name` = 'X-Pack默认插件' where `plugin_id` = 1;
update `my_plugin` set `module_name` = 'view-bubblemap-backend' where `plugin_id` = 2;

DROP TRIGGER `new_auth_panel`;
DROP TRIGGER `new_auth_dataset_group`;
DROP TRIGGER `new_auth_dataset_table`;
DROP TRIGGER `new_auth_link`;
