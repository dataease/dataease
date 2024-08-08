BEGIN;
INSERT INTO `core_sys_startup_job` VALUES ('chartFilterDynamic', 'chartFilterDynamic', 'ready');
COMMIT;

alter table `core_dataset_table_field` add params text null comment '计算字段参数';

alter table `core_datasource`
    add `enable_data_fill` tinyint default 0 null comment '启用数据填报功能';

INSERT INTO `core_menu` VALUES (64, 15, 2, 'font', 'system/font', 10, 'icon_font', '/font', 0, 1, 0);
