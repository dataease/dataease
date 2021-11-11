-- ----------------------------
-- 新增我分享出去的功能
-- ----------------------------
ALTER TABLE `dataease`.`panel_share`
ADD COLUMN `granter` varchar(255) NULL COMMENT '分享人' AFTER `target_id`;




-- ----------------------------
-- Table structure for panel_link_mapping
-- ----------------------------
DROP TABLE IF EXISTS `panel_link_mapping`;
CREATE TABLE `panel_link_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_id` varchar(255) DEFAULT NULL COMMENT '仪表板ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
