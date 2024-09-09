ALTER TABLE `visualization_outer_params_info`
    ADD COLUMN `required` tinyint(1) DEFAULT 0 COMMENT '是否必填',
ADD COLUMN `default_value` longtext NULL COMMENT '默认值 JSON格式';
ADD COLUMN `enabled_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用默认值';
update visualization_outer_params_info set required =0;