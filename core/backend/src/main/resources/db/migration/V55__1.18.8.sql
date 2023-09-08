UPDATE `my_plugin`
SET `version` = '1.18.8'
where `plugin_id` > 0
  and `version` = '1.18.7';

ALTER TABLE `sys_task_email`
    ADD COLUMN `panel_format` INT(1) NOT NULL DEFAULT 0 COMMENT '仪表板格式0:jpeg,1:pdf' AFTER `panel_id`;


DROP FUNCTION IF EXISTS `GET_CHART_VIEW_COPY_NAME`;
delimiter ;;
CREATE FUNCTION `GET_CHART_VIEW_COPY_NAME`(chartId varchar(255),pid varchar(255))
    RETURNS varchar(255) CHARSET utf8mb4
  READS SQL DATA
BEGIN

DECLARE chartName varchar(255);

DECLARE regexpInfo varchar(255);

DECLARE chartNameCount INTEGER;

select (case when `type`='richTextView' then 'RICH_TEXT_VIEW' else `name` end)  into chartName from chart_view where id =chartId;
/**
因为名称存在（）等特殊字符，所以不能直接用REGEXP进行查找
1.用like 'chartName%' 过滤可能的数据项
2.REPLACE(name,chartName,'') REGEXP '-([0-9])+$' 过滤去掉chartName后的字符以 -/d 结尾的数据
3.(LENGTH(REPLACE(name,chartName,''))-LENGTH(replace(REPLACE(name,chartName,''),'-',''))=1) 确定只出现一次 ‘-’ 防止多次copy
**/
select (count(1)+1) into chartNameCount from chart_view
where (LENGTH(REPLACE(name,chartName,''))-LENGTH(replace(REPLACE(name,chartName,''),'-',''))=1)
  and REPLACE(name,chartName,'') REGEXP '-([0-9])+$' and name like CONCAT(chartName,'%') and chart_view.scene_id=pid ;

RETURN concat(chartName,'-',chartNameCount);

END
;;
delimiter ;

ALTER TABLE `panel_view_linkage`
    ADD COLUMN `linkage_active` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用关联' AFTER `update_people`;

update panel_view_linkage set linkage_active=1;