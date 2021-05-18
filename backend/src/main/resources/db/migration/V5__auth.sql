
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_auth
-- ----------------------------
DROP TABLE IF EXISTS `sys_auth`;
CREATE TABLE `sys_auth`  (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `auth_source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权资产源 数据集 视图 仪表板',
  `auth_source_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权资产类型 dataset 数据集 view 视图 panel 仪表板',
  `auth_target` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权目标 用户 角色 组织 ',
  `auth_target_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权目标类型 user 用户 role 角色 org dept 组织',
  `auth_time` bigint(13) NULL DEFAULT NULL COMMENT '授权时间',
  `auth_details` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权明细 privilegename 名称 privilegeType 权限类型 1 查看 2 管理 3 导出 4 使用 ; privilegeValue 1 不可用 2 可用 3 部分可用',
  `auth_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权人员',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;

-- ----------------------------
-- Table structure for sys_auth_detail
-- ----------------------------
DROP TABLE IF EXISTS `sys_auth_detail`;
CREATE TABLE `sys_auth_detail`  (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `auth_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `privilege_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `privilege_type` int(6) NULL DEFAULT NULL COMMENT '权限类型',
  `privilege_value` int(6) NULL DEFAULT NULL COMMENT '权限值 1 可用 2 不用',
  `privilege_extend` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限扩展',
  `remark` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` bigint(13) NULL DEFAULT NULL,
  `update_time` bigint(13) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;

-- ----------------------------
-- Records of sys_auth_detail
-- ----------------------------
BEGIN;
INSERT INTO `sys_auth_detail` VALUES ('chart_manage', 'chart', '管理', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL), ('chart_use', 'chart', '使用', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL), ('dataset_manege', 'dataset', '管理', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL), ('dataset_use', 'dataset', '使用', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL), ('link_manage', 'link', '管理', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL), ('link_user', 'link', '使用', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL), ('panel_export', 'panel', '导出', 3, 0, 'export', '基础权限-导出', 'system', NULL, NULL), ('panel_manage', 'panel', '编辑', 5, 0, 'manage', '基础权限-管理', 'system', NULL, NULL), ('panel_user', 'panel', '查看', 1, 0, 'view', '基础权限-查看', 'system', NULL, NULL);
COMMIT;

-- ----------------------------
-- View structure for v_auth_model
-- ----------------------------
DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS select `sys_user`.`user_id` AS `id`,`sys_user`.`username` AS `name`,`sys_user`.`username` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'user' AS `model_type`,'user' AS `model_inner_type`,'target' AS `auth_type`,`sys_user`.`create_by` AS `create_by` from `sys_user` union all select `sys_role`.`role_id` AS `id`,`sys_role`.`name` AS `name`,`sys_role`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'role' AS `model_type`,'role' AS `model_inner_type`,'target' AS `auth_type`,`sys_role`.`create_by` AS `create_by` from `sys_role` union all select `sys_dept`.`dept_id` AS `id`,`sys_dept`.`name` AS `name`,`sys_dept`.`name` AS `lable`,cast(`sys_dept`.`pid` as char charset utf8mb4) AS `pid`,if((`sys_dept`.`sub_count` = 0),'leaf','spine') AS `node_type`,'dept' AS `model_type`,'dept' AS `model_inner_type`,'target' AS `auth_type`,`sys_dept`.`create_by` AS `create_by` from `sys_dept` union all select `datasource`.`id` AS `id`,`datasource`.`name` AS `NAME`,`datasource`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'link' AS `model_type`,`datasource`.`type` AS `model_inner_type`,'source' AS `auth_type`,`datasource`.`create_by` AS `create_by` from `datasource` union all select `dataset_group`.`id` AS `id`,`dataset_group`.`name` AS `NAME`,`dataset_group`.`name` AS `lable`,if(isnull(`dataset_group`.`pid`),'0',`dataset_group`.`pid`) AS `pid`,'spine' AS `node_type`,'dataset' AS `model_type`,`dataset_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_group`.`create_by` AS `create_by` from `dataset_group` union all select `dataset_table`.`id` AS `id`,`dataset_table`.`name` AS `NAME`,`dataset_table`.`name` AS `lable`,`dataset_table`.`scene_id` AS `pid`,'leaf' AS `node_type`,'dataset' AS `model_type`,`dataset_table`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_table`.`create_by` AS `create_by` from `dataset_table` union all select `chart_group`.`id` AS `id`,`chart_group`.`name` AS `name`,`chart_group`.`name` AS `label`,if(isnull(`chart_group`.`pid`),'0',`chart_group`.`pid`) AS `pid`,'spine' AS `node_type`,'chart' AS `model_type`,`chart_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_group`.`create_by` AS `create_by` from `chart_group` union all select `chart_view`.`id` AS `id`,`chart_view`.`name` AS `name`,`chart_view`.`name` AS `label`,`chart_view`.`scene_id` AS `pid`,'leaf' AS `node_type`,'chart' AS `model_type`,`chart_view`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_view`.`create_by` AS `create_by` from `chart_view` union all select `panel_group`.`id` AS `id`,`panel_group`.`name` AS `NAME`,`panel_group`.`name` AS `label`,if(isnull(`panel_group`.`pid`),'0',`panel_group`.`pid`) AS `pid`,if((`panel_group`.`node_type` = 'folder'),'spine','leaf') AS `node_type`,'panel' AS `model_type`,`panel_group`.`panel_type` AS `model_inner_type`,'source' AS `auth_type`,`panel_group`.`create_by` AS `create_by` from `panel_group` where (`panel_group`.`panel_type` = 'self');

-- ----------------------------
-- View structure for v_auth_privilege
-- ----------------------------
DROP VIEW IF EXISTS `v_auth_privilege`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_privilege` AS select `sys_auth`.`auth_source` AS `auth_source`,`sys_auth`.`auth_source_type` AS `auth_source_type`,group_concat(`sys_auth_detail`.`privilege_extend` separator ',') AS `privileges` from (`sys_auth` left join `sys_auth_detail` on((`sys_auth`.`id` = `sys_auth_detail`.`auth_id`))) where ((`sys_auth_detail`.`privilege_value` = 1) and (((`sys_auth`.`auth_target_type` = 'dept') and (`sys_auth`.`auth_target` = (select `sys_user`.`dept_id` from `sys_user` where (`sys_user`.`user_id` = '4')))) or ((`sys_auth`.`auth_target_type` = 'user') and (`sys_auth`.`auth_target` = '4')) or ((`sys_auth`.`auth_target_type` = 'role') and (`sys_auth`.`auth_target` = (select `sys_users_roles`.`role_id` from `sys_users_roles` where (`sys_users_roles`.`user_id` = '4')))))) group by `sys_auth`.`auth_source`,`sys_auth`.`auth_source_type`;

-- ----------------------------
-- Function structure for copy_auth
-- ----------------------------
DROP FUNCTION IF EXISTS `copy_auth`;
delimiter ;;
CREATE FUNCTION `copy_auth`(authSource varchar(255),authSourceType varchar(255),authUser varchar(255))
 RETURNS varchar(255) CHARSET utf8
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
        if(sys_auth_detail.privilege_type=1,1,sys_auth_detail.privilege_value),
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
DROP FUNCTION IF EXISTS `delete_auth_source`;
delimiter ;;
CREATE FUNCTION `delete_auth_source`(authSource varchar(255),authSourceType varchar(255))
 RETURNS varchar(255) CHARSET utf8
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

-- ----------------------------
-- Function structure for delete_auth_target
-- ----------------------------
DROP FUNCTION IF EXISTS `delete_auth_target`;
delimiter ;;
CREATE FUNCTION `delete_auth_target`(authTarget varchar(255),authTargetType varchar(255))
 RETURNS varchar(255) CHARSET utf8
  READS SQL DATA
BEGIN

delete from sys_auth_detail where auth_id in (
select id from  sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType
);

delete from sys_auth where sys_auth.auth_target=authTarget and sys_auth.auth_target_type=authTargetType;

RETURN 'success';

END
;;
delimiter ;

-- ----------------------------
-- Function structure for get_auths
-- ----------------------------
DROP FUNCTION IF EXISTS `get_auths`;
delimiter ;;
CREATE FUNCTION `get_auths`(authSource varchar(255),modelType varchar(255),userId varchar(255))
 RETURNS longtext CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

SELECT
	group_concat( sys_auth_detail.privilege_extend) into oTemp
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
					AND sys_auth.auth_target = ( SELECT dept_id FROM sys_user WHERE user_id = userId )
				)
				OR (
					sys_auth.auth_target_type = 'user'
					AND sys_auth.auth_target = userId
				)
				OR (
					sys_auth.auth_target_type = 'role'
					AND sys_auth.auth_target = ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
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
-- Function structure for GET_PANEL_GROUP_WITH_CHILDREN
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_PANEL_GROUP_WITH_CHILDREN`;
delimiter ;;
CREATE FUNCTION `GET_PANEL_GROUP_WITH_CHILDREN`(parentId varchar(8000))
 RETURNS varchar(8000) CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTemp VARCHAR(8000);

DECLARE oTempChild VARCHAR(8000);

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR);

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
DROP FUNCTION IF EXISTS `GET_PANEL_TEMPLATE_WITH_CHILDREN`;
delimiter ;;
CREATE FUNCTION `GET_PANEL_TEMPLATE_WITH_CHILDREN`(parentId varchar(8000))
 RETURNS varchar(8000) CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTemp VARCHAR(8000);

DECLARE oTempChild VARCHAR(8000);

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR);

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
DROP FUNCTION IF EXISTS `GET_V_AUTH_MODEL_ID_P_USE`;
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_ID_P_USE`(userId longtext,modelType varchar(255))
 RETURNS longtext CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTempIds longtext;
SELECT
	GROUP_CONCAT(mainInfo.id) into oTempIds
FROM
	(
	SELECT
		v_auth_model.*,
		authTemp.used_auth
	FROM
		v_auth_model
		LEFT JOIN (
		SELECT
			sys_auth.auth_source_type,
			sys_auth.auth_source,
		IF
			( sum( privilege_value )> 0, 1, 0 ) used_auth
		FROM
			sys_auth
			LEFT JOIN sys_auth_detail ON sys_auth.id = sys_auth_detail.auth_id
		WHERE
			sys_auth_detail.privilege_type = 1
			AND (
				(
					sys_auth.auth_target_type = 'dept'
					AND sys_auth.auth_target = ( SELECT dept_id FROM sys_user WHERE user_id = userId )
				)
				OR (
					sys_auth.auth_target_type = 'user'
					AND sys_auth.auth_target = userId
				)
				OR (
					sys_auth.auth_target_type = 'role'
					AND sys_auth.auth_target = ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
				)
			)
		GROUP BY
			sys_auth.auth_source_type,
			sys_auth.auth_source
		) authTemp ON v_auth_model.model_type = authTemp.auth_source_type
		AND v_auth_model.id = authTemp.auth_source
	WHERE authTemp.auth_source_type=modelType and
		(
			v_auth_model.node_type = 'spine'
			OR (
				v_auth_model.node_type = 'leaf'
				AND authTemp.used_auth = 1
			))) mainInfo
	LEFT JOIN (
	SELECT
		count( 1 ) AS `children_count`,
		v_auth_model.pid
	FROM
		v_auth_model
		LEFT JOIN (
		SELECT
			sys_auth.auth_source_type,
			sys_auth.auth_source,
		IF
			( sum( privilege_value )> 0, 1, 0 ) used_auth
		FROM
			sys_auth
			LEFT JOIN sys_auth_detail ON sys_auth.id = sys_auth_detail.auth_id
		WHERE
			sys_auth_detail.privilege_type = 1
			AND (
				(
					sys_auth.auth_target_type = 'dept'
					AND sys_auth.auth_target = ( SELECT dept_id FROM sys_user WHERE user_id = userId )
				)
				OR (
					sys_auth.auth_target_type = 'user'
					AND sys_auth.auth_target = userId
				)
				OR (
					sys_auth.auth_target_type = 'role'
					AND sys_auth.auth_target = ( SELECT role_id FROM sys_users_roles WHERE user_id = userId )
				)
			)
		GROUP BY
			sys_auth.auth_source_type,
			sys_auth.auth_source
		) authTemp ON v_auth_model.model_type = authTemp.auth_source_type
		AND v_auth_model.id = authTemp.auth_source
	WHERE
	  authTemp.auth_source_type=modelType and
		(
			v_auth_model.node_type = 'spine'
			OR (
				v_auth_model.node_type = 'leaf'
				AND authTemp.used_auth = 1
			))
	GROUP BY
	v_auth_model.pid
	) countTemp ON mainInfo.id = countTemp.pid
	where
	 (countTemp.children_count>0 or mainInfo.used_auth=1);
RETURN oTempIds;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_WITH_CHILDREN
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_V_AUTH_MODEL_WITH_CHILDREN`;
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_WITH_CHILDREN`(parentId longtext,modelType varchar(255))
 RETURNS longtext CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

DECLARE oTempChild longtext;

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR);

WHILE oTempChild IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempChild);

SELECT GROUP_CONCAT(id) INTO oTempChild FROM V_AUTH_MODEL WHERE FIND_IN_SET(pid,oTempChild) > 0 and V_AUTH_MODEL.model_type=modelType;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_WITH_PARENT
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_V_AUTH_MODEL_WITH_PARENT`;
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_WITH_PARENT`(childrenId longtext,modelType varchar(255))
 RETURNS longtext CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

DECLARE oTempParent longtext;

SET oTemp = '';

SET oTempParent = CAST(childrenId AS CHAR);

WHILE oTempParent IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempParent);

SELECT GROUP_CONCAT(pid) INTO oTempParent FROM V_AUTH_MODEL WHERE FIND_IN_SET(id,oTempParent) > 0 and V_AUTH_MODEL.model_type=modelType;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;



DROP TRIGGER if exists`new_auth_link`;
DROP TRIGGER if exists`delete_auth_link`;
CREATE TRIGGER `new_auth_link` AFTER INSERT ON `datasource` FOR EACH ROW select copy_auth(NEW.id,'link',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_link` AFTER DELETE ON `datasource` FOR EACH ROW select delete_auth_source(OLD.id,'link') into @ee;

DROP TRIGGER if exists`new_auth_dataset_group`;
DROP TRIGGER if exists`delete_auth_dataset_group`;
DROP TRIGGER if exists`new_auth_dataset_table`;
DROP TRIGGER if exists`delete_auth_dataset_table`;
CREATE TRIGGER `new_auth_dataset_group` AFTER INSERT ON `dataset_group` FOR EACH ROW select copy_auth(NEW.id,'dataset',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_dataset_group` AFTER DELETE ON `dataset_group` FOR EACH ROW select delete_auth_source(OLD.id,'dataset') into @ee;
CREATE TRIGGER `new_auth_dataset_table` AFTER INSERT ON `dataset_table` FOR EACH ROW select copy_auth(NEW.id,'dataset',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_dataset_table` AFTER DELETE ON `dataset_table` FOR EACH ROW select delete_auth_source(OLD.id,'dataset') into @ee;


DROP TRIGGER if exists `new_auth_chart_group`;
DROP TRIGGER if exists`delete_auth_chart_group`;
DROP TRIGGER if exists`new_auth_chart_view`;
DROP TRIGGER if exists`delete_auth_chart_view`;
CREATE TRIGGER `new_auth_chart_group` AFTER INSERT ON `chart_group` FOR EACH ROW select copy_auth(NEW.id,'chart',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_chart_group` AFTER DELETE ON `chart_group` FOR EACH ROW select delete_auth_source(OLD.id,'chart') into @ee;
CREATE TRIGGER `new_auth_chart_view` AFTER INSERT ON `chart_view` FOR EACH ROW select copy_auth(NEW.id,'chart',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_chart_view` AFTER DELETE ON `chart_view` FOR EACH ROW select delete_auth_source(OLD.id,'chart') into @ee;

DROP TRIGGER if exists`new_auth_panel`;
DROP TRIGGER if exists`delete_auth_panel`;
CREATE TRIGGER `new_auth_panel` AFTER INSERT ON `panel_group` FOR EACH ROW select copy_auth(NEW.id,'panel',NEW.create_by) into @ee;
CREATE TRIGGER `delete_auth_panel` AFTER DELETE ON `panel_group` FOR EACH ROW select delete_auth_source(OLD.id,'panel') into @ee;


DROP TRIGGER if exists`delete_auth_user_target`;
DROP TRIGGER if exists`delete_auth_role_target`;
DROP TRIGGER if exists`delete_auth_dept_target`;
CREATE TRIGGER `delete_auth_user_target` AFTER DELETE ON `sys_user` FOR EACH ROW select delete_auth_target(OLD.user_id,'user') into @ee;
CREATE TRIGGER `delete_auth_role_target` AFTER DELETE ON `sys_role` FOR EACH ROW select delete_auth_target(OLD.role_id,'role') into @ee;
CREATE TRIGGER `delete_auth_dept_target` AFTER DELETE ON `sys_dept` FOR EACH ROW select delete_auth_target(OLD.dept_id,'dept') into @ee;
