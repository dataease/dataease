CREATE TABLE `dataset_row_permissions` (
   `id`         varchar(64) NOT NULL COMMENT 'File ID',
   `auth_target_type`  varchar(255) DEFAULT NULL COMMENT '权限类型：组织/角色/用户',
   `auth_target_id`    bigint(20) DEFAULT NULL COMMENT '权限对象ID',
   `dataset_id`    varchar(64) DEFAULT NULL COMMENT '数据集ID',
   `dataset_field_id`    varchar(64) DEFAULT NULL COMMENT '数据集字段ID',
   `logic`    varchar(64) DEFAULT NULL COMMENT '与/或',
   `filter`  longtext DEFAULT NULL COMMENT '数值',
   `update_time` bigint(13) NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;


ALTER TABLE `panel_link` ADD COLUMN `user_id` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `panel_link_mapping` ADD COLUMN `user_id` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `panel_link`CHANGE COLUMN `user_id` `user_id` BIGINT(20) NOT NULL ,DROP PRIMARY KEY;

ALTER TABLE `panel_group`
ADD COLUMN `mobile_layout` tinyint(1) NULL DEFAULT 0 COMMENT '启用移动端布局' AFTER `remark`;


-- ----------------------------
-- Function structure for GET_PANEL_WITH_PRIVILEGE_AND_MOBILE
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_PANEL_WITH_PRIVILEGE_AND_MOBILE`;
delimiter ;;
CREATE FUNCTION `GET_PANEL_WITH_PRIVILEGE_AND_MOBILE`(userId longtext,modelType varchar(255),privilegeType varchar(255))
 RETURNS longtext CHARSET utf8
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
			and sys_auth.auth_source in (select id from panel_group where mobile_layout='1')
		GROUP BY
			sys_auth.auth_source_type,
			sys_auth.auth_source
			having  (sum( sys_auth_detail.privilege_value )> 0 or 1 = ( SELECT is_admin FROM sys_user WHERE user_id = userId ))) temp;
RETURN oTempLeafIds;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for GET_V_AUTH_MODEL_ID_P_USE_MOBILE
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_V_AUTH_MODEL_ID_P_USE_MOBILE`;
delimiter ;;
CREATE FUNCTION `GET_V_AUTH_MODEL_ID_P_USE_MOBILE`(userId longtext,modelType varchar(255))
 RETURNS longtext CHARSET utf8
  READS SQL DATA
BEGIN

DECLARE oTempLeafIds longtext;
DECLARE oTempAllIds longtext;

select GET_PANEL_WITH_PRIVILEGE_AND_MOBILE(userId,modelType,1) into oTempLeafIds;

select GROUP_CONCAT(id) into oTempAllIds from (select GET_V_AUTH_MODEL_WITH_PARENT ( oTempLeafIds ,modelType) cids) t, v_auth_model where v_auth_model.model_type=modelType and FIND_IN_SET(v_auth_model.id,cids) order by id asc;

RETURN oTempAllIds;
END
;;
delimiter ;



