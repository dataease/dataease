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
