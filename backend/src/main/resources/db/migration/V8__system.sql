DROP TABLE IF EXISTS `sys_dept` ;

CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '上级部门',
  `sub_count` int(5) DEFAULT '0' COMMENT '子部门数目',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `dept_sort` int(5) DEFAULT '999' COMMENT '排序',
  `enabled` bit(1) NOT NULL COMMENT '状态',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  KEY `inx_pid` (`pid`),
  KEY `inx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='部门';

INSERT INTO `sys_dept` (`dept_id`, `pid`, `sub_count`, `name`, `dept_sort`, `enabled`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('18','0','1','上海飞致云','1',b'1',null,null,'1614048906358','1614048906358');
INSERT INTO `sys_dept` (`dept_id`, `pid`, `sub_count`, `name`, `dept_sort`, `enabled`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('19','0','1','北京飞致云','2',b'1',null,null,'1614048918465','1614048918465');
INSERT INTO `sys_dept` (`dept_id`, `pid`, `sub_count`, `name`, `dept_sort`, `enabled`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('20','18','1','营销部','1',b'1',null,null,'1614048946370','1614049006759');
INSERT INTO `sys_dept` (`dept_id`, `pid`, `sub_count`, `name`, `dept_sort`, `enabled`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('21','19','0','综合部','3',b'1',null,null,'1614048963483','1615783363091');
INSERT INTO `sys_dept` (`dept_id`, `pid`, `sub_count`, `name`, `dept_sort`, `enabled`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('25','20','0','售前组','1',b'1',null,null,'1615791706945','1615791706945');



DROP TABLE IF EXISTS `sys_menu` ;

CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '上级菜单ID',
  `sub_count` int(5) DEFAULT '0' COMMENT '子菜单数目',
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
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`) USING BTREE,
  UNIQUE KEY `uniq_title` (`title`),
  UNIQUE KEY `uniq_name` (`name`),
  KEY `inx_pid` (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统菜单';

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('1','0','3','0','系统管理','系统管理','Layout','5','system','/system',null,b'0',b'0','dir:sys',null,null,null,'1614916695777');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('2','1','4','1','用户管理','用户管理','system/user/index','2','peoples','user',null,b'0',b'0','user:read',null,null,null,'1615786052463');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('3','1','3','1','菜单管理','菜单管理','system/menu/index','2','menu','menu',null,b'0',b'0','menu:read',null,null,null,null);
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('4','1','3','1','组织管理','组织管理','system/dept/index','3','dept','dept',null,b'0',b'0','dept:read',null,null,null,null);
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('5','1','3','1','角色管理','角色管理','system/role/index','4','role','role',b'0',b'0',b'0','role:read',null,null,'1614683852133','1614683852133');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('6','1','0','1','参数管理','参数管理','system/systemParamSettings/index','13','sys-tools','systemParamSettings',null,b'0',b'0','sysparam:read',null,null,null,'1615790294169');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('7','0','1','0','数据集','数据管理','Layout','3','dataset','/dataset',null,b'0',b'0','dir:data',null,null,null,'1619081474697');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('8','7','0','1','数据管理1','数据管理1','dataset/index','1','dataset','index',null,b'0',b'0','data:read',null,null,null,'1614916684821');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('9','0','1','0','视图','视图管理','Layout','2','chart','/chart',null,b'0',b'0','dir:chart',null,null,null,'1619081462127');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('10','9','0','1','视图1','视图1','chart/index','1','chart','index',null,b'0',b'0','chart:read',null,null,null,'1614915491036');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('12','3','0','2','创建菜单',null,null,'999',null,null,b'0',b'0',b'0','menu:add',null,null,'1614924617327','1614924617327');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('13','3','0','2','删除菜单',null,null,'999',null,null,b'0',b'0',b'0','menu:del',null,null,'1614924667808','1614924667808');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('14','3','0','2','编辑菜单',null,null,'999',null,null,b'0',b'0',b'0','menu:edit',null,null,'1614930734224','1614936429773');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('15','2','0','2','创建用户',null,null,'999',null,null,b'0',b'0',b'0','user:add',null,null,'1614930862373','1614930862373');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('16','2','0','2','删除用户',null,null,'999',null,null,b'0',b'0',b'0','user:del',null,null,'1614930903502','1614930903502');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('17','2','0','2','编辑用户',null,null,'999',null,null,b'0',b'0',b'0','user:edit',null,null,'1614930935529','1614930935529');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('18','4','0','2','创建组织',null,null,'999',null,null,b'0',b'0',b'0','dept:add',null,null,'1614930976297','1614930976297');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('19','4','0','2','删除组织',null,null,'999',null,null,b'0',b'0',b'0','dept:del',null,null,'1614930997130','1614930997130');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('20','4','0','2','编辑组织',null,null,'999',null,null,b'0',b'0',b'0','dept:edit',null,null,'1614931022967','1614931022967');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('21','5','0','2','创建角色',null,null,'999',null,null,b'0',b'0',b'0','role:add',null,null,'1614931069408','1614931069408');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('22','5','0','2','删除角色',null,null,'999',null,null,b'0',b'0',b'0','role:del',null,null,'1614931097720','1614931097720');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('23','5','0','2','编辑角色',null,null,'999',null,null,b'0',b'0',b'0','role:edit',null,null,'1614931124782','1614931124782');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('24','34','0','2','创建连接',null,null,'997',null,null,b'0',b'0',b'0','datasource:add',null,null,'1614931168956','1615783705537');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('25','34','0','2','删除连接',null,null,'999',null,null,b'0',b'0',b'0','datasource:del',null,null,'1614931205899','1614931205899');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('26','34','0','2','编辑连接',null,null,'999',null,null,b'0',b'0',b'0','datasource:edit',null,null,'1614931234105','1614931234105');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('27','34','0','2','校验连接',null,null,'999',null,null,b'0',b'0',b'0','datasource:validate',null,null,'1614931268578','1614931268578');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('28','2','0','2','修改密码',null,null,'999',null,null,b'0',b'0',b'0','user:editPwd',null,null,'1615275128262','1615275128262');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('29','0','1','0','仪表盘','仪表盘管理','Layout','1',null,'/panel',null,b'0',b'0','panel:read',null,null,null,'1619081454146');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('30','29','0','1','仪表盘1','仪表盘','panel/index','1',null,'index',b'0',b'0',b'0','panel:read',null,null,null,'1619081449067');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('33','0','1','0','数据源','数据源','Layout','4',null,'/datasource',b'0',b'0',b'0','dir:datasource',null,null,'1619083205537','1619083205537');
INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('34','33','4','1','数据源1','数据源1','system/datasource/index','1',null,'index',b'0',b'0',b'0','datasource:read',null,null,null,null);



DROP TABLE IF EXISTS `sys_user` ;

CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门名称',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `nick_name` varchar(255) DEFAULT NULL COMMENT '昵称',
  `gender` varchar(2) DEFAULT NULL COMMENT '性别',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `is_admin` bit(1) DEFAULT b'0' COMMENT '是否为admin账号',
  `enabled` bigint(20) DEFAULT NULL COMMENT '状态：1启用、0禁用',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新着',
  `pwd_reset_time` bigint(13) DEFAULT NULL COMMENT '修改密码的时间',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `UK_kpubos9gc2cvtkb0thktkbkes` (`email`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  UNIQUE KEY `uniq_username` (`username`),
  UNIQUE KEY `uniq_email` (`email`),
  KEY `FK5rwmryny6jthaaxkogownknqp` (`dept_id`) USING BTREE,
  KEY `inx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统用户';

INSERT INTO `sys_user` (`user_id`, `dept_id`, `username`, `nick_name`, `gender`, `phone`, `email`, `password`, `is_admin`, `enabled`, `create_by`, `update_by`, `pwd_reset_time`, `create_time`, `update_time`) VALUES ('4','0','admin','管理员','男',null,'admin@fit2cloud.com','e10adc3949ba59abbe56e057f20f883e',b'1','1',null,null,null,null,'1615184951534');
INSERT INTO `sys_user` (`user_id`, `dept_id`, `username`, `nick_name`, `gender`, `phone`, `email`, `password`, `is_admin`, `enabled`, `create_by`, `update_by`, `pwd_reset_time`, `create_time`, `update_time`) VALUES ('19','25','demo','demo','男',null,'demo@fit2cloud.com','e10adc3949ba59abbe56e057f20f883e',b'0','1',null,null,null,'1619086036234','1619086036234');



DROP TABLE IF EXISTS `sys_role` ;

CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(100) NOT NULL COMMENT '代码',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE KEY `uniq_name` (`name`),
  KEY `role_name_index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色表';

INSERT INTO `sys_role` (`role_id`, `code`, `name`, `description`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('3','admin','管理员',null,null,null,null,null);
INSERT INTO `sys_role` (`role_id`, `code`, `name`, `description`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('4','emp','普通员工',null,null,null,null,null);



DROP TABLE IF EXISTS `sys_roles_menus` ;

CREATE TABLE `sys_roles_menus` (
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`menu_id`,`role_id`) USING BTREE,
  KEY `FKcngg2qadojhi3a651a5adkvbq` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色菜单关联';

INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('1','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('2','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('3','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('4','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('5','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('6','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('7','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('8','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('9','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('10','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('11','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('14','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('15','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('16','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('17','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('18','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('19','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('20','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('21','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('22','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('23','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('24','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('25','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('26','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('27','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('28','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('29','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('30','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('31','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('32','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('33','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('34','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('101','3');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('29','4');
INSERT INTO `sys_roles_menus` (`menu_id`, `role_id`) VALUES ('30','4');



DROP TABLE IF EXISTS `sys_users_roles` ;

CREATE TABLE `sys_users_roles` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE,
  KEY `FKq4eq273l04bpu4efj0jd0jb98` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='用户角色关联';

INSERT INTO `sys_users_roles` (`user_id`, `role_id`) VALUES ('4','3');
INSERT INTO `sys_users_roles` (`user_id`, `role_id`) VALUES ('19','4');

