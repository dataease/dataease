UPDATE `sys_menu` set `component` = REPLACE(`component`, 'SysParam', 'sysParam') where (`component` like '%SysParam%');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'privateForm', 'PrivateForm') where (`component` like '%privateForm%');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'personPwd', 'PersonPwd') where (`component` like '%personPwd%');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'dataset', 'Dataset') where (`component` = 'system/task/dataset');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'form', 'Form') where (`component` = 'system/task/form');

ALTER TABLE `dataset_table_field` ADD COLUMN `date_format` VARCHAR(255) NULL AFTER `accuracy`;



