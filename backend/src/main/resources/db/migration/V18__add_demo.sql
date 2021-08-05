

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for demo_new_trend_of_diagnosis
-- ----------------------------
DROP TABLE IF EXISTS `demo_new_trend_of_diagnosis`;
CREATE TABLE `demo_new_trend_of_diagnosis` (
  `date` varchar(50) NOT NULL DEFAULT '' COMMENT '日期',
  `new_diagnosis` bigint(13) DEFAULT NULL COMMENT '新增确诊',
  `current_diagnosis` bigint(13) DEFAULT NULL COMMENT '现有确诊'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of demo_new_trend_of_diagnosis
-- ----------------------------
BEGIN;
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-08', 22, 499);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-09', 13, 485);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-10', 33, 505);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-11', 28, 506);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-12', 32, 512);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-13', 35, 523);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-14', 49, 542);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-15', 206, 727);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-16', 236, 935);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-17', 358, 1262);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-18', 258, 1497);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-19', 286, 1759);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-20', 317, 2097);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-21', 325, 2365);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-22', 743, 3098);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-23', 480, 3561);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-24', 612, 4143);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-25', 554, 4675);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-26', 655, 5036);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-27', 677, 5948);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-28', 570, 6480);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-29', 503, 6951);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-30', 381, 7303);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-5-31', 378, 7652);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-1', 362, 7983);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-2', 571, 8535);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-3', 610, 9110);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-4', 497, 9674);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-5', 541, 10049);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-6', 368, 10372);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-7', 233, 10552);
INSERT INTO `demo_new_trend_of_diagnosis` VALUES ('2021-6-8', 232, 10740);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
