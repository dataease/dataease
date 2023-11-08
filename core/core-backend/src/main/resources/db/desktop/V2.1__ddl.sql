DROP TABLE IF EXISTS `visualization_template`;
CREATE TABLE `visualization_template` (
                                          `id` varchar(50) NOT NULL COMMENT '主键',
                                          `name` varchar(255) DEFAULT NULL COMMENT '名称',
                                          `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
                                          `level` int(10) DEFAULT NULL COMMENT '层级',
                                          `dv_type` varchar(255) DEFAULT NULL COMMENT '模版种类  dataV or dashboard 目录或者文件夹',
                                          `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
                                          `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
                                          `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
                                          `snapshot` longtext COMMENT '缩略图',
                                          `template_type` varchar(255) DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建 ',
                                          `template_style` longtext COMMENT 'template 样式',
                                          `template_data` longtext COMMENT 'template 数据',
                                          `dynamic_data` longtext COMMENT '预存数据',
                                          PRIMARY KEY (`id`)
);