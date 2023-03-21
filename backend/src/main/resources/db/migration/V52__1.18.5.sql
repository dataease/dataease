UPDATE `my_plugin`
SET `version` = '1.18.5'
where `plugin_id` > 0
  and `version` = '1.18.4';


DROP FUNCTION IF EXISTS `get_auths`;
delimiter ;;
CREATE FUNCTION `get_auths`(authSource varchar(255),modelType varchar(255),userId varchar(255))
    RETURNS longtext CHARSET utf8mb4
  READS SQL DATA
BEGIN

DECLARE oTemp longtext;

DECLARE isAdmin int;

select sys_user.is_admin INTO isAdmin from sys_user where user_id =userId;
IF isAdmin = 1 THEN
		return 'ignore';
ELSE
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

END if;

END
;;
delimiter ;

INSERT INTO `system_parameter` (`param_key`, `param_value`, `type`, `sort`) VALUES ('loginlimit.multiLogin', '0', 'text', '3');

ALTER TABLE `panel_app_template`
    ADD COLUMN `link_jumps` longtext NULL AFTER `datasource_info`,
ADD COLUMN `link_jump_infos` longtext NULL AFTER `link_jumps`,
ADD COLUMN `linkages` longtext NULL AFTER `link_jump_infos`,
ADD COLUMN `linkage_fields` longtext NULL AFTER `linkages`;