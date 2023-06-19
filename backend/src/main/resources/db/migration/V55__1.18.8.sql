UPDATE `my_plugin`
SET `version` = '1.18.8'
where `plugin_id` > 0
  and `version` = '1.18.7';

ALTER TABLE `sys_task_email`
    ADD COLUMN `panel_format` INT(1) NOT NULL DEFAULT 0 COMMENT '仪表板格式0:jpeg,1:pdf' AFTER `panel_id`;

