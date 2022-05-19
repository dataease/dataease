ALTER TABLE `panel_group`
ADD COLUMN `status` varchar(255) NULL DEFAULT 'publish' COMMENT '1.publish--发布 2.unpublished--未发布' AFTER `mobile_layout`;


CREATE TABLE `de_driver` (
     `id` varchar(50) NOT NULL COMMENT '主键',
     `name` varchar(50) NOT NULL COMMENT '用户ID',
     `create_time` bigint(13) NOT NULL COMMENT '创健时间',
     `type` varchar(255) DEFAULT NULL COMMENT '数据源类型',
     `driver_class` varchar(255) DEFAULT NULL COMMENT '驱动类',
     `desc` varchar(255) DEFAULT NULL COMMENT '描述',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动';


CREATE TABLE `de_driver_details` (
     `id` varchar(50) NOT NULL COMMENT '主键',
     `de_driver_id` varchar(50) NOT NULL  COMMENT '驱动主键',
     `file_name` varchar(255) DEFAULT NULL COMMENT '名称',
     `version` varchar(255) DEFAULT NULL COMMENT '版本',
     `driver_class` longtext DEFAULT NULL COMMENT '驱动类',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动详情';