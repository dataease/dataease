alter table datasource
    add enable_data_fill tinyint(1) default 0 null comment '开启数据填报',
    add enable_data_fill_create_table tinyint(1) default 0 null comment '数据填报允许新建表';