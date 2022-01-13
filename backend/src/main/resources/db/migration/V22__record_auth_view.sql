
DROP VIEW
IF
	EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS SELECT
`sys_user`.`user_id` AS `id`,
`sys_user`.`username` AS `name`,
`sys_user`.`username` AS `label`,
'0' AS `pid`,
'leaf' AS `node_type`,
'user' AS `model_type`,
'user' AS `model_inner_type`,
'target' AS `auth_type`,
`sys_user`.`create_by` AS `create_by`
FROM
	`sys_user`
WHERE
	( `sys_user`.`is_admin` <> 1 )
	 UNION ALL
SELECT
	`sys_role`.`role_id` AS `id`,
	`sys_role`.`name` AS `name`,
	`sys_role`.`name` AS `label`,
	'0' AS `pid`,
	'leaf' AS `node_type`,
	'role' AS `model_type`,
	'role' AS `model_inner_type`,
	'target' AS `auth_type`,
	`sys_role`.`create_by` AS `create_by`
FROM
	`sys_role` UNION ALL
SELECT
	`sys_dept`.`dept_id` AS `id`,
	`sys_dept`.`name` AS `name`,
	`sys_dept`.`name` AS `lable`,
	cast( `sys_dept`.`pid` AS CHAR  CHARACTER set utf8mb4) COLLATE utf8mb4_general_ci  AS `pid`,
IF
	(( `sys_dept`.`sub_count` = 0 ), 'leaf', 'spine' ) AS `node_type`,
	'dept' AS `model_type`,
	'dept' AS `model_inner_type`,
	'target' AS `auth_type`,
	`sys_dept`.`create_by` AS `create_by`
FROM
	`sys_dept` UNION ALL
SELECT
	`datasource`.`id` AS `id`,
	`datasource`.`name` AS `NAME`,
	`datasource`.`name` AS `label`,
	'0' AS `pid`,
	'leaf' AS `node_type`,
	'link' AS `model_type`,
	`datasource`.`type` AS `model_inner_type`,
	'source' AS `auth_type`,
	`datasource`.`create_by` AS `create_by`
FROM
	`datasource` UNION ALL
SELECT
	`dataset_group`.`id` AS `id`,
	`dataset_group`.`name` AS `NAME`,
	`dataset_group`.`name` AS `lable`,
IF
	( isnull( `dataset_group`.`pid` ), '0', `dataset_group`.`pid` ) AS `pid`,
	'spine' AS `node_type`,
	'dataset' AS `model_type`,
	`dataset_group`.`type` AS `model_inner_type`,
	'source' AS `auth_type`,
	`dataset_group`.`create_by` AS `create_by`
FROM
	`dataset_group` UNION ALL
SELECT
	`dataset_table`.`id` AS `id`,
	`dataset_table`.`name` AS `NAME`,
	`dataset_table`.`name` AS `lable`,
	`dataset_table`.`scene_id` AS `pid`,
	'leaf' AS `node_type`,
	'dataset' AS `model_type`,
	`dataset_table`.`type` AS `model_inner_type`,
	'source' AS `auth_type`,
	`dataset_table`.`create_by` AS `create_by`
FROM
	`dataset_table` UNION ALL
SELECT
	`chart_group`.`id` AS `id`,
	`chart_group`.`name` AS `name`,
	`chart_group`.`name` AS `label`,
IF
	( isnull( `chart_group`.`pid` ), '0', `chart_group`.`pid` ) AS `pid`,
	'spine' AS `node_type`,
	'chart' AS `model_type`,
	`chart_group`.`type` AS `model_inner_type`,
	'source' AS `auth_type`,
	`chart_group`.`create_by` AS `create_by`
FROM
	`chart_group` UNION ALL
SELECT
	`chart_view`.`id` AS `id`,
	`chart_view`.`name` AS `name`,
	`chart_view`.`name` AS `label`,
	`chart_view`.`scene_id` AS `pid`,
	'leaf' AS `node_type`,
	'chart' AS `model_type`,
	`chart_view`.`type` AS `model_inner_type`,
	'source' AS `auth_type`,
	`chart_view`.`create_by` AS `create_by`
FROM
	`chart_view` UNION ALL
SELECT
	`panel_group`.`id` AS `id`,
	`panel_group`.`name` AS `NAME`,
	`panel_group`.`name` AS `label`,(
	CASE
			`panel_group`.`id`
			WHEN 'panel_list' THEN
			'0'
			WHEN 'default_panel' THEN
			'0' ELSE `panel_group`.`pid`
		END
		) AS `pid`,
	IF
		(( `panel_group`.`node_type` = 'folder' ), 'spine', 'leaf' ) AS `node_type`,
		'panel' AS `model_type`,
		`panel_group`.`panel_type` AS `model_inner_type`,
		'source' AS `auth_type`,
		`panel_group`.`create_by` AS `create_by`
	FROM
		`panel_group` UNION ALL
	SELECT
		`sys_menu`.`menu_id` AS `menu_id`,
		`sys_menu`.`title` AS `name`,
		`sys_menu`.`title` AS `label`,
		`sys_menu`.`pid` AS `pid`,
	IF
		(( `sys_menu`.`sub_count` > 0 ), 'spine', 'leaf' ) AS `node_type`,
		'menu' AS `model_type`,(
		CASE
				`sys_menu`.`type`
				WHEN 0 THEN
				'folder'
				WHEN 1 THEN
				'menu'
				WHEN 2 THEN
				'button'
			END
			) AS `model_inner_type`,
			'source' AS `auth_type`,
			`sys_menu`.`create_by` AS `create_by`
		FROM
			`sys_menu`
		WHERE
			(sys_menu.i_frame !=1 or  sys_menu.i_frame is null) UNION ALL
		SELECT
			`plugin_sys_menu`.`menu_id` AS `menu_id`,
			`plugin_sys_menu`.`title` AS `name`,
			`plugin_sys_menu`.`title` AS `label`,
			`plugin_sys_menu`.`pid` AS `pid`,
		IF
			(( `plugin_sys_menu`.`sub_count` > 0 ), 'spine', 'leaf' ) AS `node_type`,
			'menu' AS `model_type`,(
			CASE
					`plugin_sys_menu`.`type`
					WHEN 0 THEN
					'folder'
					WHEN 1 THEN
					'menu'
					WHEN 2 THEN
					'button'
				END
				) AS `model_inner_type`,
				'source' AS `auth_type`,
				`plugin_sys_menu`.`create_by` AS `create_by`
			FROM
				`plugin_sys_menu`
			WHERE
				(plugin_sys_menu.i_frame !=1 or  plugin_sys_menu.i_frame is null);
