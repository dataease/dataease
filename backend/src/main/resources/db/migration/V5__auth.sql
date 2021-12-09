SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_auth
-- ----------------------------
CREATE TABLE `sys_auth`  (
  `id` varchar(50) NOT NULL,
  `auth_source` varchar(255) NULL DEFAULT NULL COMMENT '授权资产源 数据集 视图 仪表板',
  `auth_source_type` varchar(255) NULL DEFAULT NULL COMMENT '授权资产类型 dataset 数据集 view 视图 panel 仪表板',
  `auth_target` varchar(255) NULL DEFAULT NULL COMMENT '授权目标 用户 角色 组织 ',
  `auth_target_type` varchar(255) NULL DEFAULT NULL COMMENT '授权目标类型 user 用户 role 角色 org dept 组织',
  `auth_time` bigint(13) NULL DEFAULT NULL COMMENT '授权时间',
  `auth_details` varchar(2000) NULL DEFAULT NULL COMMENT '授权明细 privilegename 名称 privilegeType 权限类型 1 查看 2 管理 3 导出 4 使用 ; privilegeValue 1 不可用 2 可用 3 部分可用',
  `auth_user` varchar(255) NULL DEFAULT NULL COMMENT '授权人员',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Table structure for sys_auth_detail
-- ----------------------------
CREATE TABLE `sys_auth_detail`  (
  `id` varchar(50) NOT NULL,
  `auth_id` varchar(50) NULL DEFAULT NULL,
  `privilege_name` varchar(255) NULL DEFAULT NULL COMMENT '权限名称',
  `privilege_type` int(6) NULL DEFAULT NULL COMMENT '权限类型',
  `privilege_value` int(6) NULL DEFAULT NULL COMMENT '权限值 1 可用 2 不用',
  `privilege_extend` varchar(2000) NULL DEFAULT NULL COMMENT '权限扩展',
  `remark` varchar(2000) NULL DEFAULT NULL COMMENT '备注',
  `create_user` varchar(255) NULL DEFAULT NULL,
  `create_time` bigint(13) NULL DEFAULT NULL,
  `update_time` bigint(13) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Records of sys_auth_detail
-- ----------------------------
BEGIN;
INSERT INTO `sys_auth_detail`
VALUES
('chart_grant', 'chart', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL),
('chart_manage', 'chart', 'i18n_auth_manage', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL),
('chart_use', 'chart', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL),
('dataset_grant', 'dataset', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL),
('dataset_manege', 'dataset', 'i18n_auth_manage', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL),
('dataset_use', 'dataset', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL),
('link_grant', 'link', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL),
('link_manage', 'link', 'i18n_auth_manage', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL),
('link_use', 'link', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL),
('menu_grant', 'menu', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL),
('menu_use', 'menu', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL),
('panel_export', 'panel', 'i18n_auth_export', 3, 0, 'export', '基础权限-导出', 'system', NULL, NULL),
('panel_grant', 'panel', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL),
('panel_manage', 'panel', 'i18n_auth_manage', 5, 0, 'manage', '基础权限-管理', 'system', NULL, NULL),
('panel_use', 'panel', 'i18n_auth_view', 1, 0, 'view', '基础权限-查看', 'system', NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- View structure for v_auth_model
-- ----------------------------
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS select `sys_user`.`user_id` AS `id`,`sys_user`.`username` AS `name`,`sys_user`.`username` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'user' AS `model_type`,'user' AS `model_inner_type`,'target' AS `auth_type`,`sys_user`.`create_by` AS `create_by` from `sys_user` where (`sys_user`.`is_admin` <> 1) union all select `sys_role`.`role_id` AS `id`,`sys_role`.`name` AS `name`,`sys_role`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'role' AS `model_type`,'role' AS `model_inner_type`,'target' AS `auth_type`,`sys_role`.`create_by` AS `create_by` from `sys_role` union all select `sys_dept`.`dept_id` AS `id`,`sys_dept`.`name` AS `name`,`sys_dept`.`name` AS `lable`,cast( `sys_dept`.`pid` AS CHAR  CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci  AS `pid`,if((`sys_dept`.`sub_count` = 0),'leaf','spine') AS `node_type`,'dept' AS `model_type`,'dept' AS `model_inner_type`,'target' AS `auth_type`,`sys_dept`.`create_by` AS `create_by` from `sys_dept` union all select `datasource`.`id` AS `id`,`datasource`.`name` AS `NAME`,`datasource`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'link' AS `model_type`,`datasource`.`type` AS `model_inner_type`,'source' AS `auth_type`,`datasource`.`create_by` AS `create_by` from `datasource` union all select `dataset_group`.`id` AS `id`,`dataset_group`.`name` AS `NAME`,`dataset_group`.`name` AS `lable`,if(isnull(`dataset_group`.`pid`),'0',`dataset_group`.`pid`) AS `pid`,'spine' AS `node_type`,'dataset' AS `model_type`,`dataset_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_group`.`create_by` AS `create_by` from `dataset_group` union all select `dataset_table`.`id` AS `id`,`dataset_table`.`name` AS `NAME`,`dataset_table`.`name` AS `lable`,`dataset_table`.`scene_id` AS `pid`,'leaf' AS `node_type`,'dataset' AS `model_type`,`dataset_table`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_table`.`create_by` AS `create_by` from `dataset_table` union all select `chart_group`.`id` AS `id`,`chart_group`.`name` AS `name`,`chart_group`.`name` AS `label`,if(isnull(`chart_group`.`pid`),'0',`chart_group`.`pid`) AS `pid`,'spine' AS `node_type`,'chart' AS `model_type`,`chart_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_group`.`create_by` AS `create_by` from `chart_group` union all select `chart_view`.`id` AS `id`,`chart_view`.`name` AS `name`,`chart_view`.`name` AS `label`,`chart_view`.`scene_id` AS `pid`,'leaf' AS `node_type`,'chart' AS `model_type`,`chart_view`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_view`.`create_by` AS `create_by` from `chart_view` union all select `panel_group`.`id` AS `id`,`panel_group`.`name` AS `NAME`,`panel_group`.`name` AS `label`,(case `panel_group`.`id` when 'panel_list' then '0' when 'default_panel' then '0' else `panel_group`.`pid` end) AS `pid`,if((`panel_group`.`node_type` = 'folder'),'spine','leaf') AS `node_type`,'panel' AS `model_type`,`panel_group`.`panel_type` AS `model_inner_type`,'source' AS `auth_type`,`panel_group`.`create_by` AS `create_by` from `panel_group` union all select `sys_menu`.`menu_id` AS `menu_id`,`sys_menu`.`title` AS `name`,`sys_menu`.`title` AS `label`,`sys_menu`.`pid` AS `pid`,if((`sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`sys_menu`.`create_by` AS `create_by` from `sys_menu` where ((`sys_menu`.`hidden` = 0) and ((`sys_menu`.`name` <> 'panel') or isnull(`sys_menu`.`name`))) union all select `plugin_sys_menu`.`menu_id` AS `menu_id`,`plugin_sys_menu`.`title` AS `name`,`plugin_sys_menu`.`title` AS `label`,`plugin_sys_menu`.`pid` AS `pid`,if((`plugin_sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `plugin_sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`plugin_sys_menu`.`create_by` AS `create_by` from `plugin_sys_menu` where (`plugin_sys_menu`.`hidden` = 0);

-- ----------------------------
-- View structure for v_auth_privilege
-- ----------------------------
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_privilege` AS select `sys_auth`.`auth_source` AS `auth_source`,`sys_auth`.`auth_source_type` AS `auth_source_type`,group_concat(`sys_auth_detail`.`privilege_extend` separator ',') AS `privileges` from (`sys_auth` left join `sys_auth_detail` on((`sys_auth`.`id` = `sys_auth_detail`.`auth_id`))) where ((`sys_auth_detail`.`privilege_value` = 1) and (((`sys_auth`.`auth_target_type` = 'dept') and (`sys_auth`.`auth_target` = (select `sys_user`.`dept_id` from `sys_user` where (`sys_user`.`user_id` = '4')))) or ((`sys_auth`.`auth_target_type` = 'user') and (`sys_auth`.`auth_target` = '4')) or ((`sys_auth`.`auth_target_type` = 'role') and (`sys_auth`.`auth_target` = (select `sys_users_roles`.`role_id` from `sys_users_roles` where (`sys_users_roles`.`user_id` = '4')))))) group by `sys_auth`.`auth_source`,`sys_auth`.`auth_source_type`;

-- ----------------------------
-- Function structure for CHECK_TREE_NO_MANAGE_PRIVILEGE
-- ----------------------------
delimiter ;;
CREATE FUNCTION `CHECK_TREE_NO_MANAGE_PRIVILEGE`(userId varchar(255),modelType varchar(255),nodeId varchar(255))
 RETURNS int(11)
  READS SQL DATA
BEGIN

DECLARE privilegeType INTEGER;
DECLARE allTreeIds longtext;
DECLARE allPrivilegeTreeIds longtext;
DECLARE result INTEGER;

select privilege_type into privilegeType from sys_auth_detail where auth_id =modelType and privilege_extend ='manage';
select GET_V_AUTH_MODEL_WITH_CHILDREN( nodeId ,modelType) into allTreeIds;
select GET_V_AUTH_MODEL_WITH_PRIVILEGE(userId,modelType,privilegeType) into allPrivilegeTreeIds;
select count(id) into result from v_auth_model where v_auth_model.model_type=modelType and FIND_IN_SET(v_auth_model.id,allTreeIds) and (!FIND_IN_SET(v_auth_model.id,allPrivilegeTreeIds) or ISNULL(allPrivilegeTreeIds));

RETURN result;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for copy_auth
-- ----------------------------
delimiter ;;
CREATE FUNCTION `copy_auth`(authSource varchar(255),authSourceType varchar(255),authUser varchar(255))
 RETURNS varchar(255)
  READS SQL DATA
BEGIN

DECLARE authId varchar(255);

DECLARE userId  varchar(255);

select uuid() into authId;

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
RETURN 'sucess';

END
;;
delimiter ;

-- ----------------------------
-- Function structure for delete_auth_source
-- ----------------------------
delimiter ;;
CREATE FUNCTION `delete_auth_source`(authSource varchar(255),authSourceType varchar(255))
 RETURNS varchar(255)
  READS SQL DATA
BEGIN

delete from sys_auth_detail where auth_id in (
select id from  sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType
);

delete from sys_auth where sys_auth.auth_source=authSource and sys_auth.auth_source_type=authSourceType;

RETURN 'sucess';

END
;;
delimiter ;

-- ----------------------------
-- Function structure for delete_auth_target
-- ----------------------------
delimiter ;;
CREATE FUNCTION `delete_auth_target`(authTarget varchar(255),authTargetType varchar(255))
 RETURNS varchar(255)
  READS SQL DATA
BEGIN

delete from sys_auth_detail where auth_id in (
select id from  sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType
);

delete from sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType;

RETURN 'sucess';

END
;;
delimiter ;

-- ----------------------------
-- Function structure for get_auths
-- ----------------------------
delimiter ;;
CREATE FUNCTION `get_auths`(authSource varchar(255),modelType varchar(255),userId varchar(255))
 RETURNS longtext
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

SELECT
	group_concat( DISTINCT sys_auth_detail.privilege_extend) into oTemp
FROM
	(
		`sys_auth`
		LEFT JOIN `sys_auth_detail` ON ((
				`sys_auth`.`id` = `sys_auth_detail`.`auth_id`
			)))
			where sys_auth_detail.privilege_value =1
			and sys_auth.auth_source=authSource
			AND (
				(
					sys_auth.auth_target_type = 'dept'
					AND sys_auth.auth_target in ( SELECT dept_id FROM sys_user WHERE user_id = userId )
				)
				OR (
					sys_auth.auth_target_type = 'user'
					AND sys_auth.auth_target = userId
				)
				OR (
					sys_auth.auth_target_type = 'role'
					AND sys_auth.auth_target in ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
				)
			)
GROUP BY
	`sys_auth`.`auth_source`,
	`sys_auth`.`auth_source_type`;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for get_auth_children_count
-- ----------------------------
delimiter ;;
CREATE FUNCTION `get_auth_children_count`(pidInfo varchar(255),modelType varchar(255),userName varchar(255))
 RETURNS varchar(255)
  READS SQL DATA
BEGIN

DECLARE childrenCount INTEGER;

select count(1)-1 into childrenCount from v_auth_model where FIND_IN_SET(
		v_auth_model.id,
	GET_V_AUTH_MODEL_WITH_CHILDREN ( pidInfo, modelType ))
	AND create_by = userName
	AND v_auth_model.node_type = 'leaf';

RETURN childrenCount;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for get_grant_auths
-- ----------------------------
delimiter ;;
CREATE FUNCTION `get_grant_auths`(modelType VARCHAR ( 255 ),
	userId VARCHAR ( 255 ))
 RETURNS longtext
  READS SQL DATA
BEGIN
	DECLARE
		oTemp LONGTEXT;
	SELECT
		GROUP_CONCAT( DISTINCT v_auth_model.id ) into oTemp
	FROM
		v_auth_model
		LEFT JOIN sys_auth ON v_auth_model.id = sys_auth.auth_source
		AND v_auth_model.model_type = sys_auth.auth_source_type
		LEFT JOIN sys_auth_detail ON sys_auth.id = sys_auth_detail.auth_id
	WHERE
		privilege_type = 15
		AND privilege_value = 1
		AND v_auth_model.model_type = modelType
		AND (
			(
				sys_auth.auth_target_type = 'dept'
				AND sys_auth.auth_target IN ( SELECT dept_id FROM sys_user WHERE user_id = userId )
			)
			OR (
				sys_auth.auth_target_type = 'user'
				AND sys_auth.auth_target = userId
			)
			OR (
				sys_auth.auth_target_type = 'role'
				AND sys_auth.auth_target IN ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
			)
		);
	RETURN oTemp;

	END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_PANEL_GROUP_WITH_CHILDREN
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_PANEL_GROUP_WITH_CHILDREN`(parentId varchar(8000))
 RETURNS varchar(8000)
  READS SQL DATA
BEGIN

DECLARE oTemp VARCHAR(8000);

DECLARE oTempChild VARCHAR(8000);

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci;

WHILE oTempChild IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempChild);

SELECT GROUP_CONCAT(id) INTO oTempChild FROM panel_group WHERE FIND_IN_SET(pid,oTempChild) > 0;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_PANEL_TEMPLATE_WITH_CHILDREN
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_PANEL_TEMPLATE_WITH_CHILDREN`(parentId varchar(8000))
 RETURNS varchar(8000)
  READS SQL DATA
BEGIN

DECLARE oTemp VARCHAR(8000);

DECLARE oTempChild VARCHAR(8000);

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci;


WHILE oTempChild IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempChild);

SELECT GROUP_CONCAT(id) INTO oTempChild FROM panel_template WHERE FIND_IN_SET(pid,oTempChild) > 0;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_ID_P_USE
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_ID_P_USE`(userId longtext,modelType varchar(255))
 RETURNS longtext
  READS SQL DATA
BEGIN

DECLARE oTempLeafIds longtext;
DECLARE oTempAllIds longtext;

select GET_V_AUTH_MODEL_WITH_PRIVILEGE(userId,modelType,1) into oTempLeafIds;

select GROUP_CONCAT(id) into oTempAllIds from (select GET_V_AUTH_MODEL_WITH_PARENT ( oTempLeafIds ,modelType) cids) t, v_auth_model where v_auth_model.model_type=modelType and FIND_IN_SET(v_auth_model.id,cids) order by id asc;

RETURN oTempAllIds;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_WITH_CHILDREN
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_WITH_CHILDREN`(parentId longtext,modelType varchar(255))
 RETURNS longtext
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

DECLARE oTempChild longtext;

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci;

WHILE oTempChild IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempChild);

SELECT GROUP_CONCAT(id) INTO oTempChild FROM V_AUTH_MODEL WHERE FIND_IN_SET(pid,oTempChild) > 0 and V_AUTH_MODEL.model_type=modelType order by id asc;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_WITH_PARENT
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_WITH_PARENT`(childrenId longtext,modelType varchar(255))
 RETURNS longtext
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

DECLARE oTempParent longtext;

SET oTemp = '';

SET oTempParent = CAST(childrenId AS CHAR CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci;

WHILE oTempParent IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempParent);

SELECT GROUP_CONCAT(distinct pid) INTO oTempParent FROM V_AUTH_MODEL WHERE FIND_IN_SET(id,oTempParent) > 0 and V_AUTH_MODEL.model_type=modelType order by pid asc;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_WITH_PRIVILEGE
-- ----------------------------
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_WITH_PRIVILEGE`(userId longtext,modelType varchar(255),privilegeType varchar(255))
 RETURNS longtext
  READS SQL DATA
BEGIN

DECLARE oTempLeafIds longtext;
select GROUP_CONCAT(auth_source) into oTempLeafIds from (
SELECT
			sys_auth.auth_source_type,
			sys_auth.auth_source
		FROM
			sys_auth
			LEFT JOIN sys_auth_detail ON sys_auth.id = sys_auth_detail.auth_id
		WHERE
			sys_auth_detail.privilege_type = privilegeType
			and sys_auth.auth_source_type = modelType
			AND (
				(
					sys_auth.auth_target_type = 'dept'
					AND sys_auth.auth_target in ( SELECT dept_id FROM sys_user WHERE user_id = userId )
				)
				OR (
					sys_auth.auth_target_type = 'user'
					AND sys_auth.auth_target = userId
				)
				OR (
					sys_auth.auth_target_type = 'role'
					AND sys_auth.auth_target in ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
				)
				OR (1 = ( SELECT is_admin FROM sys_user WHERE user_id = userId ))
			)
		GROUP BY
			sys_auth.auth_source_type,
			sys_auth.auth_source
			having  (sum( sys_auth_detail.privilege_value )> 0 or 1 = ( SELECT is_admin FROM sys_user WHERE user_id = userId ))) temp;
RETURN oTempLeafIds;
END
;;
delimiter ;




CREATE TRIGGER `new_auth_link` AFTER INSERT ON `datasource` FOR EACH ROW select copy_auth(NEW.id,'link',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_link` AFTER DELETE ON `datasource` FOR EACH ROW select delete_auth_source(OLD.id,'link') into @ee;

CREATE TRIGGER `new_auth_dataset_group` AFTER INSERT ON `dataset_group` FOR EACH ROW select copy_auth(NEW.id,'dataset',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_dataset_group` AFTER DELETE ON `dataset_group` FOR EACH ROW select delete_auth_source(OLD.id,'dataset') into @ee;
CREATE TRIGGER `new_auth_dataset_table` AFTER INSERT ON `dataset_table` FOR EACH ROW select copy_auth(NEW.id,'dataset',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_dataset_table` AFTER DELETE ON `dataset_table` FOR EACH ROW select delete_auth_source(OLD.id,'dataset') into @ee;


CREATE TRIGGER `new_auth_chart_group` AFTER INSERT ON `chart_group` FOR EACH ROW select copy_auth(NEW.id,'chart',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_chart_group` AFTER DELETE ON `chart_group` FOR EACH ROW select delete_auth_source(OLD.id,'chart') into @ee;
CREATE TRIGGER `new_auth_chart_view` AFTER INSERT ON `chart_view` FOR EACH ROW select copy_auth(NEW.id,'chart',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_chart_view` AFTER DELETE ON `chart_view` FOR EACH ROW select delete_auth_source(OLD.id,'chart') into @ee;

CREATE TRIGGER `new_auth_panel` AFTER INSERT ON `panel_group` FOR EACH ROW select copy_auth(NEW.id,'panel',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_panel` AFTER DELETE ON `panel_group` FOR EACH ROW select delete_auth_source(OLD.id,'panel') into @ee;


CREATE TRIGGER `delete_auth_user_target` AFTER DELETE ON `sys_user` FOR EACH ROW select delete_auth_target(OLD.user_id,'user') into @ee;
CREATE TRIGGER `delete_auth_role_target` AFTER DELETE ON `sys_role` FOR EACH ROW select delete_auth_target(OLD.role_id,'role') into @ee;
CREATE TRIGGER `delete_auth_dept_target` AFTER DELETE ON `sys_dept` FOR EACH ROW select delete_auth_target(OLD.dept_id,'dept') into @ee;
