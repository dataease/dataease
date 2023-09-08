ALTER TABLE `chart_view` ADD COLUMN `x_axis_ext` LONGTEXT COMMENT 'table-row' AFTER `x_axis`;
UPDATE `chart_view` SET `x_axis_ext` = '[]';


INSERT INTO `chart_group` (`id`, `name`, `pid`, `level`, `type`, `create_by`, `create_time`) VALUES ('0', 'i18n_public_chart', 'public_chart', -1, 'history', 'admin', NULL);

ALTER TABLE `chart_view`
MODIFY COLUMN `scene_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id' AFTER `title`,
ADD COLUMN `chart_type` varchar(255) NULL DEFAULT 'private' COMMENT '视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板' AFTER `style_priority`;

delete from sys_auth_detail where auth_id in(select id from sys_auth where auth_source_type = 'chart');
delete from sys_auth where auth_source_type = 'chart';

DROP TRIGGER `new_auth_chart_view`;
DROP TRIGGER `delete_auth_chart_view`;

delete from sys_menu where menu_id ='10';

DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS SELECT
`sys_user`.`user_id` AS `id`,
`sys_user`.`username` AS `name`,
`sys_user`.`username` AS `label`,
'0' AS `pid`,
'leaf' AS `node_type`,
'user' AS `model_type`,
'user' AS `model_inner_type`,
'target' AS `auth_type`,
`sys_user`.`create_by` AS `create_by`,
0 AS `level`,
0 AS `mode`,
'0' AS `data_source_id`
FROM
	`sys_user`
WHERE
	( `sys_user`.`is_admin` <> 1 ) UNION ALL
SELECT
	`sys_role`.`role_id` AS `id`,
	`sys_role`.`name` AS `name`,
	`sys_role`.`name` AS `label`,
	'0' AS `pid`,
	'leaf' AS `node_type`,
	'role' AS `model_type`,
	'role' AS `model_inner_type`,
	'target' AS `auth_type`,
	`sys_role`.`create_by` AS `create_by`,
	0 AS `level`,
	0 AS `mode`,
	'0' AS `data_source_id`
FROM
	`sys_role` UNION ALL
SELECT
	`sys_dept`.`dept_id` AS `id`,
	`sys_dept`.`name` AS `name`,
	`sys_dept`.`name` AS `lable`,(
		cast( `sys_dept`.`pid` AS CHAR charset utf8mb4 ) COLLATE utf8mb4_general_ci
	) AS `pid`,
IF
	(( `sys_dept`.`sub_count` = 0 ), 'leaf', 'spine' ) AS `node_type`,
	'dept' AS `model_type`,
	'dept' AS `model_inner_type`,
	'target' AS `auth_type`,
	`sys_dept`.`create_by` AS `create_by`,
	0 AS `level`,
	0 AS `mode`,
	'0' AS `data_source_id`
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
	`datasource`.`create_by` AS `create_by`,
	0 AS `level`,
	0 AS `mode`,
	'0' AS `data_source_id`
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
	`dataset_group`.`create_by` AS `create_by`,
	`dataset_group`.`level` AS `level`,
	0 AS `mode`,
	'0' AS `data_source_id`
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
	`dataset_table`.`create_by` AS `create_by`,
	0 AS `level`,
	`dataset_table`.`mode` AS `mode`,
	`dataset_table`.`data_source_id` AS `data_source_id`
FROM
	`dataset_table` UNION ALL
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
		`panel_group`.`create_by` AS `create_by`,
		0 AS `level`,
		0 AS `mode`,
		'0' AS `data_source_id`
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
			`sys_menu`.`create_by` AS `create_by`,
			0 AS `level`,
			0 AS `mode`,
			'0' AS `data_source_id`
		FROM
			`sys_menu`
		WHERE
			((
					`sys_menu`.`i_frame` <> 1
					)
			OR isnull( `sys_menu`.`i_frame` )) UNION ALL
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
				`plugin_sys_menu`.`create_by` AS `create_by`,
				0 AS `level`,
				0 AS `mode`,
				'0' AS `data_source_id`
			FROM
				`plugin_sys_menu`
			WHERE
				((
						`plugin_sys_menu`.`i_frame` <> 1
					)
	OR isnull( `plugin_sys_menu`.`i_frame` ));



delete from panel_view where id in (
'5dafcf9b-8e0a-11ec-8e4b-0242ac130002',
'5dafd1d1-8e0a-11ec-8e4b-0242ac130002',
'5dafd224-8e0a-11ec-8e4b-0242ac130002',
'5dafd26e-8e0a-11ec-8e4b-0242ac130002',
'5dafd2ac-8e0a-11ec-8e4b-0242ac130002',
'5dafd2f1-8e0a-11ec-8e4b-0242ac130002',
'5dafd32e-8e0a-11ec-8e4b-0242ac130002',
'5dafd363-8e0a-11ec-8e4b-0242ac130002',
'6c200051-8e0a-11ec-8e4b-0242ac130002',
'6c2002c2-8e0a-11ec-8e4b-0242ac130002',
'6c20030f-8e0a-11ec-8e4b-0242ac130002',
'6c2003ca-8e0a-11ec-8e4b-0242ac130002',
'6c20040d-8e0a-11ec-8e4b-0242ac130002',
'6c200442-8e0a-11ec-8e4b-0242ac130002',
'6c200478-8e0a-11ec-8e4b-0242ac130002',
'6c2004a9-8e0a-11ec-8e4b-0242ac130002',
'6c2004e4-8e0a-11ec-8e4b-0242ac130002',
'6c200530-8e0a-11ec-8e4b-0242ac130002',
'6c200568-8e0a-11ec-8e4b-0242ac130002',
'6c200598-8e0a-11ec-8e4b-0242ac130002',
'7611e439-8e0a-11ec-8e4b-0242ac130002',
'7611e678-8e0a-11ec-8e4b-0242ac130002',
'7611e6c4-8e0a-11ec-8e4b-0242ac130002',
'7611e700-8e0a-11ec-8e4b-0242ac130002',
'7611e7d0-8e0a-11ec-8e4b-0242ac130002',
'7611e80a-8e0a-11ec-8e4b-0242ac130002',
'7611e844-8e0a-11ec-8e4b-0242ac130002',
'7611e87d-8e0a-11ec-8e4b-0242ac130002',
'7611e8b5-8e0a-11ec-8e4b-0242ac130002',
'7611e8ed-8e0a-11ec-8e4b-0242ac130002',
'7611e923-8e0a-11ec-8e4b-0242ac130002',
'7611e957-8e0a-11ec-8e4b-0242ac130002',
'7611e98f-8e0a-11ec-8e4b-0242ac130002',
'7611e9c9-8e0a-11ec-8e4b-0242ac130002',
'7611ea05-8e0a-11ec-8e4b-0242ac130002',
'7611ea46-8e0a-11ec-8e4b-0242ac130002',
'805b082d-8e0a-11ec-8e4b-0242ac130002',
'805b0a85-8e0a-11ec-8e4b-0242ac130002',
'805b0abb-8e0a-11ec-8e4b-0242ac130002',
'805b0ae0-8e0a-11ec-8e4b-0242ac130002',
'805b0b0f-8e0a-11ec-8e4b-0242ac130002',
'805b0b2d-8e0a-11ec-8e4b-0242ac130002',
'805b0bdb-8e0a-11ec-8e4b-0242ac130002',
'805b0cc1-8e0a-11ec-8e4b-0242ac130002',
'805b0cf8-8e0a-11ec-8e4b-0242ac130002',
'805b0d31-8e0a-11ec-8e4b-0242ac130002',
'7583dc0d-058d-11ec-a2b0-0242ac130003',
'7583deb5-058d-11ec-a2b0-0242ac130003',
'7583df2c-058d-11ec-a2b0-0242ac130003',
'7583df74-058d-11ec-a2b0-0242ac130003',
'7583dfbe-058d-11ec-a2b0-0242ac130003',
'7583e00f-058d-11ec-a2b0-0242ac130003',
'7583e053-058d-11ec-a2b0-0242ac130003',
'7583e091-058d-11ec-a2b0-0242ac130003'
);

INSERT INTO `panel_view` VALUES ('5dafcf9b-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', '84b444e1-0088-44f9-acdc-cc39018413bc', NULL, NULL, NULL, NULL, NULL), ('5dafd1d1-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'c68db172-2df2-4aa2-aad6-077cf1684e14', NULL, NULL, NULL, NULL, NULL), ('5dafd224-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'f8d62b2b-b99a-4b6c-8378-d7c2ec4ea766', NULL, NULL, NULL, NULL, NULL), ('5dafd26e-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'c4943403-4960-4ad8-a9c5-12c46c538c34', NULL, NULL, NULL, NULL, NULL), ('5dafd2ac-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'f257452d-6fc1-4499-bdce-bd10b3e1c520', NULL, NULL, NULL, NULL, NULL), ('5dafd2f1-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', '8271c4e4-43ab-48c6-b7b4-67ccaba3f80b', NULL, NULL, NULL, NULL, NULL), ('5dafd32e-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'a0058881-b29f-4b5c-911f-7f1480b07eb0', NULL, NULL, NULL, NULL, NULL), ('5dafd363-8e0a-11ec-8e4b-0242ac130002', '117f679e-8355-4645-a692-47e2009cbc0d', 'c36cd358-0501-4f83-a323-f754485d00b1', NULL, NULL, NULL, NULL, NULL), ('6c200051-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '95f8e3a2-62a5-48a7-a719-fcf53746da8d', NULL, NULL, NULL, NULL, NULL), ('6c2002c2-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '8a26a936-89bf-45a8-b1ce-d5ef1719465d', NULL, NULL, NULL, NULL, NULL), ('6c20030f-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '8d1c3f30-0639-452e-9883-164f37353324', NULL, NULL, NULL, NULL, NULL), ('6c2003ca-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '175b25df-1939-4582-a9c5-d9e8ed3ea2b1', NULL, NULL, NULL, NULL, NULL), ('6c20040d-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', 'c3da496f-073c-413a-bebd-e7f1a4a00ba7', NULL, NULL, NULL, NULL, NULL), ('6c200442-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', 'da18eecd-feff-4140-a291-cce4abf1afaa', NULL, NULL, NULL, NULL, NULL), ('6c200478-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '3f201733-bbb3-485e-a1d6-0fe4f00b5304', NULL, NULL, NULL, NULL, NULL), ('6c2004a9-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '692d5bdc-aa70-4fce-b830-b8d6620539c6', NULL, NULL, NULL, NULL, NULL), ('6c2004e4-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', 'aff5be0c-f195-4fce-bd2b-b8d0e63764de', NULL, NULL, NULL, NULL, NULL), ('6c200530-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', 'cb66836d-a34c-40c6-87e7-0db0375ec19e', NULL, NULL, NULL, NULL, NULL), ('6c200568-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '1d06e2a0-d936-4192-b523-2eb1e8cebd51', NULL, NULL, NULL, NULL, NULL), ('6c200598-8e0a-11ec-8e4b-0242ac130002', 'c8d4c4b4-2293-417f-b76d-3632cc217bb1', '0de1d446-8300-4ab3-a4ef-4e8f8579cb2e', NULL, NULL, NULL, NULL, NULL), ('7611e439-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', 'ebac2821-d1a0-4f26-b5d9-cd5c60ac75ab', NULL, NULL, NULL, NULL, NULL), ('7611e678-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '5ad64afc-132c-40ea-8f69-2f8bfe6b31d4', NULL, NULL, NULL, NULL, NULL), ('7611e6c4-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '4242cbb0-fca4-4b27-b2a7-ca576a18815e', NULL, NULL, NULL, NULL, NULL), ('7611e700-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', 'c52b6d95-b404-4130-8635-5903cb8d0e84', NULL, NULL, NULL, NULL, NULL), ('7611e7d0-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '2e6f8b45-116d-46c4-a287-f3054e798556', NULL, NULL, NULL, NULL, NULL), ('7611e80a-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '504c0abd-7d31-4771-8ef9-a3494c7bb33c', NULL, NULL, NULL, NULL, NULL), ('7611e844-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '02136575-effb-4a0c-b5be-9886d20259b3', NULL, NULL, NULL, NULL, NULL), ('7611e87d-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '8412d80d-1830-4128-bc6a-019cf32afc7f', NULL, NULL, NULL, NULL, NULL), ('7611e8b5-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '0f9cd623-f319-4bb5-9751-7478abee3bd2', NULL, NULL, NULL, NULL, NULL), ('7611e8ed-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', 'a1d7bfef-f20c-4739-bfe4-cc55ed0b3fc8', NULL, NULL, NULL, NULL, NULL), ('7611e923-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '68fc74f2-1790-427a-ac22-49fb20edbe9a', NULL, NULL, NULL, NULL, NULL), ('7611e957-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '97400660-27a5-4502-a7cd-274190953a6c', NULL, NULL, NULL, NULL, NULL), ('7611e98f-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '07ece816-f983-493e-b25d-7bfb467d787d', NULL, NULL, NULL, NULL, NULL), ('7611e9c9-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', 'c124c0f3-3f1f-4635-bac7-f3e1f5503099', NULL, NULL, NULL, NULL, NULL), ('7611ea05-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '984059ca-3f9d-4ee4-9616-e409dd11991e', NULL, NULL, NULL, NULL, NULL), ('7611ea46-8e0a-11ec-8e4b-0242ac130002', 'd2bda4c3-3c25-40c6-bed3-994ffe2949df', '2de7c3d3-e642-4509-aff1-b2520ebfe85e', NULL, NULL, NULL, NULL, NULL), ('805b082d-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '3a5e4081-4cd5-427f-bd3a-ff7815efaf25', NULL, NULL, NULL, NULL, NULL), ('805b0a85-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '57760693-15db-4de9-9170-55ee7d1eb0eb', NULL, NULL, NULL, NULL, NULL), ('805b0abb-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '0d8bc9d7-b76b-4ec5-96e7-0df1c3426205', NULL, NULL, NULL, NULL, NULL), ('805b0ae0-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '03410ec1-1bd0-4afd-ac37-9306e00e328c', NULL, NULL, NULL, NULL, NULL), ('805b0b0f-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', 'eaa8947b-d9e7-4ca4-ba65-08965dfa620c', NULL, NULL, NULL, NULL, NULL), ('805b0b2d-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '93a58625-3730-4a07-99bd-75f174ff428d', NULL, NULL, NULL, NULL, NULL), ('805b0bdb-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '2f9bf4d5-b1d3-4cac-9df2-2c8827d65bbf', NULL, NULL, NULL, NULL, NULL), ('805b0cc1-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '1aad98e5-3f99-4c0a-aa75-ca9236de0f09', NULL, NULL, NULL, NULL, NULL), ('805b0cf8-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '5f694f25-b0fd-45f6-acbd-9dd338e196ce', NULL, NULL, NULL, NULL, NULL), ('805b0d31-8e0a-11ec-8e4b-0242ac130002', 'ceb6cd6c-531e-4a23-a467-caa5ef7218cc', '9ecb6827-f47f-4b19-b788-81a6b55940af', NULL, NULL, NULL, NULL, NULL);
update chart_view set chart_type ='public';
update chart_view set chart_type ='private'
where id in (
'84b444e1-0088-44f9-acdc-cc39018413bc',
'c68db172-2df2-4aa2-aad6-077cf1684e14',
'f8d62b2b-b99a-4b6c-8378-d7c2ec4ea766',
'c4943403-4960-4ad8-a9c5-12c46c538c34',
'f257452d-6fc1-4499-bdce-bd10b3e1c520',
'8271c4e4-43ab-48c6-b7b4-67ccaba3f80b',
'a0058881-b29f-4b5c-911f-7f1480b07eb0',
'c36cd358-0501-4f83-a323-f754485d00b1',
'95f8e3a2-62a5-48a7-a719-fcf53746da8d',
'8a26a936-89bf-45a8-b1ce-d5ef1719465d',
'8d1c3f30-0639-452e-9883-164f37353324',
'175b25df-1939-4582-a9c5-d9e8ed3ea2b1',
'c3da496f-073c-413a-bebd-e7f1a4a00ba7',
'da18eecd-feff-4140-a291-cce4abf1afaa',
'3f201733-bbb3-485e-a1d6-0fe4f00b5304',
'692d5bdc-aa70-4fce-b830-b8d6620539c6',
'aff5be0c-f195-4fce-bd2b-b8d0e63764de',
'cb66836d-a34c-40c6-87e7-0db0375ec19e',
'1d06e2a0-d936-4192-b523-2eb1e8cebd51',
'0de1d446-8300-4ab3-a4ef-4e8f8579cb2e',
'ebac2821-d1a0-4f26-b5d9-cd5c60ac75ab',
'5ad64afc-132c-40ea-8f69-2f8bfe6b31d4',
'4242cbb0-fca4-4b27-b2a7-ca576a18815e',
'c52b6d95-b404-4130-8635-5903cb8d0e84',
'2e6f8b45-116d-46c4-a287-f3054e798556',
'504c0abd-7d31-4771-8ef9-a3494c7bb33c',
'02136575-effb-4a0c-b5be-9886d20259b3',
'8412d80d-1830-4128-bc6a-019cf32afc7f',
'0f9cd623-f319-4bb5-9751-7478abee3bd2',
'a1d7bfef-f20c-4739-bfe4-cc55ed0b3fc8',
'68fc74f2-1790-427a-ac22-49fb20edbe9a',
'97400660-27a5-4502-a7cd-274190953a6c',
'07ece816-f983-493e-b25d-7bfb467d787d',
'c124c0f3-3f1f-4635-bac7-f3e1f5503099',
'984059ca-3f9d-4ee4-9616-e409dd11991e',
'2de7c3d3-e642-4509-aff1-b2520ebfe85e',
'3a5e4081-4cd5-427f-bd3a-ff7815efaf25',
'57760693-15db-4de9-9170-55ee7d1eb0eb',
'0d8bc9d7-b76b-4ec5-96e7-0df1c3426205',
'03410ec1-1bd0-4afd-ac37-9306e00e328c',
'eaa8947b-d9e7-4ca4-ba65-08965dfa620c',
'93a58625-3730-4a07-99bd-75f174ff428d',
'2f9bf4d5-b1d3-4cac-9df2-2c8827d65bbf',
'1aad98e5-3f99-4c0a-aa75-ca9236de0f09',
'5f694f25-b0fd-45f6-acbd-9dd338e196ce',
'9ecb6827-f47f-4b19-b788-81a6b55940af'
);

delete from chart_group where id in (
'2c200620-f2f3-4224-a41d-381fa061591b',
'3f551269-d985-4633-884d-d118704da2db',
'4de97755-5d5a-4fe0-9af0-27601f967787',
'5a8e8b0a-2f64-4d1b-aac1-d284b2b8436f',
'bc7542d8-2b7e-4909-81ff-3627b0227501',
'bfa7d87f-c76f-4406-9f19-0adccb7c568d'
);

ALTER TABLE `panel_link_mapping`
ADD COLUMN `uuid` varchar(8) NULL COMMENT 'uuid' AFTER `user_id`;

ALTER TABLE `chart_view`
ADD COLUMN `is_plugin` bit(1) NULL COMMENT '是否插件' AFTER `chart_type`;


INSERT INTO `my_plugin` VALUES (2, '视图默认插件', 'default', 0, 20000, 'view', '默认视图插件', '1.0-SNAPSHOT', NULL, 'fit2cloud-chenyw', 0, NULL, NULL, 'dataease-extensions-backend', NULL);


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_background_image
-- ----------------------------
DROP TABLE IF EXISTS `sys_background_image`;
CREATE TABLE `sys_background_image` (
  `id` varchar(64)  NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `classification` varchar(255) NOT NULL,
  `content` longtext,
  `remark` varchar(255) DEFAULT NULL,
  `sort` int(8) DEFAULT NULL,
  `upload_time` bigint(13) DEFAULT NULL,
  `base_url` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Records of sys_background_image
-- ----------------------------
BEGIN;
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_1', '边框1', '蓝色调', '', NULL, NULL, NULL, 'img/board', 'board/blue_1.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_2', '边框2', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_2.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_3', '边框3', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_3.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_4', '边框4', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_4.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_5', '边框5', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_5.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_6', '边框6', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_6.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_7', '边框7', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_7.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_8', '边框8', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_8.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('blue_9', '边框9', '蓝色调', NULL, NULL, NULL, NULL, 'img/board', 'board/blue_9.svg');
INSERT INTO `sys_background_image` (`id`, `name`, `classification`, `content`, `remark`, `sort`, `upload_time`, `base_url`, `url`) VALUES ('dark_1', '边框1', '深色调', NULL, NULL, NULL, NULL, 'img/board', 'board/dark_1.svg');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `panel_view`
ADD COLUMN `position` varchar(255) NULL DEFAULT 'panel' COMMENT '视图位置 panel 仪表板中，tab Tab页中' AFTER `update_time`;

ALTER TABLE `datasource`
    CHANGE COLUMN `status` `status` LONGTEXT NULL DEFAULT NULL COMMENT '状态' ;

ALTER TABLE `panel_view`
ADD COLUMN `copy_from_panel` varchar(255) NULL AFTER `position`,
ADD COLUMN `copy_from_view` varchar(255) NULL AFTER `copy_from_panel`,
ADD COLUMN `copy_from` varchar(255) NULL COMMENT '如果有复制 最近一次的复制来源id' AFTER `copy_from_view`,
ADD COLUMN `copy_id` varchar(255) NULL COMMENT '本次复制的执行ID' AFTER `copy_from`;

ALTER TABLE `panel_view_linkage`
ADD COLUMN `copy_from` varchar(255) NULL AFTER `ext2`,
ADD COLUMN `copy_id` varchar(255) NULL AFTER `copy_from`;

ALTER TABLE `panel_view_linkage_field`
ADD COLUMN `copy_from` varchar(255) NULL AFTER `update_time`,
ADD COLUMN `copy_id` varchar(255) NULL AFTER `copy_from`;

ALTER TABLE `panel_link_jump`
ADD COLUMN `copy_from` varchar(255) NULL AFTER `checked`,
ADD COLUMN `copy_id` varchar(255) NULL AFTER `copy_from`;

ALTER TABLE `panel_link_jump_info`
ADD COLUMN `copy_from` varchar(255) NULL AFTER `checked`,
ADD COLUMN `copy_id` varchar(255) NULL AFTER `copy_from`;

ALTER TABLE `panel_link_jump_target_view_info`
ADD COLUMN `copy_from` varchar(255) NULL AFTER `target_field_id`,
ADD COLUMN `copy_id` varchar(255) NULL AFTER `copy_from`;

DROP TABLE IF EXISTS `dataease_code_version`;
CREATE TABLE `dataease_code_version` (
  `installed_rank` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `installed_on` timestamp NULL DEFAULT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `dataease_version_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- Records of dataease_code_version
-- ----------------------------
BEGIN;
INSERT INTO `dataease_code_version` VALUES (0, 'init', NULL, 1);
COMMIT;

DELETE FROM `sys_menu` WHERE pid=34;
UPDATE `sys_menu` SET `sub_count` = '0' WHERE (`menu_id` = '34');

-- ----------------------------
-- View structure for v_history_chart_view
-- ----------------------------
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_history_chart_view` AS select `chart_group`.`id` AS `id`,`chart_group`.`id` AS `inner_id`,`chart_group`.`name` AS `NAME`,`chart_group`.`name` AS `label`,`chart_group`.`pid` AS `pid`,`chart_group`.`type` AS `model_inner_type`,'spine' AS `node_type`,'view' AS `model_type`,1 AS `mode` from `chart_group` union all select distinct `chart_view`.`id` AS `id`,`chart_view`.`id` AS `inner_id`,`chart_view`.`name` AS `NAME`,`chart_view`.`name` AS `label`,`chart_view`.`scene_id` AS `pid`,`chart_view`.`type` AS `model_inner_type`,'leaf' AS `node_type`,'view' AS `model_type`,1 AS `mode` from `chart_view` where (`chart_view`.`chart_type` = 'public');



-- ----------------------------
-- Function structure for GET_CHART_GROUP_WITH_CHILDREN
-- ----------------------------
DROP FUNCTION IF EXISTS `GET_CHART_GROUP_WITH_CHILDREN`;
delimiter ;;
CREATE FUNCTION `GET_CHART_GROUP_WITH_CHILDREN`(parentId varchar(8000))
 RETURNS LONGTEXT CHARSET utf8mb4 COLLATE utf8mb4_general_ci
  READS SQL DATA
BEGIN

DECLARE oTemp LONGTEXT;

DECLARE oTempChild LONGTEXT;

SET oTemp = '';

SET oTempChild = CAST(parentId AS CHAR);

WHILE oTempChild IS NOT NULL

DO

SET oTemp = CONCAT(oTemp,',',oTempChild);

SELECT GROUP_CONCAT(id) INTO oTempChild FROM v_history_chart_view WHERE FIND_IN_SET(pid,oTempChild) > 0;

END WHILE;

RETURN oTemp;

END
;;
delimiter ;

delete from chart_view where id in (
'ce33ad2c-3915-41cc-8d86-55405456ed05',
'd73eda10-68c9-40e2-b03a-a7cbe3e5b365',
'de923e69-df6b-4f61-9391-da2987f77b51'
);
