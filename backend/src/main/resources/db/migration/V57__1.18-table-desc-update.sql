ALTER TABLE `area_mapping` COMMENT = '国内省市区（县）映射表';
ALTER TABLE `area_mapping_global` COMMENT = '全球省市区（县）映射表';
ALTER TABLE `chart_group` COMMENT = '图表分组表';
ALTER TABLE `chart_view` COMMENT = '视图表';
ALTER TABLE `chart_view_cache` COMMENT = '视图缓存表';
ALTER TABLE `chart_view_field` COMMENT = '视图计算字段表';
ALTER TABLE `dataease_code_version` COMMENT = '数据初始化记录表';
ALTER TABLE `dataease_version` COMMENT = '数据库版本变更记录表';
ALTER TABLE `dataset_column_permissions` COMMENT = '数据集列权限配置表';
ALTER TABLE `dataset_group` COMMENT = '数据集分组表';
ALTER TABLE `dataset_row_permissions` COMMENT = '数据集行权限候选值配置表（包含系统变量的候选值）';
ALTER TABLE `dataset_row_permissions_tree` COMMENT = '数据集行权限配置表';
ALTER TABLE `dataset_sql_log` COMMENT = 'SQL数据集预览执行日志记录表';
ALTER TABLE `dataset_table` COMMENT = '数据集信息表';
ALTER TABLE `dataset_table_field` COMMENT = '数据集字段表';
ALTER TABLE `dataset_table_function` COMMENT = '数据集常用函数提示表';
ALTER TABLE `dataset_table_incremental_config` COMMENT = '定时同步数据集增量更新配置表';
ALTER TABLE `dataset_table_task` COMMENT = '定时同步数据集更新任务表';
ALTER TABLE `dataset_table_task_log` COMMENT = '定时同步数据集同步任务执行日志表';
ALTER TABLE `dataset_table_union` COMMENT = '关联数据集关联关系表';
ALTER TABLE `datasource` COMMENT = '数据源配置信息表';
ALTER TABLE `demo_alt_4a` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_4a_5a` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_5a` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_region` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_region` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_tourists_total` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_tourists_type` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_alt_tourist_attractions` COMMENT = '官方示例模板数据（阿勒泰地区旅游数据）';
ALTER TABLE `demo_domestic_epidemic` COMMENT = '官方示例模板数据（疫情数据）';
ALTER TABLE `demo_gdp_2021` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_gdp_by_city` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_gdp_by_city_top10` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_gdp_by_industry` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_gdp_district_top100` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_gdp_history` COMMENT = '官方示例模板数据（全国GDP）';
ALTER TABLE `demo_hntv_age` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_keywords` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_labels` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_media` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_messages` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_region` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_shows` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_hntv_topics` COMMENT = '官方示例模板数据（某节目相关数据分析）';
ALTER TABLE `demo_stny_carbon dioxide_emissions` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `demo_stny_carbon_emission_trend` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `demo_stny_disposable_energy` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `demo_stny_energy_consumption_proportion` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `demo_stny_energy_consumption_total` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `demo_stny_province_city_index` COMMENT = '官方示例模板数据（双碳及能源情况概览）';
ALTER TABLE `de_engine` COMMENT = '引擎设置表';
ALTER TABLE `file_content` COMMENT = '文件内容表';
ALTER TABLE `file_metadata` COMMENT = '文件基础信息表';
ALTER TABLE `license` COMMENT = '企业版许可证信息表';
ALTER TABLE `panel_app_template` COMMENT = '应用管理-应用表';
ALTER TABLE `panel_app_template_log` COMMENT = '应用管理-应用记录表';
ALTER TABLE `panel_group` COMMENT = '仪表板信息表';
ALTER TABLE `panel_group_extend` COMMENT = '仪表板和模板渲染的临时数据';
ALTER TABLE `panel_group_extend_data` COMMENT = '仪表板模板的结构数据';
ALTER TABLE `panel_link` COMMENT = '仪表板链接表';
ALTER TABLE `panel_link_jump` COMMENT = '仪表板视图跳转记录表';
ALTER TABLE `panel_link_jump_info` COMMENT = '仪表板视图跳转配置表';
ALTER TABLE `panel_link_jump_target_view_info` COMMENT = '仪表板视图跳转目标仪表板视图字段配置表';
ALTER TABLE `panel_link_mapping` COMMENT = '仪表板跳转映射表';
ALTER TABLE `panel_outer_params` COMMENT = '仪表板与外部参数关联关系表';
ALTER TABLE `panel_outer_params_info` COMMENT = '仪表板外部参数配置表';
ALTER TABLE `panel_outer_params_target_view_info` COMMENT = '仪表板外部参数联动视图字段信息表';
ALTER TABLE `panel_pdf_template` COMMENT = '仪表板 PDF 模板表';
ALTER TABLE `panel_subject` COMMENT = '仪表板主题表';
ALTER TABLE `panel_template` COMMENT = '仪表板模板表';
ALTER TABLE `panel_view` COMMENT = '仪表板视图表';
ALTER TABLE `panel_view_linkage` COMMENT = '仪表板视图联动关联关系表';
ALTER TABLE `panel_view_linkage_field` COMMENT = '仪表板视图联动关联字段关系表';
ALTER TABLE `panel_watermark` COMMENT = '仪表板水印设置表';
ALTER TABLE `plugin_sys_menu` COMMENT = '系统菜单表';
ALTER TABLE `qrtz_blob_triggers` COMMENT = '自定义触发器存储（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_calendars` COMMENT = 'Quartz日历（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_cron_triggers` COMMENT = 'CronTrigger存储（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_fired_triggers` COMMENT = '存储已经触发的trigger相关信息（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_job_details` COMMENT = '存储jobDetails信息（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_locks` COMMENT = 'Quartz锁表，为多个节点调度提供分布式锁（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_paused_trigger_grps` COMMENT = '存放暂停掉的触发器（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_scheduler_state` COMMENT = '存储所有节点的scheduler（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_simple_triggers` COMMENT = 'SimpleTrigger存储（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_simprop_triggers` COMMENT = '存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器（开源作业调度框架Quartz）';
ALTER TABLE `qrtz_triggers` COMMENT = '存储定义的trigger（开源作业调度框架Quartz）';
ALTER TABLE `schedule` COMMENT = '定时任务表';
ALTER TABLE `system_parameter` COMMENT = '系统内置参数表';
ALTER TABLE `sys_auth` COMMENT = '权限授权表';
ALTER TABLE `sys_auth_detail` COMMENT = '权限授权明细表';
ALTER TABLE `sys_background_image` COMMENT = '视图内置背景图记录表';
ALTER TABLE `sys_external_token` COMMENT = '外部认证 tocken 记录表';
ALTER TABLE `sys_log` COMMENT = '系统操作日志表';
ALTER TABLE `sys_login_limit` COMMENT = '用户登陆失败记录表，用于做登陆限制';
ALTER TABLE `sys_param_assist` COMMENT = '页脚内容配置表';
ALTER TABLE `sys_startup_job` COMMENT = '系统启动任务';
ALTER TABLE `sys_task` COMMENT = '定时报告任务表';
ALTER TABLE `sys_task_email` COMMENT = '定时报告任务配置表';
ALTER TABLE `sys_task_instance` COMMENT = '定时报告执行记录表';
ALTER TABLE `sys_theme` COMMENT = '系统外观主题配置表';
ALTER TABLE `sys_theme_item` COMMENT = '系统外观主题内置颜色配置表';
ALTER TABLE `sys_user_assist` COMMENT = '用户关联信息表';
ALTER TABLE `task_instance` COMMENT = '系统内置定时任务实例状态信息表（数据源定时状态检查等）';



ALTER TABLE `chart_view_field`
    MODIFY COLUMN `origin_name` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '字段表达式' AFTER `chart_id`,
    MODIFY COLUMN `size` int(11) NULL DEFAULT NULL COMMENT '字段长度（允许为空，默认0）' AFTER `type`,
    MODIFY COLUMN `de_extract_type` int(10) NOT NULL COMMENT '字段类型' AFTER `de_type_format`;

ALTER TABLE `dataease_code_version`
    MODIFY COLUMN `installed_rank` int(11) NOT NULL COMMENT '执行顺序（主键）' FIRST,
    MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述' AFTER `installed_rank`,
    MODIFY COLUMN `installed_on` timestamp(0) NULL DEFAULT NULL COMMENT '执行时间' AFTER `description`,
    MODIFY COLUMN `success` tinyint(1) NOT NULL COMMENT '状态（1-成功，0-失败）' AFTER `installed_on`;

ALTER TABLE `dataease_version`
    MODIFY COLUMN `installed_rank` int(11) NOT NULL COMMENT '执行顺序（主键）' FIRST,
    MODIFY COLUMN `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版本' AFTER `installed_rank`,
    MODIFY COLUMN `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述' AFTER `version`,
    MODIFY COLUMN `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型' AFTER `description`,
    MODIFY COLUMN `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '脚本名称' AFTER `type`,
    MODIFY COLUMN `checksum` int(11) NULL DEFAULT NULL COMMENT '脚本内容一致性校验码' AFTER `script`,
    MODIFY COLUMN `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行用户' AFTER `checksum`,
    MODIFY COLUMN `installed_on` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '执行时间' AFTER `installed_by`,
    MODIFY COLUMN `execution_time` int(11) NOT NULL COMMENT '执行时长' AFTER `installed_on`,
    MODIFY COLUMN `success` tinyint(1) NOT NULL COMMENT '状态（1-成功，0-失败）' AFTER `execution_time`;

ALTER TABLE `dataset_column_permissions`
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '更新时间' AFTER `white_list_user`;

ALTER TABLE `dataset_row_permissions`
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '更新时间' AFTER `enum_check_field`;

ALTER TABLE `dataset_row_permissions_tree`
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '更新时间' AFTER `white_list_dept`;

ALTER TABLE `dataset_table`
    MODIFY COLUMN `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `qrtz_instance` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Quartz 实例 ID' AFTER `create_time`,
    MODIFY COLUMN `sync_status` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '同步状态（定时同步数据集）' AFTER `qrtz_instance`,
    MODIFY COLUMN `last_update_time` bigint(13) NULL DEFAULT 0 COMMENT '最后一次更新时间' AFTER `sync_status`,
    MODIFY COLUMN `sql_variable_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'SQL变量' AFTER `last_update_time`;

ALTER TABLE `dataset_table_field`
    MODIFY COLUMN `size` int(11) NULL DEFAULT NULL COMMENT '字段长度（允许为空，默认0）' AFTER `type`,
    MODIFY COLUMN `de_extract_type` int(10) NOT NULL COMMENT '原字段类型：0-STRING，1-TIME，2-INT，3-FLOAT，4-BOOL，5-LOCATION，6-BINARY' AFTER `de_type_format`,
    MODIFY COLUMN `date_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日期格式' AFTER `accuracy`;

ALTER TABLE `dataset_table_task`
    MODIFY COLUMN `extra_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '简单重复时的执行频率' AFTER `last_exec_status`;

ALTER TABLE `dataset_table_task_log`
    MODIFY COLUMN `trigger_type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行频率的类型（Cron-表达式设定，Custom-自定义）' AFTER `create_time`;

ALTER TABLE `de_driver_details`
    MODIFY COLUMN `trans_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '替换后的 jar 包名称' AFTER `driver_class`,
    MODIFY COLUMN `is_trans_name` tinyint(1) NULL DEFAULT NULL COMMENT '是否将上传 jar 包替换了名称（1-是，0-否）' AFTER `trans_name`;

ALTER TABLE `license`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `panel_app_template`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `level` int(8) NULL DEFAULT NULL COMMENT '层级（0-应用分类，1-应用）' AFTER `node_type`,
    MODIFY COLUMN `icon` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '应用分类的封面图标' AFTER `version`,
    MODIFY COLUMN `link_jumps` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设置了跳转的仪表板视图信息' AFTER `datasource_info`,
    MODIFY COLUMN `link_jump_infos` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '仪表板视图跳转配置信息' AFTER `link_jumps`,
    MODIFY COLUMN `linkages` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '仪表板视图联动关联关系表' AFTER `link_jump_infos`,
    MODIFY COLUMN `linkage_fields` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '仪表板视图联动关联字段关系表' AFTER `linkages`,
    MODIFY COLUMN `snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '缩略图' AFTER `linkage_fields`,
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '修改时间' AFTER `snapshot`,
    MODIFY COLUMN `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人' AFTER `update_time`,
    MODIFY COLUMN `create_time` bigint(13) NULL DEFAULT NULL COMMENT '创建时间' AFTER `update_user`,
    MODIFY COLUMN `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人' AFTER `create_time`;

ALTER TABLE `panel_app_template_log`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `is_success`;

ALTER TABLE `panel_design`
    MODIFY COLUMN `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `panel_group`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `extend1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展字段1' AFTER `source`,
    MODIFY COLUMN `extend2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展字段2' AFTER `extend1`,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `extend2`;

ALTER TABLE `panel_group_extend`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `panel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仪表板ID' AFTER `id`;

ALTER TABLE `panel_group_extend_data`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `panel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仪表板ID' AFTER `id`,
    MODIFY COLUMN `view_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视图ID' AFTER `panel_id`,
    MODIFY COLUMN `view_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '视图详情' AFTER `view_id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `view_details`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_link`
    MODIFY COLUMN `user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建用户ID' AFTER `over_time`;

ALTER TABLE `panel_link_jump`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `checked`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_link_jump_info`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `link_jump_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'panel_link_jump 表的 ID' AFTER `id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `attach_params`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_link_jump_target_view_info`
    MODIFY COLUMN `target_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `link_jump_info_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'panel_link_jump_info 表的 ID' AFTER `target_id`,
    MODIFY COLUMN `target_view_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标视图ID' AFTER `link_jump_info_id`,
    MODIFY COLUMN `target_field_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标字段ID' AFTER `target_view_id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `target_field_id`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_link_mapping`
    MODIFY COLUMN `user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建用户ID' AFTER `resource_id`;

ALTER TABLE `panel_outer_params`
    MODIFY COLUMN `params_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `panel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仪表板ID' AFTER `params_id`,
    MODIFY COLUMN `checked` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用外部参数标识（1-是，0-否）' AFTER `panel_id`,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `checked`,
    MODIFY COLUMN `copy_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `remark`,
    MODIFY COLUMN `copy_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_outer_params_info`
    MODIFY COLUMN `params_info_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `params_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'panel_outer_params 表的 ID' AFTER `params_info_id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `checked`,
    MODIFY COLUMN `copy_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_outer_params_target_view_info`
    MODIFY COLUMN `target_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `params_info_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'panel_outer_params_info 表的 ID' AFTER `target_id`,
    MODIFY COLUMN `target_view_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联动视图ID' AFTER `params_info_id`,
    MODIFY COLUMN `target_field_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联动字段ID' AFTER `target_view_id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `target_field_id`,
    MODIFY COLUMN `copy_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_subject`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `panel_template`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `panel_view`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from_panel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源仪表板' AFTER `position`,
    MODIFY COLUMN `copy_from_view` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源视图' AFTER `copy_from_panel`;

ALTER TABLE `panel_view_linkage`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `panel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仪表板ID' AFTER `id`,
    MODIFY COLUMN `ext1` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展字段1' AFTER `linkage_active`,
    MODIFY COLUMN `ext2` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展字段2' AFTER `ext1`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `ext2`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_view_linkage_field`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `update_time`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `panel_watermark`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人' AFTER `setting_content`,
    MODIFY COLUMN `create_time` bigint(13) NULL DEFAULT NULL COMMENT '创建时间' AFTER `create_by`;

ALTER TABLE `plugin_sys_menu`
    MODIFY COLUMN `menu_id` bigint(8) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `pid` bigint(8) NULL DEFAULT NULL COMMENT '父级ID' AFTER `menu_id`,
    MODIFY COLUMN `sub_count` int(8) NULL DEFAULT NULL COMMENT '下级数量' AFTER `pid`,
    MODIFY COLUMN `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型' AFTER `sub_count`,
    MODIFY COLUMN `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单标题' AFTER `type`,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称' AFTER `title`,
    MODIFY COLUMN `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件' AFTER `name`,
    MODIFY COLUMN `menu_sort` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '排序' AFTER `component`,
    MODIFY COLUMN `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标' AFTER `menu_sort`,
    MODIFY COLUMN `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径' AFTER `icon`,
    MODIFY COLUMN `i_frame` tinyint(1) NULL DEFAULT NULL COMMENT '是否参与授权（0-参与 1-不参与）' AFTER `path`,
    MODIFY COLUMN `cache` tinyint(1) NULL DEFAULT NULL COMMENT '缓存' AFTER `i_frame`,
    MODIFY COLUMN `hidden` tinyint(1) NULL DEFAULT NULL COMMENT '是否隐藏（1-是，0-否）' AFTER `cache`,
    MODIFY COLUMN `permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识' AFTER `hidden`,
    MODIFY COLUMN `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人' AFTER `permission`,
    MODIFY COLUMN `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人' AFTER `create_by`,
    MODIFY COLUMN `create_time` bigint(13) NULL DEFAULT NULL COMMENT '创建时间' AFTER `update_by`,
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    MODIFY COLUMN `no_layout` tinyint(1) NULL DEFAULT NULL COMMENT '是否独立窗口页面' AFTER `update_time`;

ALTER TABLE `sys_auth`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `sys_auth_detail`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `auth_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'sys_auth 表的 ID' AFTER `id`,
    MODIFY COLUMN `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人' AFTER `remark`,
    MODIFY COLUMN `create_time` bigint(13) NULL DEFAULT NULL COMMENT '创建时间' AFTER `create_user`,
    MODIFY COLUMN `update_time` bigint(13) NULL DEFAULT NULL COMMENT '更新时间' AFTER `create_time`;

ALTER TABLE `sys_background_image`
    MODIFY COLUMN `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `classification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名' AFTER `name`,
    MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容' AFTER `classification`,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `content`,
    MODIFY COLUMN `sort` int(8) NULL DEFAULT NULL COMMENT '排序' AFTER `remark`,
    MODIFY COLUMN `upload_time` bigint(13) NULL DEFAULT NULL COMMENT '上传时间' AFTER `sort`,
    MODIFY COLUMN `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在目录地址' AFTER `upload_time`,
    MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图片url' AFTER `base_url`;

ALTER TABLE `sys_login_limit`
    MODIFY COLUMN `login_type` int(8) NOT NULL COMMENT '登陆类型（0-失败）' FIRST,
    MODIFY COLUMN `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名' AFTER `login_type`,
    MODIFY COLUMN `record_time` bigint(13) NOT NULL COMMENT '记录时间' AFTER `username`;

ALTER TABLE `sys_menu`
    MODIFY COLUMN `i_frame` bit(1) NULL DEFAULT NULL COMMENT '是否参与授权（0-参与 1-不参与）' AFTER `path`;

ALTER TABLE `sys_param_assist`
    MODIFY COLUMN `id` bigint(21) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

ALTER TABLE `sys_task_email`
    MODIFY COLUMN `view_data_range` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'view' COMMENT '视图数据范围（view-展示数据，data-全部数据）' AFTER `reci_users`;

ALTER TABLE `sys_theme`
    MODIFY COLUMN `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态（1-正在使用，0-未使用）' AFTER `name`,
    MODIFY COLUMN `senior` tinyint(1) NULL DEFAULT NULL COMMENT '高级标识' AFTER `status`;

ALTER TABLE `sys_user_assist`
    MODIFY COLUMN `larksuite_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '国际飞书账号' AFTER `lark_id`;
