DROP TABLE IF EXISTS `export_task`;
CREATE TABLE `export_task`
(
    `id`                  varchar(255) NOT NULL,
    `user_id`             bigint(20)   NOT NULL,
    `file_name`           varchar(2048) DEFAULT NULL,
    `file_size`           DOUBLE        DEFAULT NULL,
    `file_size_unit`      varchar(255)  DEFAULT NULL,
    `export_from`         varchar(255)  DEFAULT NULL,
    `export_status`       varchar(255)  DEFAULT NULL,
    `export_from_type`    varchar(255)  DEFAULT NULL,
    `export_time`         bigint(20)    DEFAULT NULL,
    `export_pogress`      varchar(255)  DEFAULT NULL,
    `export_machine_name` varchar(512)  DEFAULT NULL,
    `params`              longtext     NOT NULL COMMENT '过滤参数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;


update `sys_user`
set `pwd_reset_time` = `update_time`
where `pwd_reset_time` IS NULL;

UPDATE `area_mapping`
set `county_code` = '330113',
    `county_name` = '临平区'
where `county_code` = '330103';
UPDATE `area_mapping`
set `county_code` = '330114',
    `county_name` = '钱塘区'
where `county_code` = '330104';

UPDATE `area_mapping_global`
set `county_code` = '156330113',
    `county_name` = '临平区'
where `county_code` = '156330103';
UPDATE `area_mapping_global`
set `county_code` = '156330114',
    `county_name` = '钱塘区'
where `county_code` = '156330104';


INSERT INTO `sys_auth_detail` (`id`, `auth_id`, `privilege_name`, `privilege_type`, `privilege_value`, `privilege_extend`, `remark`, `create_user`, `create_time`, `update_time`, `copy_from`, `copy_id`) VALUES ('data_fill_write', 'data_fill', 'i18n_auth_write', 2, 0, 'write', '基础权限-写入', 'system', NULL, NULL, NULL, NULL);

delete sys_auth_detail,sys_auth  from sys_auth_detail INNER JOIN sys_auth on sys_auth.id = sys_auth_detail.auth_id
where sys_auth.auth_source_type ='data_fill';

delete from sys_auth where auth_source_type ='data_fill';