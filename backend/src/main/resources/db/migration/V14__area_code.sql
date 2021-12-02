CREATE TABLE `area_mapping`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `province_name` varchar(255) NULL DEFAULT NULL COMMENT '省名称',
  `province_code` varchar(255)  NULL DEFAULT NULL COMMENT '省代码',
  `city_name` varchar(255)  NULL DEFAULT NULL COMMENT '市名称',
  `city_code` varchar(255) NULL DEFAULT NULL COMMENT '市代码',
  `county_name` varchar(255) NULL DEFAULT NULL COMMENT '县名称',
  `county_code` varchar(255) NULL DEFAULT NULL COMMENT '县代码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;
