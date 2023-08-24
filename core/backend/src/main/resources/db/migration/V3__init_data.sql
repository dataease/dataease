INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('default.language', 'zh_CN', 'text', 5);

BEGIN;
INSERT INTO `sys_dept` VALUES (1, 0, 0, '默认组织', 0, NULL, NULL, 1622533297817, 1622533297817);
COMMIT;

BEGIN;
INSERT INTO `sys_menu` VALUES (1, 0, 3, 0, '系统管理', 'system', 'Layout', 6, 'system', '/system', NULL, b'0', b'0', 'dir:sys', NULL, NULL, NULL, 1614916695777);
INSERT INTO `sys_menu` VALUES (2, 1, 4, 1, '用户管理', 'system-user', 'system/user/index', 1, 'peoples', 'user', NULL, b'0', b'0', 'user:read', NULL, NULL, NULL, 1620281952752);
INSERT INTO `sys_menu` VALUES (8, 0, 0, 1, '数据集', 'dataset', 'dataset/index', 3, '', '/dataset', NULL, b'0', b'0', 'data:read', NULL, NULL, NULL, 1614916684821);
INSERT INTO `sys_menu` VALUES (10, 0, 0, 1, '视图', 'view', 'chart/index', 2, '', '/chart', NULL, b'0', b'0', 'chart:read', NULL, NULL, NULL, 1614915491036);
INSERT INTO `sys_menu` VALUES (15, 2, 0, 2, '创建用户', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:add', NULL, NULL, 1614930862373, 1614930862373);
INSERT INTO `sys_menu` VALUES (16, 2, 0, 2, '删除用户', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:del', NULL, NULL, 1614930903502, 1614930903502);
INSERT INTO `sys_menu` VALUES (17, 2, 0, 2, '编辑用户', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:edit', NULL, NULL, 1614930935529, 1614930935529);
INSERT INTO `sys_menu` VALUES (24, 34, 0, 2, '创建连接', NULL, NULL, 997, NULL, NULL, b'0', b'0', b'0', 'datasource:add', NULL, NULL, 1614931168956, 1615783705537);

INSERT INTO `sys_menu` VALUES (28, 2, 0, 2, '修改密码', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:editPwd', NULL, NULL, 1615275128262, 1615275128262);
INSERT INTO `sys_menu` VALUES (30, 0, 0, 1, '仪表板', 'panel', 'panel/index', 1, NULL, '/panel', b'0', b'0', b'0', NULL, NULL, NULL, NULL, 1619081449067);
INSERT INTO `sys_menu` VALUES (34, 0, 4, 1, '数据源', 'datasource', 'system/datasource/index', 4, NULL, '/datasource', b'0', b'0', b'0', 'datasource:read', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (35, 1, 0, 1, '用户表单', 'system-user-form', 'system/user/form', 10, '', 'user-form', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (39, 0, 0, 1, '数据源表单', 'datasource-form', 'system/datasource/form', 5, NULL, '/ds-form', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (40, 1, 0, 1, '模板管理', 'system-template', 'panel/template/index', 13, 'dashboard', 'panel/template/index', NULL, b'0', b'0', 'template:read', NULL, NULL, NULL, 1620444227389);
INSERT INTO `sys_menu` VALUES (50, 0, 0, 1, '个人信息', 'person-info', 'system/user/privateForm', 999, NULL, '/person-info', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (51, 0, 0, 1, '重置密码', 'person-pwd-reset', 'system/user/personPwd', 999, NULL, '/person-pwd', b'0', b'0', b'1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (52, 0, 0, 1, '关于', 'about', 'system/about/index', 16, 'system', '/about', b'0', b'0', b'1', NULL, NULL, NULL, NULL, 1620897406691);
COMMIT;

BEGIN;
INSERT INTO `sys_user` VALUES (1, 0, 'admin', '管理员', '男', NULL, 'admin@fit2cloud.com', '40b8893ea9ebc2d631c4bb42bb1e8996', b'1', 1, NULL, NULL, NULL, NULL, 1615184951534, 'zh_CN');
INSERT INTO `sys_user` VALUES (2, 1, 'demo', 'demo', '男', NULL, 'demo@fit2cloud.com', '40b8893ea9ebc2d631c4bb42bb1e8996', b'0', 1, NULL, NULL, NULL, 1619086036234, 1622533509697, 'zh_CN');
COMMIT;


BEGIN;
INSERT INTO `sys_role` VALUES (1, '管理员', '', NULL, NULL, REPLACE(unix_timestamp(current_timestamp(3)),'.',''), null);
INSERT INTO `sys_role` VALUES (2, '普通员工', '', NULL, NULL, REPLACE(unix_timestamp(current_timestamp(3)),'.',''), null);
COMMIT;


BEGIN;
INSERT INTO `sys_roles_menus` VALUES (1, 1);
INSERT INTO `sys_roles_menus` VALUES (2, 1);
INSERT INTO `sys_roles_menus` VALUES (3, 1);
INSERT INTO `sys_roles_menus` VALUES (4, 1);
INSERT INTO `sys_roles_menus` VALUES (5, 1);
INSERT INTO `sys_roles_menus` VALUES (6, 1);
INSERT INTO `sys_roles_menus` VALUES (8, 1);
INSERT INTO `sys_roles_menus` VALUES (10, 1);
INSERT INTO `sys_roles_menus` VALUES (11, 1);
INSERT INTO `sys_roles_menus` VALUES (14, 1);
INSERT INTO `sys_roles_menus` VALUES (15, 1);
INSERT INTO `sys_roles_menus` VALUES (16, 1);
INSERT INTO `sys_roles_menus` VALUES (17, 1);
INSERT INTO `sys_roles_menus` VALUES (18, 1);
INSERT INTO `sys_roles_menus` VALUES (19, 1);
INSERT INTO `sys_roles_menus` VALUES (20, 1);
INSERT INTO `sys_roles_menus` VALUES (21, 1);
INSERT INTO `sys_roles_menus` VALUES (22, 1);
INSERT INTO `sys_roles_menus` VALUES (23, 1);
INSERT INTO `sys_roles_menus` VALUES (24, 1);
INSERT INTO `sys_roles_menus` VALUES (25, 1);
INSERT INTO `sys_roles_menus` VALUES (26, 1);
INSERT INTO `sys_roles_menus` VALUES (27, 1);
INSERT INTO `sys_roles_menus` VALUES (28, 1);
INSERT INTO `sys_roles_menus` VALUES (30, 1);
INSERT INTO `sys_roles_menus` VALUES (31, 1);
INSERT INTO `sys_roles_menus` VALUES (32, 1);
INSERT INTO `sys_roles_menus` VALUES (34, 1);
INSERT INTO `sys_roles_menus` VALUES (40, 1);
INSERT INTO `sys_roles_menus` VALUES (41, 1);
INSERT INTO `sys_roles_menus` VALUES (42, 1);
INSERT INTO `sys_roles_menus` VALUES (8, 2);
INSERT INTO `sys_roles_menus` VALUES (10, 2);
INSERT INTO `sys_roles_menus` VALUES (24, 2);
INSERT INTO `sys_roles_menus` VALUES (25, 2);
INSERT INTO `sys_roles_menus` VALUES (26, 2);
INSERT INTO `sys_roles_menus` VALUES (27, 2);
INSERT INTO `sys_roles_menus` VALUES (30, 2);
INSERT INTO `sys_roles_menus` VALUES (34, 2);
COMMIT;

BEGIN;
INSERT INTO `sys_users_roles` VALUES (1, 1);
INSERT INTO `sys_users_roles` VALUES (2, 2);
COMMIT;

INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.favicon', NULL, 'file', 6);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.loginImage', NULL, 'file', 3);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.loginLogo', NULL, 'file', 2);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.loginTitle', '', 'text', 4);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.logo', NULL, 'file', 1);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.title', '', 'text', 5);

BEGIN;
INSERT INTO `panel_group`(`id`, `name`, `pid`, `level`, `node_type`, `create_by`, `create_time`, `panel_type`, `panel_style`, `panel_data`, `source`, `extend1`, `extend2`, `remark`) VALUES ('default_panel', 'i18n_default_panel', '0', -1, 'folder', 'admin', NULL, 'system', '{}', '[]', NULL, NULL, NULL, '系统内置 默认仪表板');
INSERT INTO `panel_group`(`id`, `name`, `pid`, `level`, `node_type`, `create_by`, `create_time`, `panel_type`, `panel_style`, `panel_data`, `source`, `extend1`, `extend2`, `remark`) VALUES ('panel_list', 'i18n_panel_list', '0', -1, 'folder', 'admin', NULL, 'self', '{}', '[]', NULL, NULL, NULL, '系统内置 仪表板列表');
COMMIT;



INSERT INTO `panel_template`(`id`, `name`, `pid`, `level`, `node_type`, `create_by`, `create_time`, `snapshot`, `template_type`, `template_style`, `template_data`, `dynamic_data`) VALUES ('self', '用户模板', '', -1, 'folder', NULL, NULL, '', 'self', '', '', NULL);
INSERT INTO `panel_template`(`id`, `name`, `pid`, `level`, `node_type`, `create_by`, `create_time`, `snapshot`, `template_type`, `template_style`, `template_data`, `dynamic_data`) VALUES ('system', '系统模板', '', -1, 'folder', NULL, NULL, NULL, 'system', NULL, NULL, NULL);

BEGIN;
INSERT INTO `my_plugin` VALUES (1, 'xpakc默认插件', 'default', 0, 20000, 'xpack', '请购买正式许可', '1.0-SNAPSHOT', NULL, 'fit2cloud-chenyw', 1, 1620624387327, NULL, 'deplugin-xpack-backend', NULL);
COMMIT;
-- ----------------------------


