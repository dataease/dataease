CREATE TABLE `file_picture` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `url` longtext CHARACTER SET utf8mb4 COMMENT '图片地址',
  `img_detailed` longtext CHARACTER SET utf8mb4 COMMENT '图片详细信息',
  `type` int(5) DEFAULT NULL COMMENT '图片类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='图片表'
