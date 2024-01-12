ALTER TABLE `visualization_background` COMMENT = '边框背景表';
/*
ALTER TABLE `visualization_background_image` COMMENT = '背景图';
ALTER TABLE `visualization_link_jump` COMMENT = '跳转记录表';
ALTER TABLE `visualization_link_jump_info` COMMENT = '跳转配置表';
ALTER TABLE `visualization_link_jump_target_view_info` COMMENT = '跳转目标仪表板视图字段配置表';
ALTER TABLE `visualization_linkage` COMMENT = '联动记录表';
ALTER TABLE `visualization_linkage_field` COMMENT = '联动字段';
ALTER TABLE `visualization_subject` COMMENT = '主题表';
ALTER TABLE `visualization_template_extend_data` COMMENT = '模板视图明细信息表';

ALTER TABLE `core_dataset_group`
    MODIFY COLUMN `qrtz_instance` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Quartz 实例 ID' AFTER `create_time`;

ALTER TABLE `core_dataset_table_field`
    MODIFY COLUMN `size` int(0) NULL DEFAULT NULL COMMENT '字段长度（允许为空，默认0）' AFTER `type`,
    MODIFY COLUMN `date_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间字段类型' AFTER `accuracy`;

ALTER TABLE `core_datasource_task`
    MODIFY COLUMN `extra_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '额外数据' AFTER `last_exec_status`;

ALTER TABLE `core_datasource_task_log`
    MODIFY COLUMN `trigger_type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新频率类型' AFTER `create_time`;

ALTER TABLE `core_driver_jar`
    MODIFY COLUMN `trans_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '替换后的 jar 包名称' AFTER `driver_class`,
    MODIFY COLUMN `is_trans_name` tinyint(1) NULL DEFAULT NULL COMMENT '是否将上传 jar 包替换了名称（1-是，0-否）' AFTER `trans_name`;

ALTER TABLE `core_rsa`
    MODIFY COLUMN `aes_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AES 加密算法的 key' AFTER `create_time`;

ALTER TABLE `data_visualization_info`
    MODIFY COLUMN `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键' FIRST;

ALTER TABLE `de_standalone_version`
    MODIFY COLUMN `installed_rank` int(0) NOT NULL COMMENT '执行顺序（主键）' FIRST,
    MODIFY COLUMN `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本' AFTER `installed_rank`,
    MODIFY COLUMN `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '描述' AFTER `version`,
    MODIFY COLUMN `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型' AFTER `description`,
    MODIFY COLUMN `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '脚本名称' AFTER `type`,
    MODIFY COLUMN `checksum` int(0) NULL DEFAULT NULL COMMENT '脚本内容一致性校验码' AFTER `script`,
    MODIFY COLUMN `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行用户' AFTER `checksum`,
    MODIFY COLUMN `installed_on` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '执行时间' AFTER `installed_by`,
    MODIFY COLUMN `execution_time` int(0) NOT NULL COMMENT '执行时长' AFTER `installed_on`,
    MODIFY COLUMN `success` tinyint(1) NOT NULL COMMENT '状态（1-成功，0-失败）' AFTER `execution_time`;



ALTER TABLE `visualization_background`
    MODIFY COLUMN `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `classification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名' AFTER `name`,
    MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容' AFTER `classification`,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注' AFTER `content`,
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序' AFTER `remark`,
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间' AFTER `sort`,
    MODIFY COLUMN `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在目录地址' AFTER `upload_time`,
    MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片url' AFTER `base_url`;

ALTER TABLE `visualization_background_image`
    MODIFY COLUMN `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称' AFTER `id`,
    MODIFY COLUMN `classification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名' AFTER `name`,
    MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容' AFTER `classification`,
    MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注' AFTER `content`,
    MODIFY COLUMN `sort` int(0) NULL DEFAULT NULL COMMENT '排序' AFTER `remark`,
    MODIFY COLUMN `upload_time` bigint(0) NULL DEFAULT NULL COMMENT '上传时间' AFTER `sort`,
    MODIFY COLUMN `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在目录地址' AFTER `upload_time`,
    MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片url' AFTER `base_url`;

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
    MODIFY COLUMN `target_view_id` bigint(0) NULL DEFAULT NULL COMMENT '目标视图ID' AFTER `source_field_active_id`,
    MODIFY COLUMN `target_field_id` bigint(0) NULL DEFAULT NULL COMMENT '目标字段ID' AFTER `target_view_id`,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `target_field_id`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_linkage`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '联动大屏/仪表板ID' AFTER `id`,
    MODIFY COLUMN `ext1` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1' AFTER `linkage_active`,
    MODIFY COLUMN `ext2` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2' AFTER `ext1`,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `ext2`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_linkage_field`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `copy_from` bigint(0) NULL DEFAULT NULL COMMENT '复制来源' AFTER `update_time`,
    MODIFY COLUMN `copy_id` bigint(0) NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `visualization_subject`
    MODIFY COLUMN `create_num` int(0) NOT NULL DEFAULT 0 COMMENT '创建序号' AFTER `cover_url`;

ALTER TABLE `visualization_template_category`
    MODIFY COLUMN `template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建' AFTER `snapshot`;

ALTER TABLE `visualization_template_extend_data`
    MODIFY COLUMN `id` bigint(0) NOT NULL COMMENT '主键' FIRST,
    MODIFY COLUMN `dv_id` bigint(0) NULL DEFAULT NULL COMMENT '模板ID' AFTER `id`,
    MODIFY COLUMN `view_id` bigint(0) NULL DEFAULT NULL COMMENT '视图ID' AFTER `dv_id`,
    MODIFY COLUMN `view_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视图详情' AFTER `view_id`,
    MODIFY COLUMN `copy_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '复制来源' AFTER `view_details`,
    MODIFY COLUMN `copy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '复制来源ID' AFTER `copy_from`;

ALTER TABLE `core_opt_recent`
    MODIFY COLUMN `resource_type` int(0) NOT NULL COMMENT '资源类型 1-可视化资源 2-仪表板 3-数据大屏 4-数据集 5-数据源 6-模板' AFTER `uid`;

 */
