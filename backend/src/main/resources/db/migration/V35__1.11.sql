ALTER TABLE `panel_group`
ADD COLUMN `status` varchar(255) NULL DEFAULT 'publish' COMMENT '1.publish--发布 2.unpublished--未发布' AFTER `mobile_layout`;

UPDATE `sys_menu` SET `sub_count` = 1 WHERE `menu_id` = 40;
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (401, 40, 0, 2, '查看系统模板', NULL, NULL, 999, NULL, NULL, 0, 0, 0, 'sys-template:read', NULL, NULL, 1614930862373, 1614930862373);
