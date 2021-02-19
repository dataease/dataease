DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '上级部门',
  `sub_count` int(5) DEFAULT 0 COMMENT '子部门数目',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `dept_sort` int(5) DEFAULT 999 COMMENT '排序',
  `enabled` bit(1) NOT NULL COMMENT '状态',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  KEY `inx_pid` (`pid`),
  KEY `inx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='部门';


DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '字典名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='数据字典';


DROP TABLE IF EXISTS `sys_dict_detail`;
CREATE TABLE `sys_dict_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_id` bigint(11) DEFAULT NULL COMMENT '字典id',
  `label` varchar(255) NOT NULL COMMENT '字典标签',
  `value` varchar(255) NOT NULL COMMENT '字典值',
  `dict_sort` int(5) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`detail_id`) USING BTREE,
  KEY `FK5tpkputc6d9nboxojdbgnpmyb` (`dict_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='数据字典详情';


DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '上级菜单ID',
  `sub_count` int(5) DEFAULT 0 COMMENT '子菜单数目',
  `type` int(11) DEFAULT NULL COMMENT '菜单类型',
  `title` varchar(255) DEFAULT NULL COMMENT '菜单标题',
  `name` varchar(255) DEFAULT NULL COMMENT '组件名称',
  `component` varchar(255) DEFAULT NULL COMMENT '组件',
  `menu_sort` int(5) DEFAULT NULL COMMENT '排序',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `path` varchar(255) DEFAULT NULL COMMENT '链接地址',
  `i_frame` bit(1) DEFAULT NULL COMMENT '是否外链',
  `cache` bit(1) DEFAULT b'0' COMMENT '缓存',
  `hidden` bit(1) DEFAULT b'0' COMMENT '隐藏',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`) USING BTREE,
  UNIQUE KEY `uniq_title` (`title`),
  UNIQUE KEY `uniq_name` (`name`),
  KEY `inx_pid` (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统菜单';


DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE KEY `uniq_name` (`name`),
  KEY `role_name_index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` VALUES (1, '超级管理员', '-', NULL, 'admin', '2018-11-23 11:04:37', '2020-08-06 16:10:24');
INSERT INTO `sys_role` VALUES (2, '普通用户', '-', NULL, 'admin', '2018-11-23 13:09:06', '2020-09-05 10:45:12');
COMMIT;


DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门名称',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `nick_name` varchar(255) DEFAULT NULL COMMENT '昵称',
  `gender` varchar(2) DEFAULT NULL COMMENT '性别',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `avatar_name` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `avatar_path` varchar(255) DEFAULT NULL COMMENT '头像真实路径',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `is_admin` bit(1) DEFAULT b'0' COMMENT '是否为admin账号',
  `enabled` bigint(20) DEFAULT NULL COMMENT '状态：1启用、0禁用',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新着',
  `pwd_reset_time` datetime DEFAULT NULL COMMENT '修改密码的时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `UK_kpubos9gc2cvtkb0thktkbkes` (`email`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  UNIQUE KEY `uniq_username` (`username`),
  UNIQUE KEY `uniq_email` (`email`),
  KEY `FK5rwmryny6jthaaxkogownknqp` (`dept_id`) USING BTREE,
  KEY `FKpq2dhypk2qgt68nauh2by22jb` (`avatar_name`) USING BTREE,
  KEY `inx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统用户';

BEGIN;
INSERT INTO `sys_user` VALUES (1, 2, 'admin', '管理员', '男', '18888888888', '201507802@qq.com', 'avatar-20200806032259161.png', '/Users/jie/Documents/work/me/admin/eladmin/~/avatar/avatar-20200806032259161.png', 'AkE8KFGCjkd0eAI6upPrTCGaGUe9+NzYmr2sFqjmD7jcI/ObzxVwy4bf8ihHl2wVzAFf8RJccS7H+617sgvCeQ==', b'1', 1, NULL, 'admin', '2020-05-03 16:38:31', '2018-08-23 09:11:56', '2020-09-05 10:43:31');
COMMIT;

DROP TABLE IF EXISTS `sys_users_roles`;
CREATE TABLE `sys_users_roles` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE,
  KEY `FKq4eq273l04bpu4efj0jd0jb98` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='用户角色关联';


DROP TABLE IF EXISTS `datasource`;
CREATE TABLE `datasource` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `type` varchar(255) NOT NULL COMMENT '类型',
  `configuration` longtext COMMENT '详细信息',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='数据源链接';



DROP TABLE IF EXISTS `sys_roles_menus`;
CREATE TABLE `sys_roles_menus` (
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`menu_id`,`role_id`) USING BTREE,
  KEY `FKcngg2qadojhi3a651a5adkvbq` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色菜单关联';

