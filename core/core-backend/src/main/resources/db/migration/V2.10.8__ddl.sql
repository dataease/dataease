alter table `core_dataset_group`
    add is_cross bit null comment '是否跨源';

INSERT INTO `core_sys_startup_job`
VALUES ('datasetCrossListener', 'datasetCrossListener', 'ready');