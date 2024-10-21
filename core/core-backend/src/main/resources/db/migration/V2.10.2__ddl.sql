INSERT INTO area (id, level, name, pid) VALUES ('156440315', 'district', '大鹏新区', '156440300');

DELETE ccv
FROM
	core_chart_view ccv
	INNER JOIN data_visualization_info dvi ON ccv.scene_id = dvi.id
WHERE
	dvi.delete_flag =1;
delete from data_visualization_info dvi where dvi.delete_flag =1;
DELETE FROM area where pid = '156710100' OR id = '156710100';