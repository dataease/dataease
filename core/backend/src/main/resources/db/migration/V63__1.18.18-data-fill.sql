DROP TABLE IF EXISTS `data_fill_form`;
create table data_fill_form
(
    id            varchar(50)                            not null comment '主键ID'
        primary key,
    name          varchar(255)                           null comment '名称',
    pid           varchar(255)                           null comment '父级ID',
    level         int(10)                                null comment '层级',
    node_type     varchar(255)                           null comment 'folder/panel 目录或文件夹',
    table_name    varchar(255)                           null comment '表名',
    datasource    varchar(255)                           null comment '数据源',
    forms         longtext                               null comment '表单内容',
    create_index  tinyint(1) default 0                   null comment '是否创建索引',
    table_indexes longtext                               null comment '索引',
    create_by     varchar(255)                           null comment '创建人',
    create_time   datetime   default current_timestamp() null comment '创建时间',
    update_by     varchar(255)                           null comment '更新人',
    update_time   datetime   default current_timestamp() null comment '更新时间'
)
    comment '数据填报表单';

CREATE TRIGGER `delete_auth_data_fill_form`
    AFTER DELETE
    ON `data_fill_form`
    FOR EACH ROW select delete_auth_source(OLD.id, 'data_fill')
                 into @ee;


DROP VIEW IF EXISTS `v_auth_model`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_auth_model` AS
SELECT `sys_user`.`user_id`                                            AS `id`,
       concat(`sys_user`.`nick_name`, '（', `sys_user`.`username`, '）') AS `name`,
       `sys_user`.`username`                                           AS `label`,
       '0'                                                             AS `pid`,
       'leaf'                                                          AS `node_type`,
       'user'                                                          AS `model_type`,
       'user'                                                          AS `model_inner_type`,
       'target'                                                        AS `auth_type`,
       `sys_user`.`create_by`                                          AS `create_by`,
       0                                                               AS `level`,
       0                                                               AS `mode`,
       '0'                                                             AS `data_source_id`
FROM `sys_user`
WHERE (`sys_user`.`is_admin` <> 1)
UNION ALL
SELECT `sys_role`.`role_id`   AS `id`,
       `sys_role`.`name`      AS `name`,
       `sys_role`.`name`      AS `label`,
       '0'                    AS `pid`,
       'leaf'                 AS `node_type`,
       'role'                 AS `model_type`,
       'role'                 AS `model_inner_type`,
       'target'               AS `auth_type`,
       `sys_role`.`create_by` AS `create_by`,
       0                      AS `level`,
       0                      AS `mode`,
       '0'                    AS `data_source_id`
FROM `sys_role`
UNION ALL
SELECT `sys_dept`.`dept_id`                                AS `id`,
       `sys_dept`.`name`                                   AS `name`,
       `sys_dept`.`name`                                   AS `label`,
       (
               cast(`sys_dept`.`pid` AS CHAR charset utf8mb4) COLLATE utf8mb4_general_ci
           )                                               AS `pid`,
       IF
           ((`sys_dept`.`sub_count` = 0), 'leaf', 'spine') AS `node_type`,
       'dept'                                              AS `model_type`,
       'dept'                                              AS `model_inner_type`,
       'target'                                            AS `auth_type`,
       `sys_dept`.`create_by`                              AS `create_by`,
       0                                                   AS `level`,
       0                                                   AS `mode`,
       '0'                                                 AS `data_source_id`
FROM `sys_dept`
UNION ALL
SELECT `datasource`.`id`        AS `id`,
       `datasource`.`name`      AS `NAME`,
       `datasource`.`name`      AS `label`,
       '0'                      AS `pid`,
       'leaf'                   AS `node_type`,
       'link'                   AS `model_type`,
       `datasource`.`type`      AS `model_inner_type`,
       'source'                 AS `auth_type`,
       `datasource`.`create_by` AS `create_by`,
       0                        AS `level`,
       0                        AS `mode`,
       '0'                      AS `data_source_id`
FROM `datasource`
UNION ALL
SELECT `dataset_group`.`id`                                            AS `id`,
       `dataset_group`.`name`                                          AS `NAME`,
       `dataset_group`.`name`                                          AS `label`,
       IF
           (isnull(`dataset_group`.`pid`), '0', `dataset_group`.`pid`) AS `pid`,
       'spine'                                                         AS `node_type`,
       'dataset'                                                       AS `model_type`,
       `dataset_group`.`type`                                          AS `model_inner_type`,
       'source'                                                        AS `auth_type`,
       `dataset_group`.`create_by`                                     AS `create_by`,
       `dataset_group`.`level`                                         AS `level`,
       0                                                               AS `mode`,
       '0'                                                             AS `data_source_id`
FROM `dataset_group`
UNION ALL
SELECT `data_fill_form`.`id`                                            AS `id`,
       `data_fill_form`.`name`                                          AS `NAME`,
       `data_fill_form`.`name`                                          AS `label`,
       (
           CASE
               `data_fill_form`.`id`
               WHEN 'data_fill_list' THEN
                   '0'
               WHEN 'default_data_fill' THEN
                   '0'
               ELSE `data_fill_form`.`pid`
               END
           )                                                            AS `pid`,
       IF
           ((`data_fill_form`.`node_type` = 'folder'), 'spine', 'leaf') AS `node_type`,
       'data_fill'                                                      AS `model_type`,
       'form'                                                           AS `model_inner_type`,
       'source'                                                         AS `auth_type`,
       `data_fill_form`.`create_by`                                     AS `create_by`,
       `data_fill_form`.`level`                                         AS `level`,
       0                                                                AS `mode`,
       '0'                                                              AS `data_source_id`
FROM `data_fill_form`
UNION ALL
SELECT `dataset_table`.`id`             AS `id`,
       `dataset_table`.`name`           AS `NAME`,
       `dataset_table`.`name`           AS `label`,
       `dataset_table`.`scene_id`       AS `pid`,
       'leaf'                           AS `node_type`,
       'dataset'                        AS `model_type`,
       `dataset_table`.`type`           AS `model_inner_type`,
       'source'                         AS `auth_type`,
       `dataset_table`.`create_by`      AS `create_by`,
       0                                AS `level`,
       `dataset_table`.`mode`           AS `mode`,
       `dataset_table`.`data_source_id` AS `data_source_id`
FROM `dataset_table`
UNION ALL
SELECT `panel_group`.`id`                                            AS `id`,
       `panel_group`.`name`                                          AS `NAME`,
       `panel_group`.`name`                                          AS `label`,
       (
           CASE
               `panel_group`.`id`
               WHEN 'panel_list' THEN
                   '0'
               WHEN 'default_panel' THEN
                   '0'
               ELSE `panel_group`.`pid`
               END
           )                                                         AS `pid`,
       IF
           ((`panel_group`.`node_type` = 'folder'), 'spine', 'leaf') AS `node_type`,
       'panel'                                                       AS `model_type`,
       `panel_group`.`panel_type`                                    AS `model_inner_type`,
       'source'                                                      AS `auth_type`,
       `panel_group`.`create_by`                                     AS `create_by`,
       0                                                             AS `level`,
       0                                                             AS `mode`,
       '0'                                                           AS `data_source_id`
FROM `panel_group`
UNION ALL
SELECT `sys_menu`.`menu_id`                                AS `menu_id`,
       `sys_menu`.`title`                                  AS `name`,
       `sys_menu`.`title`                                  AS `label`,
       `sys_menu`.`pid`                                    AS `pid`,
       IF
           ((`sys_menu`.`sub_count` > 0), 'spine', 'leaf') AS `node_type`,
       'menu'                                              AS `model_type`,
       (
           CASE
               `sys_menu`.`type`
               WHEN 0 THEN
                   'folder'
               WHEN 1 THEN
                   'menu'
               WHEN 2 THEN
                   'button'
               END
           )                                               AS `model_inner_type`,
       'source'                                            AS `auth_type`,
       `sys_menu`.`create_by`                              AS `create_by`,
       0                                                   AS `level`,
       0                                                   AS `mode`,
       '0'                                                 AS `data_source_id`
FROM `sys_menu`
WHERE ((
           `sys_menu`.`i_frame` <> 1
           )
    OR isnull(`sys_menu`.`i_frame`))
UNION ALL
SELECT `plugin_sys_menu`.`menu_id`                                AS `menu_id`,
       `plugin_sys_menu`.`title`                                  AS `name`,
       `plugin_sys_menu`.`title`                                  AS `label`,
       `plugin_sys_menu`.`pid`                                    AS `pid`,
       IF
           ((`plugin_sys_menu`.`sub_count` > 0), 'spine', 'leaf') AS `node_type`,
       'menu'                                                     AS `model_type`,
       (
           CASE
               `plugin_sys_menu`.`type`
               WHEN 0 THEN
                   'folder'
               WHEN 1 THEN
                   'menu'
               WHEN 2 THEN
                   'button'
               END
           )                                                      AS `model_inner_type`,
       'source'                                                   AS `auth_type`,
       `plugin_sys_menu`.`create_by`                              AS `create_by`,
       0                                                          AS `level`,
       0                                                          AS `mode`,
       '0'                                                        AS `data_source_id`
FROM `plugin_sys_menu`
WHERE ((
           `plugin_sys_menu`.`i_frame` <> 1
           )
    OR isnull(`plugin_sys_menu`.`i_frame`));


INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('data_fill_grant', 'data_fill', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'system', NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('data_fill_manage', 'data_fill', 'i18n_auth_manage', 3, 0, 'manage', '基础权限-管理', 'system', NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`,
                               `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`,
                               `copy_id`)
VALUES ('data_fill_use', 'data_fill', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'system', NULL, NULL, NULL, NULL);



DROP TABLE IF EXISTS `data_fill_commit_log`;
create table data_fill_commit_log
(
    id          varchar(50)                          not null
        primary key,
    form_id     varchar(50)                          not null,
    data_id     varchar(255)                         null,
    operate     varchar(50)                          not null,
    commit_by   varchar(255)                         not null,
    commit_time datetime default current_timestamp() null
);

create index data_fill_commit_log_form_id_index
    on data_fill_commit_log (form_id);


DROP TABLE IF EXISTS `data_fill_task`;
create table data_fill_task
(
    id                      bigint auto_increment comment '任务ID'
        primary key,
    name                    varchar(255)                           not null comment '任务名称',
    form_id                 varchar(100)                           not null comment '任务ID',
    start_time              datetime                               null comment '开始时间',
    end_time                datetime                               null comment '结束时间',
    rate_type               int(10)                                not null comment '频率方式',
    rate_val                varchar(255)                           null comment '频率值',
    publish_start_time      datetime                               null,
    publish_end_time        datetime                               null,
    publish_range_time_type int                                    null,
    publish_range_time      int                                    null,
    creator                 bigint                                 not null comment '创建者ID',
    create_time             datetime   default current_timestamp() null comment '创建时间',
    status                  tinyint(1) default 1                   null comment '运行状态',
    reci_users              longtext                               null comment '接收人账号',
    role_list               longtext                               null comment '收件角色',
    org_list                longtext                               null comment '收件组织'
)
    comment '数据填报任务';

create index data_fill_task_form_id_index
    on data_fill_task (form_id);


DROP TABLE IF EXISTS `data_fill_user_task`;
create table data_fill_user_task
(
    id          varchar(50)  not null comment 'ID'
        primary key,
    task_id     bigint       not null comment '任务ID',
    form_id     varchar(100) not null comment '表单ID',
    value_id    varchar(100) null comment '表内值ID',
    user        bigint       not null comment '用户ID',
    start_time  datetime     null comment '开始时间',
    end_time    datetime     null comment '结束时间',
    finish_time datetime     null comment '完成时间'
)
    comment '数据填报用户任务';

create index data_fill_user_task_form_id_index
    on data_fill_user_task (form_id);

create index data_fill_user_task_task_id_index
    on data_fill_user_task (task_id);


INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('3c790b17-17c0-4ba6-8c8b-b7a6bee57772', '1200', 'menu', '1', 'role', 1711448429062, null, 'admin', null, null, null);
INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('8e5404a8-ef7f-407e-b67e-4ff7064fdede', '1200', 'menu', '2', 'role', 1711517966976, null, 'admin', null, null, null);
INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('31fa8a67-1513-4297-8a00-c34458a0f760', '1201', 'menu', '1', 'role', 1711448429055, null, 'admin', null, null, null);
INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('44d9496a-047c-4fd4-9b20-94eaff01fa0b', '1201', 'menu', '2', 'role', 1711517966965, null, 'admin', null, null, null);
INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('16eeb4df-e194-4f5b-9817-d1e48c006036', '1202', 'menu', '2', 'role', 1711448593512, null, 'admin', null, null, null);
INSERT INTO sys_auth (id, auth_source, auth_source_type, auth_target, auth_target_type, auth_time, auth_details, auth_user, update_time, copy_from, copy_id) VALUES ('f4112033-f9bf-4e99-9d8e-9417663a6683', '1202', 'menu', '1', 'role', 1711448429067, null, 'admin', null, null, null);

INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('31b8e9ef-ebfc-11ee-8430-0242ac140002', '44d9496a-047c-4fd4-9b20-94eaff01fa0b', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'admin', 1711517966000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('31ba695a-ebfc-11ee-8430-0242ac140002', '8e5404a8-ef7f-407e-b67e-4ff7064fdede', 'i18n_auth_use', 1, 0, 'use', '基础权限-使用', 'admin', 1711517966000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c77c8d-eb5a-11ee-8430-0242ac140002', '31fa8a67-1513-4297-8a00-c34458a0f760', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c81b67-eb5a-11ee-8430-0242ac140002', '3c790b17-17c0-4ba6-8c8b-b7a6bee57772', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c8c0b7-eb5a-11ee-8430-0242ac140002', 'f4112033-f9bf-4e99-9d8e-9417663a6683', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('abccfe10-eb5a-11ee-8430-0242ac140002', '16eeb4df-e194-4f5b-9817-d1e48c006036', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1711448593000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('31b8e988-ebfc-11ee-8430-0242ac140002', '44d9496a-047c-4fd4-9b20-94eaff01fa0b', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711517966000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('31ba6902-ebfc-11ee-8430-0242ac140002', '8e5404a8-ef7f-407e-b67e-4ff7064fdede', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711517966000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c77c73-eb5a-11ee-8430-0242ac140002', '31fa8a67-1513-4297-8a00-c34458a0f760', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c81b44-eb5a-11ee-8430-0242ac140002', '3c790b17-17c0-4ba6-8c8b-b7a6bee57772', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('49c8c096-eb5a-11ee-8430-0242ac140002', 'f4112033-f9bf-4e99-9d8e-9417663a6683', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711448429000, null, null, null);
INSERT INTO sys_auth_detail (id, auth_id, privilege_name, privilege_type, privilege_value, privilege_extend, remark, create_user, create_time, update_time, copy_from, copy_id) VALUES ('abccfdbb-eb5a-11ee-8430-0242ac140002', '16eeb4df-e194-4f5b-9817-d1e48c006036', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1711448593000, null, null, null);


