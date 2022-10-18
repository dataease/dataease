UPDATE `sys_menu` set `component` = REPLACE(`component`, 'SysParam', 'sysParam') where (`component` like '%SysParam%');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'privateForm', 'PrivateForm') where (`component` like '%privateForm%');

UPDATE `sys_menu` set `component` = REPLACE(`component`, 'personPwd', 'PsersonPwd') where (`component` like '%personPwd%');
