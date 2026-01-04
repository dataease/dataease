ALTER TABLE `xpack_threshold_info`
    ADD COLUMN `show_field_value` tinyint(1) NOT NULL DEFAULT 0 COMMENT '显示字段值' AFTER `repeat_send`;

ALTER TABLE `xpack_threshold_info_snapshot`
    ADD COLUMN `show_field_value` tinyint(1) NOT NULL DEFAULT 0 COMMENT '显示字段值' AFTER `repeat_send`;

ALTER TABLE `snapshot_visualization_outer_params_target_view_info`
    ADD COLUMN `match_mode` varchar(255) NULL DEFAULT 'self' COMMENT '匹配方式' AFTER `target_ds_id`;

ALTER TABLE `visualization_outer_params_target_view_info`
    ADD COLUMN `match_mode` varchar(255) NULL DEFAULT 'self' COMMENT '匹配方式' AFTER `target_ds_id`;
