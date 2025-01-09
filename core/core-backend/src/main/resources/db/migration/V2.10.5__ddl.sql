ALTER TABLE `xpack_threshold_info`
    ADD COLUMN `reci_larksuite_groups` longtext NULL COMMENT '国际飞书群' AFTER `reci_lark_groups`;
