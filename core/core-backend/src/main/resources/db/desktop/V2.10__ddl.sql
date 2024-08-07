BEGIN;
INSERT INTO `core_sys_startup_job` VALUES ('chartFilterDynamic', 'chartFilterDynamic', 'ready');
COMMIT;

alter table `core_datasource`
    add `enable_data_fill` tinyint default 0 null comment '启用数据填报功能';
