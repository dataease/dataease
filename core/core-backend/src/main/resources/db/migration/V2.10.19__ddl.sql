ALTER TABLE `xpack_threshold_info`
    ADD COLUMN `show_field_value` tinyint(1) NOT NULL DEFAULT 0 COMMENT '显示字段值' AFTER `repeat_send`;

ALTER TABLE `xpack_threshold_info_snapshot`
    ADD COLUMN `show_field_value` tinyint(1) NOT NULL DEFAULT 0 COMMENT '显示字段值' AFTER `repeat_send`;

