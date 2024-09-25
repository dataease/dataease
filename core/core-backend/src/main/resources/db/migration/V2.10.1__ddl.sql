ALTER TABLE `visualization_outer_params_info`
    ADD COLUMN `required`        tinyint(1)   DEFAULT 0 COMMENT '是否必填',
    ADD COLUMN `default_value`   varchar(255) DEFAULT NULL COMMENT '默认值 JSON格式',
    ADD COLUMN `enabled_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用默认值';
update visualization_outer_params_info
set required =0;


ALTER TABLE `visualization_link_jump_info`
    ADD COLUMN `window_size` varchar(255) NULL DEFAULT 'middle' COMMENT '窗口大小large middle small';

