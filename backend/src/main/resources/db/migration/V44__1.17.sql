ALTER TABLE `sys_log`
    CHANGE COLUMN `user_id` `user_id` BIGINT NULL COMMENT '操作人',
    CHANGE COLUMN `login_name` `login_name` VARCHAR (255) NULL COMMENT '登录账号',
    CHANGE COLUMN `nick_name` `nick_name` VARCHAR (255) NULL COMMENT '姓名';

UPDATE `sys_menu` SET `component` = 'dataset/Form' WHERE (`menu_id` = '800');


UPDATE `sys_menu` SET `component` = 'msg/All' WHERE (`component` = 'msg/all');
UPDATE `sys_menu` SET `component` = 'msg/Setting' WHERE (`component` = 'msg/setting');
