CREATE TABLE IF NOT EXISTS `file_content` (
    `file_id` varchar(64)  NOT NULL COMMENT 'File ID',
    `file`    longblob COMMENT 'File content',
    PRIMARY KEY (`file_id`)
    )
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `file_metadata` (
    `id`          varchar(64) NOT NULL COMMENT 'File ID',
    `name`        varchar(64) NOT NULL COMMENT 'File name',
    `type`        varchar(64) DEFAULT NULL COMMENT 'File type',
    `size`        bigint(13)  NOT NULL COMMENT 'File size',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `system_parameter` (
    `param_key`   varchar(64) CHARACTER SET utf8mb4 NOT NULL COMMENT 'Parameter name',
    `param_value` varchar(255)                               DEFAULT NULL COMMENT 'Parameter value',
    `type`        varchar(100)                      NOT NULL DEFAULT 'text' COMMENT 'Parameter type',
    `sort`        int(5)                                     DEFAULT NULL COMMENT 'Sort',
    PRIMARY KEY (`param_key`)
)ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_role` (
    `id`          varchar(50) NOT NULL COMMENT 'ID of user''s role info',
    `user_id`     varchar(50) NOT NULL COMMENT 'User ID of this user-role info',
    `role_id`     varchar(50) NOT NULL COMMENT 'Role ID of this user-role info',
    `source_id`   varchar(50) DEFAULT NULL COMMENT 'The (system|organization|workspace) ID of this user-role info',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `datasource`;
CREATE TABLE `datasource` (
  `id`          varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
  `name`     varchar(50) NOT NULL COMMENT '名称',
  `desc`  varchar(50) COMMENT '描述',
  `type`  varchar(50) NOT NULL COMMENT '类型',
  `configuration`  longtext NOT NULL COMMENT '详细信息',
  `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  `create_by` varchar(50)  COMMENT '创建人ID',
  PRIMARY KEY (`id`)
)ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_group`;
CREATE TABLE IF NOT EXISTS `dataset_group` (
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `name` varchar(64) NOT NULL COMMENT '名称',
    `pid` varchar(50) COMMENT '父级ID',
    `level` int(10) COMMENT '当前分组处于第几级',
    `type` varchar(50) COMMENT 'group or scene',
    `create_by` varchar(50)  COMMENT '创建人ID',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '上级部门',
  `sub_count` int(5) DEFAULT '0' COMMENT '子部门数目',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `dept_sort` int(5) DEFAULT '999' COMMENT '排序',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  KEY `inx_pid` (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='组织机构';

DROP TABLE IF EXISTS `sys_menu`;
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
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统菜单';

DROP TABLE IF EXISTS `sys_user`;
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
  `language` varchar(20) DEFAULT NULL COMMENT '语言',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `UK_kpubos9gc2cvtkb0thktkbkes` (`email`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  UNIQUE KEY `uniq_username` (`username`),
  UNIQUE KEY `uniq_email` (`email`),
  KEY `FK5rwmryny6jthaaxkogownknqp` (`dept_id`) USING BTREE,
  KEY `inx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE KEY `uniq_name` (`name`),
  KEY `role_name_index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色表';

DROP TABLE IF EXISTS `sys_roles_menus`;
CREATE TABLE `sys_roles_menus` (
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`menu_id`,`role_id`) USING BTREE,
  KEY `FKcngg2qadojhi3a651a5adkvbq` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色菜单关联';

DROP TABLE IF EXISTS `sys_users_roles`;
CREATE TABLE `sys_users_roles` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE,
  KEY `FKq4eq273l04bpu4efj0jd0jb98` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='用户角色关联';

DROP TABLE IF EXISTS `my_plugin`;
CREATE TABLE `my_plugin` (
  `plugin_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '插件名称',
  `store` varchar(255) DEFAULT NULL COMMENT '商家',
  `free` tinyint(1) DEFAULT '0' COMMENT '是否免费',
  `cost` int(10) DEFAULT NULL COMMENT '费用',
  `category` varchar(255) DEFAULT NULL COMMENT '列别',
  `descript` varchar(255) DEFAULT NULL COMMENT '描述',
  `version` varchar(255) DEFAULT NULL COMMENT '版本号',
  `install_type` int(4) DEFAULT NULL COMMENT '安装类型',
  `creator` varchar(255) DEFAULT NULL COMMENT '开发者',
  `load_mybatis` tinyint(1) DEFAULT '0' COMMENT '是否需要加载mybatis',
  `release_time` bigint(13) DEFAULT NULL COMMENT '发布时间',
  `install_time` bigint(13) DEFAULT NULL COMMENT '安装时间',
  `module_name` varchar(255) DEFAULT NULL COMMENT 'jar包名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  PRIMARY KEY (`plugin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='插件表';

DROP TABLE IF EXISTS `license`;
CREATE TABLE `license` (
   `id` varchar(50) NOT NULL,
   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   `license` longtext COMMENT 'license',
   `f2c_license` longtext COMMENT 'F2C License',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dataset_table`;
CREATE TABLE IF NOT EXISTS `dataset_table`
(
    `id`             varchar(50) NOT NULL COMMENT 'ID',
    `name`           varchar(64) NOT NULL COMMENT '表名称',
    `scene_id`       varchar(50) NOT NULL COMMENT '场景ID',
    `data_source_id` varchar(50) COMMENT '数据源ID',
    `type`           varchar(50) COMMENT 'db,sql,excel,custom',
    `mode`           int(10) DEFAULT 0 COMMENT '连接模式：0-直连，1-定时同步',
    `info`           longtext COMMENT '表原始信息',
    `create_by`      varchar(50) COMMENT '创建人ID',
    `create_time`    bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_field`;
CREATE TABLE IF NOT EXISTS `dataset_table_field` (
   `id` varchar(50) NOT NULL COMMENT 'ID',
   `table_id` varchar(50) NOT NULL COMMENT '表ID',
   `origin_name` varchar(255) NOT NULL COMMENT '原始名',
   `name` varchar(255) NOT NULL COMMENT '字段名',
   `dataease_name` varchar(255) NOT NULL COMMENT '字段名',
   `type` varchar(50) NOT NULL COMMENT '原始字段类型',
   `size` int(11) DEFAULT NULL,
   `de_type` int(10) NOT NULL COMMENT 'dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值...',
   `de_extract_type` int(10) NOT NULL,
   `checked` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中',
   `column_index` int(10) NOT NULL COMMENT '列位置',
   `last_sync_time` bigint(13) DEFAULT NULL COMMENT '同步时间',
   PRIMARY KEY (`id`),
   KEY `IDX_TABLE_ID` (`table_id`),
   KEY `IDX_DE_TYPE` (`de_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dataset_table_task`;
CREATE TABLE IF NOT EXISTS `dataset_table_task`
(
    `id`          varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`    varchar(50)  NOT NULL COMMENT '表ID',
    `name`        varchar(255) NOT NULL COMMENT '任务名称',
    `type`        varchar(50)  NOT NULL COMMENT '更新方式：0-全量更新 1-增量更新',
    `start_time`  bigint(13) COMMENT '开始时间',
    `rate`        varchar(50)  NOT NULL COMMENT '执行频率：0 一次性 1 cron',
    `cron`        varchar(255) COMMENT 'cron表达式',
    `end`         varchar(50)  NOT NULL COMMENT '结束限制 0 无限制 1 设定结束时间',
    `end_time`    bigint(13) COMMENT '结束时间',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `IDX_TABLE_ID` (`table_id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_task_log`;
CREATE TABLE IF NOT EXISTS `dataset_table_task_log` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `table_id` varchar(50) NOT NULL COMMENT '表ID',
  `task_id` varchar(50) DEFAULT NULL COMMENT '任务ID',
  `start_time` bigint(13) DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint(13) DEFAULT NULL COMMENT '结束时间',
  `status` varchar(50) NOT NULL COMMENT '执行状态',
  `info` longtext COMMENT '错误信息',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `IDX_TABLE_ID` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chart start
DROP TABLE IF EXISTS `chart_group`;
CREATE TABLE IF NOT EXISTS `chart_group`
(
    `id`          varchar(50) NOT NULL COMMENT 'ID',
    `name`        varchar(64) NOT NULL COMMENT '名称',
    `pid`         varchar(50) COMMENT '父级ID',
    `level`       int(10) COMMENT '当前分组处于第几级',
    `type`        varchar(50) COMMENT 'group or scene',
    `create_by`   varchar(50) COMMENT '创建人ID',
    `create_time` bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `chart_view`;
CREATE TABLE `chart_view` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `scene_id` varchar(50) NOT NULL COMMENT '场景ID',
  `table_id` varchar(50) NOT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `title` varchar(50) DEFAULT NULL COMMENT 'EChart标题',
  `x_axis` longtext COMMENT '横轴field',
  `y_axis` longtext COMMENT '纵轴field',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_style` longtext COMMENT '组件样式',
  `custom_filter` longtext COMMENT '结果过滤',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 视图',
  PRIMARY KEY (`id`),
  KEY `IDX_TABLE_ID` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chart end

DROP TABLE IF EXISTS `panel_design`;
CREATE TABLE `panel_design` (
    `id` varchar(100) NOT NULL,
    `panel_id` varchar(100) DEFAULT NULL COMMENT 'panel id',
    `component_id` varchar(100) DEFAULT NULL COMMENT '组件 id',
    `component_style` varchar(2000) DEFAULT NULL COMMENT '组件样式 样式',
    `component_position` varchar(2000) DEFAULT NULL COMMENT '组件样式 样式定位',
    `component_type` varchar(255) DEFAULT NULL COMMENT '组件 类型 view 视图 public 公共组件',
    `component_details` varchar(2000) DEFAULT NULL COMMENT '组件明细',
    `update_time` bigint(13) DEFAULT NULL COMMENT '修改时间',
    `update_person` varchar(255) DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='仪表板和组件的关联关系 组件分为普通视图和系统组件';

DROP TABLE IF EXISTS `panel_group`;
CREATE TABLE `panel_group` (
  `id` varchar(50) NOT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
  `level` int(10) DEFAULT NULL COMMENT '层级',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `panel_type` varchar(255) NOT NULL COMMENT '仪表板类型 system 系统内置 self 用户自建 ',
  `panel_style` longtext NOT NULL COMMENT 'panel 样式',
  `panel_data` longtext COMMENT 'panel 数据',
  `source` varchar(255) DEFAULT NULL COMMENT '数据来源 导入 或者 其他仪表板另存',
  `extend1` varchar(255) DEFAULT NULL,
  `extend2` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `panel_view`;
CREATE TABLE `panel_view` (
  `id` varchar(50) NOT NULL,
  `panel_group_id` varchar(50) DEFAULT NULL COMMENT 'panel_group_id',
  `content` blob COMMENT '内容',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
  `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `panel_store`;
CREATE TABLE `panel_store` (
   `store_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
   `panel_group_id` varchar(50) NOT NULL COMMENT '仪表板ID',
   `user_id` bigint(20) NOT NULL COMMENT '用户ID',
   `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
   PRIMARY KEY (`store_id`) USING BTREE,
   KEY `UK_store_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='仪表板收藏';


DROP TABLE IF EXISTS `panel_template`;
CREATE TABLE `panel_template` (
  `id` varchar(50) NOT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
  `level` int(10) DEFAULT NULL COMMENT '层级',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `snapshot` longtext COMMENT '缩略图',
  `template_type` varchar(255) DEFAULT NULL COMMENT '仪表板类型 system 系统内置 self 用户自建 ',
  `template_style` longtext COMMENT 'template 样式',
  `template_data` longtext COMMENT 'template 数据',
  `dynamic_data` longtext COMMENT '预存数据',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `panel_share`;
CREATE TABLE `panel_share` (
       `share_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分享ID',
       `panel_group_id` varchar(50) DEFAULT NULL COMMENT '仪表板ID',
       `target_id` bigint(20) DEFAULT NULL COMMENT '目标ID',
       `create_time` bigint(13) DEFAULT NULL COMMENT '创建日期',
       `type` int(8) DEFAULT NULL COMMENT '类型0:user,1:role,2dept',
       PRIMARY KEY (`share_id`) USING BTREE,
       KEY `UK_share_arget_id` (`target_id`) ,
       KEY `UK_share_panel_group_id` (`panel_group_id`) ,
       KEY `UK_share_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='仪表板分享';

DROP TABLE IF EXISTS `panel_link`;
CREATE TABLE `panel_link` (
      `resource_id` varchar(50) NOT NULL COMMENT '资源ID',
      `valid` tinyint(1) default 0 COMMENT '启用链接',
      `enable_pwd` tinyint(1) default 0 COMMENT '启用密码',
      `pwd` varchar(255) DEFAULT NULL  COMMENT '密码',
      PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='仪表板链接';

DROP TABLE IF EXISTS `panel_template`;
CREATE TABLE `panel_template` (
      `id` varchar(50) NOT NULL,
      `name` varchar(255) DEFAULT NULL COMMENT '名称',
      `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
      `level` int(10) DEFAULT NULL COMMENT '层级',
      `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
      `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
      `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
      `snapshot` longtext COMMENT '缩略图',
      `template_type` varchar(255) DEFAULT NULL COMMENT '仪表板类型 system 系统内置 self 用户自建 ',
      `template_style` longtext COMMENT 'template 样式',
      `template_data` longtext COMMENT 'template 数据',
      `dynamic_data` longtext COMMENT '预存数据',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `panel_subject`;
CREATE TABLE `panel_subject` (
     `id` varchar(50) NOT NULL,
     `name` varchar(255) DEFAULT NULL COMMENT '主题名称',
     `type` varchar(255) DEFAULT NULL COMMENT '主题类型 system 系统主题，self 自定义主题',
     `details` longtext COMMENT '主题内容',
     `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
     `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
     `update_time` bigint(13) DEFAULT NULL COMMENT '更新时间',
     `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dataset_table_incremental_config`;
CREATE TABLE IF NOT EXISTS `dataset_table_incremental_config`
(
    `id`          varchar(50)  NOT NULL COMMENT 'ID',
    `table_id`    varchar(50)  NOT NULL COMMENT '表ID',
    `incremental_delete`  longtext COMMENT '详细信息',
    `incremental_add`  longtext COMMENT '详细信息',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `dataset_table_union`;
CREATE TABLE IF NOT EXISTS `dataset_table_union`
(
    `id`                    varchar(50) NOT NULL COMMENT 'ID',
    `source_table_id`       varchar(50) COMMENT '关联表ID',
    `source_table_field_id` varchar(50) COMMENT '关联字段ID',
    `source_union_relation` varchar(50) COMMENT '关联关系,1:1、1:N、N:1',
    `target_table_id`       varchar(50) COMMENT '被关联表ID',
    `target_table_field_id` varchar(50) COMMENT '被关联字段ID',
    `target_union_relation` varchar(50) COMMENT '被关联关系,1:1、1:N、N:1',
    `create_by`             varchar(50) COMMENT '创建人ID',
    `create_time`           bigint(13) COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `license`;
CREATE TABLE `license` (
   `id` varchar(50) NOT NULL,
   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   `license` longtext COMMENT 'license',
   `f2c_license` longtext COMMENT 'F2C License',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `plugin_sys_menu`;
CREATE TABLE `plugin_sys_menu` (
  `menu_id` bigint(8) NOT NULL,
  `pid` bigint(8) DEFAULT NULL,
  `sub_count` int(8) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `component` varchar(255) DEFAULT NULL,
  `menu_sort` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `i_frame` tinyint(1) DEFAULT NULL,
  `cache` tinyint(1) DEFAULT NULL,
  `hidden` tinyint(1) DEFAULT NULL,
  `permission` varchar(255) DEFAULT NULL,
  `create_by` varchar(255) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `create_time` bigint(13) DEFAULT NULL,
  `update_time` bigint(13) DEFAULT NULL,
  `no_layout` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
