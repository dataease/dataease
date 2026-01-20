ALTER TABLE `snapshot_visualization_outer_params_target_view_info`
    ADD COLUMN `match_mode` varchar(255) NULL DEFAULT 'self' COMMENT '匹配方式';

ALTER TABLE `visualization_outer_params_target_view_info`
    ADD COLUMN `match_mode` varchar(255) NULL DEFAULT 'self' COMMENT '匹配方式';
