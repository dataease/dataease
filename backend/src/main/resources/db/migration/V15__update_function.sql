DROP FUNCTION IF EXISTS `GET_CHART_VIEW_COPY_NAME`;
delimiter ;;
CREATE FUNCTION `GET_CHART_VIEW_COPY_NAME`(chartId varchar(255)) RETURNS varchar(255) CHARSET utf8
    READS SQL DATA
BEGIN

DECLARE chartName varchar(255);

DECLARE pid varchar(255);

DECLARE regexpInfo varchar(255);

DECLARE chartNameCount INTEGER;

select `name` ,`scene_id` into chartName, pid from chart_view where id =chartId;
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

DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS select `sys_user`.`user_id` AS `id`,`sys_user`.`username` AS `name`,`sys_user`.`username` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'user' AS `model_type`,'user' AS `model_inner_type`,'target' AS `auth_type`,`sys_user`.`create_by` AS `create_by` from `sys_user` union all select `sys_role`.`role_id` AS `id`,`sys_role`.`name` AS `name`,`sys_role`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'role' AS `model_type`,'role' AS `model_inner_type`,'target' AS `auth_type`,`sys_role`.`create_by` AS `create_by` from `sys_role` union all select `sys_dept`.`dept_id` AS `id`,`sys_dept`.`name` AS `name`,`sys_dept`.`name` AS `lable`,cast(`sys_dept`.`pid` as char charset utf8mb4) AS `pid`,if((`sys_dept`.`sub_count` = 0),'leaf','spine') AS `node_type`,'dept' AS `model_type`,'dept' AS `model_inner_type`,'target' AS `auth_type`,`sys_dept`.`create_by` AS `create_by` from `sys_dept` union all select `datasource`.`id` AS `id`,`datasource`.`name` AS `NAME`,`datasource`.`name` AS `label`,'0' AS `pid`,'leaf' AS `node_type`,'link' AS `model_type`,`datasource`.`type` AS `model_inner_type`,'source' AS `auth_type`,`datasource`.`create_by` AS `create_by` from `datasource` union all select `dataset_group`.`id` AS `id`,`dataset_group`.`name` AS `NAME`,`dataset_group`.`name` AS `lable`,if(isnull(`dataset_group`.`pid`),'0',`dataset_group`.`pid`) AS `pid`,'spine' AS `node_type`,'dataset' AS `model_type`,`dataset_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_group`.`create_by` AS `create_by` from `dataset_group` union all select `dataset_table`.`id` AS `id`,`dataset_table`.`name` AS `NAME`,`dataset_table`.`name` AS `lable`,`dataset_table`.`scene_id` AS `pid`,'leaf' AS `node_type`,'dataset' AS `model_type`,`dataset_table`.`type` AS `model_inner_type`,'source' AS `auth_type`,`dataset_table`.`create_by` AS `create_by` from `dataset_table` union all select `chart_group`.`id` AS `id`,`chart_group`.`name` AS `name`,`chart_group`.`name` AS `label`,if(isnull(`chart_group`.`pid`),'0',`chart_group`.`pid`) AS `pid`,'spine' AS `node_type`,'chart' AS `model_type`,`chart_group`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_group`.`create_by` AS `create_by` from `chart_group` union all select `chart_view`.`id` AS `id`,`chart_view`.`name` AS `name`,`chart_view`.`name` AS `label`,`chart_view`.`scene_id` AS `pid`,'leaf' AS `node_type`,'chart' AS `model_type`,`chart_view`.`type` AS `model_inner_type`,'source' AS `auth_type`,`chart_view`.`create_by` AS `create_by` from `chart_view` union all select `panel_group`.`id` AS `id`,`panel_group`.`name` AS `NAME`,`panel_group`.`name` AS `label`,(case `panel_group`.`id` when 'panel_list' then '0' when 'default_panel' then '0' else `panel_group`.`pid` end) AS `pid`,if((`panel_group`.`node_type` = 'folder'),'spine','leaf') AS `node_type`,'panel' AS `model_type`,`panel_group`.`panel_type` AS `model_inner_type`,'source' AS `auth_type`,`panel_group`.`create_by` AS `create_by` from `panel_group` union all select `sys_menu`.`menu_id` AS `menu_id`,`sys_menu`.`title` AS `name`,`sys_menu`.`title` AS `label`,`sys_menu`.`pid` AS `pid`,if((`sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`sys_menu`.`create_by` AS `create_by` from `sys_menu` where (((`sys_menu`.`permission` is not null) or (`sys_menu`.`pid` = 0)) and ((`sys_menu`.`name` <> 'panel') or isnull(`sys_menu`.`name`))) union all select `plugin_sys_menu`.`menu_id` AS `menu_id`,`plugin_sys_menu`.`title` AS `name`,`plugin_sys_menu`.`title` AS `label`,`plugin_sys_menu`.`pid` AS `pid`,if((`plugin_sys_menu`.`sub_count` > 0),'spine','leaf') AS `node_type`,'menu' AS `model_type`,(case `plugin_sys_menu`.`type` when 0 then 'folder' when 1 then 'menu' when 2 then 'button' end) AS `model_inner_type`,'source' AS `auth_type`,`plugin_sys_menu`.`create_by` AS `create_by` from `plugin_sys_menu` where ((`plugin_sys_menu`.`permission` is not null) or (`plugin_sys_menu`.`pid` = 0));
