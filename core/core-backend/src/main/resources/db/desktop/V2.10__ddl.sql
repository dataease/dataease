BEGIN;
INSERT INTO `core_sys_startup_job` VALUES ('chartFilterDynamic', 'chartFilterDynamic', 'ready');
COMMIT;

alter table `core_dataset_table_field` add params text null comment '计算字段参数';

alter table `core_datasource`
    add `enable_data_fill` tinyint default 0 null comment '启用数据填报功能';


ALTER TABLE `visualization_outer_params_target_view_info`
    MODIFY COLUMN `target_view_id` varchar(50) NULL DEFAULT NULL COMMENT '联动视图ID/联动过滤项ID' ,
    ADD COLUMN `target_ds_id` varchar(50) NULL COMMENT '联动数据集id/联动过滤组件id' ;

    
alter table `core_chart_view`
    add flow_map_start_name longtext comment '流向地图起点名称field';
alter table `core_chart_view`
    add flow_map_end_name longtext comment '流向地图终点名称field';
alter table `core_chart_view`
    add ext_color longtext comment '颜色维度field';

update visualization_outer_params_target_view_info tvi INNER JOIN core_chart_view ccv on tvi.target_view_id = ccv.id
    set tvi.target_ds_id = ccv.table_id
