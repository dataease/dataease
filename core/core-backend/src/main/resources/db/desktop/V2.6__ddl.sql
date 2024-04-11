ALTER TABLE `xpack_share`
    ADD COLUMN `auto_pwd` tinyint(1) NOT NULL DEFAULT 1 COMMENT '自动生成密码';

ALTER TABLE `data_visualization_info`
    ADD COLUMN `version` int NULL DEFAULT 3 COMMENT '可视化资源版本';
update data_visualization_info set version = 2;