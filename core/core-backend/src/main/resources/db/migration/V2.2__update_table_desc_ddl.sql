ALTER TABLE `QRTZ_BLOB_TRIGGERS`
    COMMENT = '自定义触发器存储（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_CALENDARS`
    COMMENT = 'Quartz日历（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_CRON_TRIGGERS`
    COMMENT = 'CronTrigger存储（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_FIRED_TRIGGERS`
    COMMENT = '存储已经触发的trigger相关信息（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_JOB_DETAILS`
    COMMENT = '存储jobDetails信息（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_LOCKS`
    COMMENT = 'Quartz锁表，为多个节点调度提供分布式锁（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
    COMMENT = '存放暂停掉的触发器（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_SCHEDULER_STATE`
    COMMENT = '存储所有节点的scheduler（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_SIMPLE_TRIGGERS`
    COMMENT = 'SimpleTrigger存储（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_SIMPROP_TRIGGERS`
    COMMENT = '存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器（开源作业调度框架Quartz）';
ALTER TABLE `QRTZ_TRIGGERS`
    COMMENT = '存储定义的trigger（开源作业调度框架Quartz）';
ALTER TABLE `area`
    COMMENT = '地图区域表';
ALTER TABLE `core_area_custom`
    COMMENT = '自定义地图区域信息表';
ALTER TABLE `core_chart_view`
    COMMENT = '组件图表表';
ALTER TABLE `core_dataset_group`
    COMMENT = '数据集分组表';
ALTER TABLE `core_dataset_table`
    COMMENT = 'table数据集';
ALTER TABLE `core_dataset_table_field`
    COMMENT = 'table数据集表字段';
ALTER TABLE `core_dataset_table_sql_log`
    COMMENT = 'table数据集查询sql日志';
ALTER TABLE `core_datasource`
    COMMENT = '数据源表';
ALTER TABLE `core_datasource_task`
    COMMENT = '数据源定时同步任务';
ALTER TABLE `core_datasource_task_log`
    COMMENT = '数据源定时同步任务执行日志';
ALTER TABLE `core_de_engine`
    COMMENT = '数据引擎';
ALTER TABLE `core_driver`
    COMMENT = '驱动';
ALTER TABLE `core_driver_jar`
    COMMENT = '驱动详情';
ALTER TABLE `core_menu`
    COMMENT = '路由菜单';
ALTER TABLE `core_opt_recent`
    COMMENT = '可视化资源表';
ALTER TABLE `core_rsa`
    COMMENT = 'rsa 密钥表';
ALTER TABLE `core_store`
    COMMENT = '用户收藏表';
ALTER TABLE `core_sys_setting`
    COMMENT = '系统设置表';
ALTER TABLE `data_visualization_info`
    COMMENT = '可视化大屏信息表';
ALTER TABLE `de_standalone_version`
    COMMENT = '数据库版本变更记录表';

ALTER TABLE `visualization_background`
    COMMENT = '边框背景表';
ALTER TABLE `visualization_background_image`
    COMMENT = '背景图';
ALTER TABLE `visualization_link_jump`
    COMMENT = '跳转记录表';
ALTER TABLE `visualization_link_jump_info`
    COMMENT = '跳转配置表';
ALTER TABLE `visualization_link_jump_target_view_info`
    COMMENT = '跳转目标仪表板图表字段配置表';
ALTER TABLE `visualization_linkage`
    COMMENT = '联动记录表';
ALTER TABLE `visualization_linkage_field`
    COMMENT = '联动字段';
ALTER TABLE `visualization_subject`
    COMMENT = '主题表';
ALTER TABLE `visualization_template_extend_data`
    COMMENT = '模板图表明细信息表';

ALTER TABLE `core_dataset_group`
    MODIFY COLUMN `qrtz_instance` varchar(1024) NULL DEFAULT NULL COMMENT 'Quartz 实例 ID' AFTER `create_time`;

ALTER TABLE `core_dataset_table_field`
    MODIFY COLUMN `size` int(0) NULL DEFAULT NULL COMMENT '字段长度（允许为空，默认0）' AFTER `type`,
    MODIFY COLUMN `date_format` varchar(255) NULL DEFAULT NULL COMMENT '时间字段类型' AFTER `accuracy`;

ALTER TABLE `core_datasource_task`
    MODIFY COLUMN `extra_data` longtext NULL COMMENT '额外数据' AFTER `last_exec_status`;

ALTER TABLE `core_datasource_task_log`
    MODIFY COLUMN `trigger_type` varchar(45) NULL DEFAULT NULL COMMENT '更新频率类型' AFTER `create_time`;

ALTER TABLE `core_driver_jar`
    MODIFY COLUMN `trans_name` varchar(255) NULL DEFAULT NULL COMMENT '替换后的 jar 包名称' AFTER `driver_class`,
    MODIFY COLUMN `is_trans_name` tinyint(1) NULL DEFAULT NULL COMMENT '是否将上传 jar 包替换了名称（1-是，0-否）' AFTER `trans_name`;

ALTER TABLE `core_rsa`
    MODIFY COLUMN `aes_key` text NOT NULL COMMENT 'AES 加密算法的 key' AFTER `create_time`;

ALTER TABLE `data_visualization_info`
    MODIFY COLUMN `id` varchar(50) NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `de_standalone_version`
    MODIFY COLUMN `installed_rank` int(0) NOT NULL COMMENT '执行顺序（主键）' FIRST,
    MODIFY COLUMN `version` varchar(50) NULL DEFAULT NULL COMMENT '版本' AFTER `installed_rank`,
    MODIFY COLUMN `description` varchar(200) NOT NULL COMMENT '描述' AFTER `version`,
    MODIFY COLUMN `type` varchar(20) NOT NULL COMMENT '类型' AFTER `description`,
    MODIFY COLUMN `script` varchar(1000) NOT NULL COMMENT '脚本名称' AFTER `type`,
    MODIFY COLUMN `checksum` int(0) NULL DEFAULT NULL COMMENT '脚本内容一致性校验码' AFTER `script`,
    MODIFY COLUMN `installed_by` varchar(100) NOT NULL COMMENT '执行用户' AFTER `checksum`,
    MODIFY COLUMN `installed_on` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '执行时间' AFTER `installed_by`,
    MODIFY COLUMN `execution_time` int(0) NOT NULL COMMENT '执行时长' AFTER `installed_on`,
    MODIFY COLUMN `success` tinyint(1) NOT NULL COMMENT '状态（1-成功，0-失败）' AFTER `execution_time`;


ALTER TABLE `visualization_background`
    MODIFY COLUMN `id` varchar(64) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `name` varchar(255) NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `classification` varchar(255) NOT NULL COMMENT '分类名' AFTER `name`,
    MODIFY COLUMN `content` longtext NULL COMMENT '内容' AFTER `classification`,
    MODIFY COLUMN `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注' AFTER `content`,
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序' AFTER `remark`,
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间' AFTER `sort`,
    MODIFY COLUMN `base_url` varchar(255) NULL DEFAULT NULL COMMENT '所在目录地址' AFTER `upload_time`,
    MODIFY COLUMN `url` varchar(255) NULL DEFAULT NULL COMMENT '图片url' AFTER `base_url`;

ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `id` varchar(64) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `name` varchar(255) NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `classification` varchar(255) NOT NULL COMMENT '分类名' AFTER `name`,
    MODIFY COLUMN `content` longtext NULL COMMENT '内容' AFTER `classification`,
    MODIFY COLUMN `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注' AFTER `content`,
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序' AFTER `remark`,
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间' AFTER `sort`,
    MODIFY COLUMN `base_url` varchar(255) NULL DEFAULT NULL COMMENT '所在目录地址' AFTER `upload_time`,
    MODIFY COLUMN `url` varchar(255) NULL DEFAULT NULL COMMENT '图片url' AFTER `base_url`;

ALTER TABLE `visualization_link_jump`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `checked`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_link_jump_info`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `attach_params`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `target_id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `link_jump_info_id` bigint(0) NULL DEFAULT NULL COMMENT 'visualization_link_jump_info 表的 ID' AFTER `target_id`,
    MODIFY COLUMN `target_view_id` bigint(0) NULL DEFAULT NULL COMMENT '目标图表ID' AFTER `source_field_active_id`,
    MODIFY COLUMN `target_field_id` bigint(0) NULL DEFAULT NULL COMMENT '目标字段ID' AFTER `target_view_id`,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `target_field_id`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '联动大屏/仪表板ID' AFTER `id`,
    MODIFY COLUMN `ext1` varchar(2000) NULL DEFAULT NULL COMMENT '扩展字段1' AFTER `linkage_active`,
    MODIFY COLUMN `ext2` varchar(2000) NULL DEFAULT NULL COMMENT '扩展字段2' AFTER `ext1`,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `ext2`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_linkage_field`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `update_time`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_subject`
    MODIFY COLUMN `create_num` int(0) NOT NULL DEFAULT 0 COMMENT '创建序号' AFTER `cover_url`;

ALTER TABLE `visualization_template_category`
    MODIFY COLUMN `template_type` varchar(255) NULL DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建' AFTER `snapshot`;

ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '模板ID' AFTER `id`,
    MODIFY COLUMN `view_id` bigint(0) NULL DEFAULT NULL COMMENT '图表ID' AFTER `dv_id`,
    MODIFY COLUMN `view_details` longtext NULL COMMENT '图表详情' AFTER `view_id`,
    MODIFY COLUMN `copy_from` varchar(255) NULL DEFAULT NULL COMMENT '复制来源' AFTER `view_details`,
    MODIFY COLUMN `copy_id` varchar(255) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `core_opt_recent`
    MODIFY COLUMN `resource_type` int(0) NOT NULL COMMENT '资源类型 1-可视化资源 2-仪表板 3-数据大屏 4-数据集 5-数据源 6-模板' AFTER `uid`;
