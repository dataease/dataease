alter table data_fill_form
    add commit_new_update tinyint(1) default 0 null;
alter table data_fill_user_task
    modify value_id text null comment '表内值ID';

