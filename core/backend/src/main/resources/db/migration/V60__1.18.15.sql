ALTER TABLE `datasource`
    ADD COLUMN `version` varchar(255) NULL COMMENT '版本' AFTER `status`;

ALTER TABLE `sys_auth`
    MODIFY COLUMN `auth_details` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '授权明细' AFTER `auth_time`;

ALTER TABLE `sys_auth_detail`
    MODIFY COLUMN `privilege_type` int(6) NULL DEFAULT NULL COMMENT '权限类型：1 使用/查看  3 导出/管理 5 仪表板管理 15 授权' AFTER `privilege_name`,
    MODIFY COLUMN `privilege_value` int(6) NULL DEFAULT NULL COMMENT '权限值：1 可用 0 不可用' AFTER `privilege_type`;

ALTER TABLE `sys_task_email`
    MODIFY COLUMN `groups` longtext NULL COMMENT '群聊' AFTER `view_data_range`;