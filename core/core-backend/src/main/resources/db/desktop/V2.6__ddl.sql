ALTER TABLE `data_visualization_info`
    ADD COLUMN `version` int NULL DEFAULT 3 COMMENT '可视化资源版本';
update data_visualization_info set version = 2;

ALTER TABLE `visualization_template`
    ADD COLUMN `version` int NULL DEFAULT 3 COMMENT '使用资源的版本';
update visualization_template set version = 2;
