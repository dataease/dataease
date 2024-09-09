ALTER TABLE `visualization_outer_params_info`
    ADD COLUMN `required` tinyint(1) DEFAULT 0 COMMENT '是否必填',
ADD COLUMN `default_value` longtext NULL COMMENT '默认值 JSON格式';
update visualization_outer_params_info set required =0;