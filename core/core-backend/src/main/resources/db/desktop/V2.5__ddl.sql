
DROP TABLE IF EXISTS `visualization_outer_params`;
CREATE TABLE `visualization_outer_params` (
                                      `params_id` varchar(50) NOT NULL COMMENT '主键',
                                      `visualization_id` varchar(50) DEFAULT NULL COMMENT '可视化资源ID',
                                      `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用外部参数标识（1-是，0-否）',
                                      `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                                      `copy_from` varchar(50) DEFAULT NULL COMMENT '复制来源',
                                      `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                      PRIMARY KEY (`params_id`)
) COMMENT='外部参数关联关系表';


DROP TABLE IF EXISTS `visualization_outer_params_info`;
CREATE TABLE `visualization_outer_params_info` (
                                           `params_info_id` varchar(50) NOT NULL COMMENT '主键',
                                           `params_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params 表的 ID',
                                           `param_name` varchar(255) DEFAULT NULL COMMENT '参数名',
                                           `checked` tinyint(1) DEFAULT NULL COMMENT '是否启用',
                                           `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
                                           `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                           PRIMARY KEY (`params_info_id`)
)  COMMENT='外部参数配置表';


DROP TABLE IF EXISTS `visualization_outer_params_target_view_info`;
CREATE TABLE `visualization_outer_params_target_view_info` (
                                                       `target_id` varchar(50) NOT NULL COMMENT '主键',
                                                       `params_info_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params_info 表的 ID',
                                                       `target_view_id` varchar(50) DEFAULT NULL COMMENT '联动视图ID',
                                                       `target_field_id` varchar(50) DEFAULT NULL COMMENT '联动字段ID',
                                                       `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
                                                       `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
                                                       PRIMARY KEY (`target_id`)
)  COMMENT='外部参数联动视图字段信息表';

ALTER TABLE `data_visualization_info`
    MODIFY COLUMN `mobile_layout` tinyint NULL DEFAULT 0 COMMENT '移动端布局0-关闭 1-开启';
update data_visualization_info set mobile_layout = 0;
INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`) VALUES (3, 'ai.baseUrl', 'https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb', 'text', 0);
