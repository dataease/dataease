COMMENT ON TABLE `QRTZ_BLOB_TRIGGERS`
    IS '自定义触发器存储（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_CALENDARS`
    IS 'Quartz日历（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_CRON_TRIGGERS`
    IS 'CronTrigger存储（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_FIRED_TRIGGERS`
    IS '存储已经触发的trigger相关信息（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_JOB_DETAILS`
    IS '存储jobDetails信息（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_LOCKS`
    IS 'Quartz锁表，为多个节点调度提供分布式锁（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
    IS '存放暂停掉的触发器（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_SCHEDULER_STATE`
    IS '存储所有节点的scheduler（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_SIMPLE_TRIGGERS`
    IS 'SimpleTrigger存储（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_SIMPROP_TRIGGERS`
    IS '存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器（开源作业调度框架Quartz）';
COMMENT ON TABLE `QRTZ_TRIGGERS`
    IS '存储定义的trigger（开源作业调度框架Quartz）';
COMMENT ON TABLE `area`
    IS '地图区域表';
COMMENT ON TABLE `core_area_custom`
    IS '自定义地图区域信息表';
COMMENT ON TABLE `core_chart_view`
    IS '组件图表表';
COMMENT ON TABLE `core_dataset_group`
    IS '数据集分组表';
COMMENT ON TABLE `core_dataset_table`
    IS 'table数据集';
COMMENT ON TABLE `core_dataset_table_field`
    IS 'table数据集表字段';
COMMENT ON TABLE `core_dataset_table_sql_log`
    IS 'table数据集查询sql日志';
COMMENT ON TABLE `core_datasource`
    IS '数据源表';
COMMENT ON TABLE `core_datasource_task`
    IS '数据源定时同步任务';
COMMENT ON TABLE `core_datasource_task_log`
    IS '数据源定时同步任务执行日志';
COMMENT ON TABLE `core_de_engine`
    IS '数据引擎';
COMMENT ON TABLE `core_driver`
    IS '驱动';
COMMENT ON TABLE `core_driver_jar`
    IS '驱动详情';
COMMENT ON TABLE `core_menu`
    IS '路由菜单';
COMMENT ON TABLE `core_opt_recent`
    IS '可视化资源表';
COMMENT ON TABLE `core_rsa`
    IS 'rsa 密钥表';
COMMENT ON TABLE `core_store`
    IS '用户收藏表';
COMMENT ON TABLE `core_sys_setting`
    IS '系统设置表';
COMMENT ON TABLE `data_visualization_info`
    IS '可视化大屏信息表';

COMMENT ON TABLE `visualization_background`
    IS '边框背景表';
COMMENT ON TABLE `visualization_background_image`
    IS '背景图';
COMMENT ON TABLE `visualization_link_jump`
    IS '跳转记录表';
COMMENT ON TABLE `visualization_link_jump_info`
    IS '跳转配置表';
COMMENT ON TABLE `visualization_link_jump_target_view_info`
    IS '跳转目标仪表板图表字段配置表';
COMMENT ON TABLE `visualization_linkage`
    IS '联动记录表';
COMMENT ON TABLE `visualization_linkage_field`
    IS '联动字段';
COMMENT ON TABLE `visualization_subject`
    IS '主题表';
COMMENT ON TABLE `visualization_template_extend_data`
    IS '模板图表明细信息表';

ALTER TABLE `core_dataset_group`
    MODIFY COLUMN `qrtz_instance` varchar(1024) NULL DEFAULT NULL COMMENT 'Quartz 实例 ID';

ALTER TABLE `core_dataset_table_field`
    MODIFY COLUMN `size` int(0) NULL DEFAULT NULL COMMENT '字段长度（允许为空，默认0）';
ALTER TABLE `core_dataset_table_field`
    MODIFY COLUMN `date_format` varchar(255) NULL DEFAULT NULL COMMENT '时间字段类型';

ALTER TABLE `core_datasource_task`
    MODIFY COLUMN `extra_data` longtext NULL COMMENT '额外数据';

ALTER TABLE `core_datasource_task_log`
    MODIFY COLUMN `trigger_type` varchar(45) NULL DEFAULT NULL COMMENT '更新频率类型';

ALTER TABLE `core_driver_jar`
    MODIFY COLUMN `trans_name` varchar(255) NULL DEFAULT NULL COMMENT '替换后的 jar 包名称';
ALTER TABLE `core_driver_jar`
    MODIFY COLUMN `is_trans_name` tinyint(1) NULL DEFAULT NULL COMMENT '是否将上传 jar 包替换了名称（1-是，0-否）';

ALTER TABLE `core_rsa`
    MODIFY COLUMN `aes_key` text NOT NULL COMMENT 'AES 加密算法的 key';

ALTER TABLE `data_visualization_info`
    MODIFY COLUMN `id` varchar(50) NOT NULL COMMENT '主键';

ALTER TABLE `visualization_background`
    MODIFY COLUMN `id` varchar(64) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `name` varchar(255) NULL DEFAULT NULL COMMENT '名称';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `classification` varchar(255) NOT NULL COMMENT '分类名';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `content` longtext NULL COMMENT '内容';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `base_url` varchar(255) NULL DEFAULT NULL COMMENT '所在目录地址';
ALTER TABLE `visualization_background`
    MODIFY COLUMN `url` varchar(255) NULL DEFAULT NULL COMMENT '图片url';

ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `id` varchar(64) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `name` varchar(255) NULL DEFAULT NULL COMMENT '名称';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `classification` varchar(255) NOT NULL COMMENT '分类名';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `content` longtext NULL COMMENT '内容';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `base_url` varchar(255) NULL DEFAULT NULL COMMENT '所在目录地址';
ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `url` varchar(255) NULL DEFAULT NULL COMMENT '图片url';

ALTER TABLE `visualization_link_jump`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_link_jump`
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_link_jump`
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `visualization_link_jump_info`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_link_jump_info`
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_link_jump_info`
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `target_id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `link_jump_info_id` bigint(0) NULL DEFAULT NULL COMMENT 'visualization_link_jump_info 表的 ID';
ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `target_view_id` bigint(0) NULL DEFAULT NULL COMMENT '目标图表ID';
ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `target_field_id` bigint(0) NULL DEFAULT NULL COMMENT '目标字段ID';
ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '联动大屏/仪表板ID';
ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `ext1` varchar(2000) NULL DEFAULT NULL COMMENT '扩展字段1';
ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `ext2` varchar(2000) NULL DEFAULT NULL COMMENT '扩展字段2';
ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `visualization_linkage_field`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_linkage_field`
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_linkage_field`
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `visualization_subject`
    MODIFY COLUMN `create_num` int(0) NOT NULL DEFAULT 0 COMMENT '创建序号';

ALTER TABLE `visualization_template_category`
    MODIFY COLUMN `template_type` varchar(255) NULL DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建';

ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键';
ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '模板ID';
ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `view_id` bigint(0) NULL DEFAULT NULL COMMENT '图表ID';
ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `view_details` longtext NULL COMMENT '图表详情';
ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `copy_from` varchar(255) NULL DEFAULT NULL COMMENT '复制来源';
ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `copy_id` varchar(255) NULL DEFAULT NULL COMMENT '复制来源ID';

ALTER TABLE `core_opt_recent`
    MODIFY COLUMN `resource_type` int(0) NOT NULL COMMENT '资源类型 1-可视化资源 2-仪表板 3-数据大屏 4-数据集 5-数据源 6-模板';
