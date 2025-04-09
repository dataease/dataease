
DROP TABLE IF EXISTS `snapshot_core_chart_view`;
CREATE TABLE `snapshot_core_chart_view` (
                                            `id` bigint NOT NULL COMMENT 'ID',
                                            `title` varchar(1024) DEFAULT NULL COMMENT '标题',
                                            `scene_id` bigint NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
                                            `table_id` bigint DEFAULT NULL COMMENT '数据集表ID',
                                            `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
                                            `render` varchar(50) DEFAULT NULL COMMENT '图表渲染方式',
                                            `result_count` int DEFAULT NULL COMMENT '展示结果',
                                            `result_mode` varchar(50) DEFAULT NULL COMMENT '展示模式',
                                            `x_axis` longtext COMMENT '横轴field',
                                            `x_axis_ext` longtext COMMENT 'table-row',
                                            `y_axis` longtext COMMENT '纵轴field',
                                            `y_axis_ext` longtext COMMENT '副轴',
                                            `ext_stack` longtext COMMENT '堆叠项',
                                            `ext_bubble` longtext COMMENT '气泡大小',
                                            `ext_label` longtext COMMENT '动态标签',
                                            `ext_tooltip` longtext COMMENT '动态提示',
                                            `custom_attr` longtext COMMENT '图形属性',
                                            `custom_attr_mobile` longtext COMMENT '图形属性_移动端',
                                            `custom_style` longtext COMMENT '组件样式',
                                            `custom_style_mobile` longtext COMMENT '组件样式_移动端',
                                            `custom_filter` longtext COMMENT '结果过滤',
                                            `drill_fields` longtext COMMENT '钻取字段',
                                            `senior` longtext COMMENT '高级',
                                            `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
                                            `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                            `update_time` bigint DEFAULT NULL COMMENT '更新时间',
                                            `snapshot` longtext COMMENT '缩略图 ',
                                            `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 图表',
                                            `chart_type` varchar(255) DEFAULT 'private' COMMENT '图表类型 public 公共 历史可复用的图表，private 私有 专属某个仪表板',
                                            `is_plugin` bit(1) DEFAULT NULL COMMENT '是否插件',
                                            `data_from` varchar(255) DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据',
                                            `view_fields` longtext COMMENT '图表字段集合',
                                            `refresh_view_enable` tinyint(1) DEFAULT '0' COMMENT '是否开启刷新',
                                            `refresh_unit` varchar(255) DEFAULT 'minute' COMMENT '刷新时间单位',
                                            `refresh_time` int DEFAULT '5' COMMENT '刷新时间',
                                            `linkage_active` tinyint(1) DEFAULT '0' COMMENT '是否开启联动',
                                            `jump_active` tinyint(1) DEFAULT '0' COMMENT '是否开启跳转',
                                            `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                            `copy_id` bigint DEFAULT NULL COMMENT '复制ID',
                                            `aggregate` bit(1) DEFAULT NULL COMMENT '区间条形图开启时间纬度开启聚合',
                                            `flow_map_start_name` longtext COMMENT '流向地图起点名称field',
                                            `flow_map_end_name` longtext COMMENT '流向地图终点名称field',
                                            `ext_color` longtext COMMENT '颜色维度field',
                                            `sort_priority` longtext COMMENT '字段排序优先级',
                                            PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_data_visualization_info
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_data_visualization_info`;
CREATE TABLE `snapshot_data_visualization_info` (
                                                    `id` varchar(50) NOT NULL COMMENT '主键',
                                                    `name` varchar(255) DEFAULT NULL COMMENT '名称',
                                                    `pid` varchar(50) DEFAULT NULL COMMENT '父id',
                                                    `org_id` varchar(50) DEFAULT NULL COMMENT '所属组织id',
                                                    `level` int DEFAULT NULL COMMENT '层级',
                                                    `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
                                                    `type` varchar(50) DEFAULT NULL COMMENT '类型',
                                                    `canvas_style_data` longtext COMMENT '样式数据',
                                                    `component_data` longtext COMMENT '组件数据',
                                                    `mobile_layout` tinyint DEFAULT '0' COMMENT '移动端布局0-关闭 1-开启',
                                                    `status` int DEFAULT '1' COMMENT '状态 0-未发布 1-已发布',
                                                    `self_watermark_status` int DEFAULT '0' COMMENT '是否单独打开水印 0-关闭 1-开启',
                                                    `sort` int DEFAULT '0' COMMENT '排序',
                                                    `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                                    `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
                                                    `update_time` bigint DEFAULT NULL COMMENT '更新时间',
                                                    `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
                                                    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                                                    `source` varchar(255) DEFAULT NULL COMMENT '数据来源',
                                                    `delete_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志',
                                                    `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
                                                    `delete_by` varchar(255) DEFAULT NULL COMMENT '删除人',
                                                    `version` int DEFAULT '3' COMMENT '可视化资源版本',
                                                    `content_id` varchar(50) DEFAULT '0' COMMENT '内容标识',
                                                    `check_version` varchar(50) DEFAULT '1' COMMENT '内容检查标识',
                                                    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_link_jump
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_link_jump`;
CREATE TABLE `snapshot_visualization_link_jump` (
                                                    `id` bigint NOT NULL COMMENT '主键',
                                                    `source_dv_id` bigint DEFAULT NULL COMMENT '源仪表板ID',
                                                    `source_view_id` bigint DEFAULT NULL COMMENT '源图表ID',
                                                    `link_jump_info` varchar(4000) DEFAULT NULL COMMENT '跳转信息',
                                                    `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
                                                    `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                                    `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
                                                    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_link_jump_info
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_link_jump_info`;
CREATE TABLE `snapshot_visualization_link_jump_info` (
                                                         `id` bigint NOT NULL COMMENT '主键',
                                                         `link_jump_id` bigint DEFAULT NULL COMMENT 'link jump ID',
                                                         `link_type` varchar(255) DEFAULT NULL COMMENT '关联类型 inner 内部仪表板，outer 外部链接',
                                                         `jump_type` varchar(255) DEFAULT NULL COMMENT '跳转类型 _blank 新开页面 _self 当前窗口',
                                                         `target_dv_id` bigint DEFAULT NULL COMMENT '关联仪表板ID',
                                                         `source_field_id` bigint DEFAULT NULL COMMENT '字段ID',
                                                         `content` varchar(4000) DEFAULT NULL COMMENT '内容 linkType = outer时使用',
                                                         `checked` tinyint(1) DEFAULT NULL COMMENT '是否可用',
                                                         `attach_params` tinyint(1) DEFAULT NULL COMMENT '是否附加点击参数',
                                                         `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                                         `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
                                                         `window_size` varchar(255) DEFAULT 'middle' COMMENT '窗口大小large middle small',
                                                         PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_link_jump_target_view_info
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_link_jump_target_view_info`;
CREATE TABLE `snapshot_visualization_link_jump_target_view_info` (
                                                                     `target_id` bigint NOT NULL COMMENT '主键',
                                                                     `link_jump_info_id` bigint DEFAULT NULL COMMENT 'visualization_link_jump_info 表的 ID',
                                                                     `source_field_active_id` bigint DEFAULT NULL COMMENT '勾选字段设置的匹配字段，也可以不是勾选字段本身',
                                                                     `target_view_id` varchar(50) DEFAULT NULL COMMENT '目标图表ID',
                                                                     `target_field_id` varchar(50) DEFAULT NULL COMMENT '目标字段ID',
                                                                     `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                                                     `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
                                                                     `target_type` varchar(50) DEFAULT 'view' COMMENT '联动目标类型 view 图表 filter 过滤组件 outParams 外部参数',
                                                                     PRIMARY KEY (`target_id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_linkage
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_linkage`;
CREATE TABLE `snapshot_visualization_linkage` (
                                                  `id` bigint NOT NULL COMMENT '主键',
                                                  `dv_id` bigint DEFAULT NULL COMMENT '联动大屏/仪表板ID',
                                                  `source_view_id` bigint DEFAULT NULL COMMENT '源图表id',
                                                  `target_view_id` bigint DEFAULT NULL COMMENT '联动图表id',
                                                  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
                                                  `update_people` varchar(255) DEFAULT NULL COMMENT '更新人',
                                                  `linkage_active` tinyint(1) DEFAULT '0' COMMENT '是否启用关联',
                                                  `ext1` varchar(2000) DEFAULT NULL COMMENT '扩展字段1',
                                                  `ext2` varchar(2000) DEFAULT NULL COMMENT '扩展字段2',
                                                  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                                  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
                                                  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_linkage_field
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_linkage_field`;
CREATE TABLE `snapshot_visualization_linkage_field` (
                                                        `id` bigint NOT NULL COMMENT '主键',
                                                        `linkage_id` bigint DEFAULT NULL COMMENT '联动ID',
                                                        `source_field` bigint DEFAULT NULL COMMENT '源图表字段',
                                                        `target_field` bigint DEFAULT NULL COMMENT '目标图表字段',
                                                        `update_time` bigint DEFAULT NULL COMMENT '更新时间',
                                                        `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
                                                        `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
                                                        PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_outer_params
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_outer_params`;
CREATE TABLE `snapshot_visualization_outer_params` (
                                                       `params_id` varchar(50) NOT NULL COMMENT '主键',
                                                       `visualization_id` varchar(50) DEFAULT NULL COMMENT '可视化资源ID',
                                                       `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用外部参数标识（1-是，0-否）',
                                                       `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                                                       `copy_from` varchar(50) DEFAULT NULL COMMENT '复制来源',
                                                       `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                                       PRIMARY KEY (`params_id`)
);

-- ----------------------------
-- Table structure for snapshot_visualization_outer_params_info
-- ----------------------------
DROP TABLE IF EXISTS `snapshot_visualization_outer_params_info`;
CREATE TABLE `snapshot_visualization_outer_params_info` (
                                                            `params_info_id` varchar(50) NOT NULL COMMENT '主键',
                                                            `params_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params 表的 ID',
                                                            `param_name` varchar(255) DEFAULT NULL COMMENT '参数名',
                                                            `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
                                                            `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
                                                            `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                                            `required` tinyint(1) DEFAULT '0' COMMENT '是否必填',
                                                            `default_value` varchar(255) DEFAULT NULL COMMENT '默认值 JSON格式',
                                                            `enabled_default` tinyint(1) DEFAULT '0' COMMENT '是否启用默认值',
                                                            PRIMARY KEY (`params_info_id`)
);

DROP TABLE IF EXISTS `snapshot_visualization_outer_params_target_view_info`;
CREATE TABLE `snapshot_visualization_outer_params_target_view_info` (
                                                                        `target_id` varchar(50) NOT NULL COMMENT '主键',
                                                                        `params_info_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params_info 表的 ID',
                                                                        `target_view_id` varchar(50) DEFAULT NULL COMMENT '联动视图ID/联动过滤项ID',
                                                                        `target_field_id` varchar(50) DEFAULT NULL COMMENT '联动字段ID',
                                                                        `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
                                                                        `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                                                        `target_ds_id` varchar(50) DEFAULT NULL COMMENT '联动数据集id/联动过滤组件id',
                                                                        PRIMARY KEY (`target_id`)
);

DROP TABLE IF EXISTS `de_template_version`;
CREATE TABLE `de_template_version` (
                                       `installed_rank` int NOT NULL,
                                       `version` varchar(50)  DEFAULT NULL,
                                       `description` varchar(200)  DEFAULT NULL,
                                       `type` varchar(20)  DEFAULT NULL,
                                       `script` varchar(1000)  NOT NULL,
                                       `checksum` int DEFAULT NULL,
                                       `installed_by` varchar(100)  DEFAULT NULL,
                                       `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `execution_time` int DEFAULT NULL,
                                       `success` tinyint(1) NOT NULL,
                                       PRIMARY KEY (`installed_rank`)
);
