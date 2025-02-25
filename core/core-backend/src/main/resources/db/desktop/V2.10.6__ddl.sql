ALTER TABLE `core_dataset_table_field`
    ADD COLUMN `group_list` longtext NULL;

ALTER TABLE `core_dataset_table_field`
    ADD COLUMN `other_group` longtext NULL;


alter table `per_data_filling_task_sub_instance`
    modify `data_id` varchar(255) null comment '数据ID';
alter table per_data_filling_commit_log
    modify `data_id` varchar(255) not null comment '操作的数据ID';
