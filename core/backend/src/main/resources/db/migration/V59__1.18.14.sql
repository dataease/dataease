UPDATE `sys_user`
set `enabled` = 0
where `user_id` = 2;

ALTER TABLE `sys_task_email`
    ADD COLUMN `groups` varchar(255) NULL COMMENT '群聊' AFTER `view_data_range`;

ALTER TABLE `sys_task_email`
    ADD COLUMN `ext_wait_time` int(0) NOT NULL DEFAULT 0 COMMENT '加载仪表板额外等待时间(s)' AFTER `groups`;

ALTER TABLE `sys_task_email`
    ADD COLUMN `role_list` longtext NULL COMMENT '收件角色' AFTER `ext_wait_time`,
    ADD COLUMN `org_list`  longtext NULL COMMENT '收件组织' AFTER `role_list`;

ALTER TABLE `sys_task_email`
    MODIFY COLUMN `reci_users` longtext NULL COMMENT '接收人账号' AFTER `conditions`;

ALTER TABLE de_driver ADD COLUMN `surpport_versions` LONGTEXT NULL AFTER `desc`;

BEGIN;
INSERT INTO `sys_startup_job` VALUES ('chartFilterMerge', 'chartFilterMerge', 'ready');
COMMIT;
