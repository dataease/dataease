delimiter ;;
CREATE  FUNCTION `GET_CHART_VIEW_COPY_NAME`(chartId varchar(255)) RETURNS varchar(255)
    READS SQL DATA
BEGIN

DECLARE chartName varchar(255);

DECLARE pid varchar(255);

DECLARE regexpInfo varchar(255);

DECLARE chartNameCount INTEGER;

select `name` ,`scene_id` into chartName, pid from chart_view where id =chartId;

set regexpInfo = concat('^',chartName,'-copy','\\(([0-9])+\\)$');

select (count(1)+1) into chartNameCount from chart_view where name REGEXP regexpInfo;

RETURN concat(chartName,'-copy(',chartNameCount,')');

END
;;
delimiter ;
