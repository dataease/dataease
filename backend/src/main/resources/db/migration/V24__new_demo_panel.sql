

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for demo_gdp_2021
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_2021`;
CREATE TABLE `demo_gdp_2021` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `2021GDP）` varchar(255) DEFAULT NULL,
  `2020GDP` varchar(255) DEFAULT NULL,
  `increase` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_2021
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_2021` VALUES (1, '532167', '463324.76', '12.7');
COMMIT;

-- ----------------------------
-- Table structure for demo_gdp_by_city
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_by_city`;
CREATE TABLE `demo_gdp_by_city` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `province` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `gdp` double(255,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=309 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_by_city
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_by_city` VALUES (1, '广东省', '深圳市', 14324.47);
INSERT INTO `demo_gdp_by_city` VALUES (2, '广东省', '广州市', 13101.89);
INSERT INTO `demo_gdp_by_city` VALUES (3, '广东省', '佛山市', 5474.12);
INSERT INTO `demo_gdp_by_city` VALUES (4, '广东省', '东莞市', 5000.11);
INSERT INTO `demo_gdp_by_city` VALUES (5, '广东省', '惠州市', 2226.58);
INSERT INTO `demo_gdp_by_city` VALUES (6, '广东省', '珠海市', 1841.47);
INSERT INTO `demo_gdp_by_city` VALUES (7, '广东省', '茂名市', 1660.62);
INSERT INTO `demo_gdp_by_city` VALUES (8, '广东省', '江门市', 1656.57);
INSERT INTO `demo_gdp_by_city` VALUES (9, '广东省', '中山市', 1651.20);
INSERT INTO `demo_gdp_by_city` VALUES (10, '广东省', '湛江市', 1620.41);
INSERT INTO `demo_gdp_by_city` VALUES (11, '广东省', '汕头市', 1377.60);
INSERT INTO `demo_gdp_by_city` VALUES (12, '广东省', '肇庆市', 1125.57);
INSERT INTO `demo_gdp_by_city` VALUES (13, '广东省', '揭阳市', 1034.39);
INSERT INTO `demo_gdp_by_city` VALUES (14, '广东省', '清远市', 900.40);
INSERT INTO `demo_gdp_by_city` VALUES (15, '广东省', '阳江市', 719.32);
INSERT INTO `demo_gdp_by_city` VALUES (16, '广东省', '韶关市', 709.06);
INSERT INTO `demo_gdp_by_city` VALUES (17, '广东省', '梅州市', 603.30);
INSERT INTO `demo_gdp_by_city` VALUES (18, '广东省', '汕尾市', 587.62);
INSERT INTO `demo_gdp_by_city` VALUES (19, '广东省', '潮州市', 575.88);
INSERT INTO `demo_gdp_by_city` VALUES (20, '广东省', '河源市', 546.35);
INSERT INTO `demo_gdp_by_city` VALUES (21, '广东省', '云浮市', 489.34);
INSERT INTO `demo_gdp_by_city` VALUES (22, '江苏省', '苏州市', 10684.66);
INSERT INTO `demo_gdp_by_city` VALUES (23, '江苏省', '南京市', 7622.77);
INSERT INTO `demo_gdp_by_city` VALUES (24, '江苏省', '无锡市', 6499.21);
INSERT INTO `demo_gdp_by_city` VALUES (25, '江苏省', '南通市', 5483.17);
INSERT INTO `demo_gdp_by_city` VALUES (26, '江苏省', '常州市', 4089.00);
INSERT INTO `demo_gdp_by_city` VALUES (27, '江苏省', '徐州市', 3607.39);
INSERT INTO `demo_gdp_by_city` VALUES (28, '江苏省', '扬州市', 3064.78);
INSERT INTO `demo_gdp_by_city` VALUES (29, '江苏省', '盐城市', 3045.00);
INSERT INTO `demo_gdp_by_city` VALUES (30, '江苏省', '泰州市', 2860.04);
INSERT INTO `demo_gdp_by_city` VALUES (31, '江苏省', '镇江市', 2389.90);
INSERT INTO `demo_gdp_by_city` VALUES (32, '江苏省', '淮安市', 2174.46);
INSERT INTO `demo_gdp_by_city` VALUES (33, '江苏省', '连云港市', 1716.80);
INSERT INTO `demo_gdp_by_city` VALUES (34, '江苏省', '宿迁市', 1653.19);
INSERT INTO `demo_gdp_by_city` VALUES (35, '山东省', '青岛市', 6539.21);
INSERT INTO `demo_gdp_by_city` VALUES (36, '山东省', '济南市', 5198.75);
INSERT INTO `demo_gdp_by_city` VALUES (37, '山东省', '烟台市', 3918.38);
INSERT INTO `demo_gdp_by_city` VALUES (38, '山东省', '潍坊市', 3269.30);
INSERT INTO `demo_gdp_by_city` VALUES (39, '山东省', '临沂市', 2552.90);
INSERT INTO `demo_gdp_by_city` VALUES (40, '山东省', '济宁市', 2379.66);
INSERT INTO `demo_gdp_by_city` VALUES (41, '山东省', '淄博市', 2003.92);
INSERT INTO `demo_gdp_by_city` VALUES (42, '山东省', '菏泽市', 1927.21);
INSERT INTO `demo_gdp_by_city` VALUES (43, '山东省', '德州市', 1663.27);
INSERT INTO `demo_gdp_by_city` VALUES (44, '山东省', '东营市', 1617.45);
INSERT INTO `demo_gdp_by_city` VALUES (45, '山东省', '威海市', 1516.02);
INSERT INTO `demo_gdp_by_city` VALUES (46, '山东省', '泰安市', 1474.50);
INSERT INTO `demo_gdp_by_city` VALUES (47, '山东省', '滨州市', 1328.25);
INSERT INTO `demo_gdp_by_city` VALUES (48, '山东省', '聊城市', 1257.97);
INSERT INTO `demo_gdp_by_city` VALUES (49, '山东省', '日照市', 1037.35);
INSERT INTO `demo_gdp_by_city` VALUES (50, '山东省', '枣庄市', 926.11);
INSERT INTO `demo_gdp_by_city` VALUES (51, '浙江省', '杭州市', 8656.03);
INSERT INTO `demo_gdp_by_city` VALUES (52, '浙江省', '宁波市', 6640.59);
INSERT INTO `demo_gdp_by_city` VALUES (53, '浙江省', '温州市', 3644.23);
INSERT INTO `demo_gdp_by_city` VALUES (54, '浙江省', '绍兴市', 3188.61);
INSERT INTO `demo_gdp_by_city` VALUES (55, '浙江省', '嘉兴市', 2942.14);
INSERT INTO `demo_gdp_by_city` VALUES (56, '浙江省', '台州市', 2722.97);
INSERT INTO `demo_gdp_by_city` VALUES (57, '浙江省', '金华市', 2506.37);
INSERT INTO `demo_gdp_by_city` VALUES (58, '浙江省', '湖州市', 1712.68);
INSERT INTO `demo_gdp_by_city` VALUES (59, '浙江省', '衢州市', 874.25);
INSERT INTO `demo_gdp_by_city` VALUES (60, '浙江省', '丽水市', 830.04);
INSERT INTO `demo_gdp_by_city` VALUES (61, '浙江省', '舟山市', 796.93);
INSERT INTO `demo_gdp_by_city` VALUES (62, '河南省', '郑州市', 6314.90);
INSERT INTO `demo_gdp_by_city` VALUES (63, '河南省', '洛阳市', 2369.99);
INSERT INTO `demo_gdp_by_city` VALUES (64, '河南省', '南阳市', 2063.56);
INSERT INTO `demo_gdp_by_city` VALUES (65, '河南省', '周口市', 1710.45);
INSERT INTO `demo_gdp_by_city` VALUES (66, '河南省', '新乡市', 1537.91);
INSERT INTO `demo_gdp_by_city` VALUES (67, '河南省', '商丘市', 1481.44);
INSERT INTO `demo_gdp_by_city` VALUES (68, '河南省', '信阳市', 1429.81);
INSERT INTO `demo_gdp_by_city` VALUES (69, '河南省', '平顶山市', 1288.61);
INSERT INTO `demo_gdp_by_city` VALUES (70, '河南省', '开封市', 1242.11);
INSERT INTO `demo_gdp_by_city` VALUES (71, '河南省', '安阳市', 1176.30);
INSERT INTO `demo_gdp_by_city` VALUES (72, '河南省', '濮阳市', 842.31);
INSERT INTO `demo_gdp_by_city` VALUES (73, '河南省', '三门峡市', 735.39);
INSERT INTO `demo_gdp_by_city` VALUES (74, '河南省', '鹤壁市', 526.56);
INSERT INTO `demo_gdp_by_city` VALUES (75, '四川省', '成都市', 9602.72);
INSERT INTO `demo_gdp_by_city` VALUES (76, '四川省', '绵阳市', 1546.17);
INSERT INTO `demo_gdp_by_city` VALUES (77, '四川省', '宜宾市', 1381.79);
INSERT INTO `demo_gdp_by_city` VALUES (78, '四川省', '德阳市', 1228.69);
INSERT INTO `demo_gdp_by_city` VALUES (79, '四川省', '南充市', 1143.36);
INSERT INTO `demo_gdp_by_city` VALUES (80, '四川省', '泸州市', 1122.80);
INSERT INTO `demo_gdp_by_city` VALUES (81, '四川省', '达州市', 1061.60);
INSERT INTO `demo_gdp_by_city` VALUES (82, '四川省', '乐山市', 1005.89);
INSERT INTO `demo_gdp_by_city` VALUES (83, '四川省', '凉山州', 912.81);
INSERT INTO `demo_gdp_by_city` VALUES (84, '四川省', '内江市', 744.07);
INSERT INTO `demo_gdp_by_city` VALUES (85, '四川省', '自贡市', 740.07);
INSERT INTO `demo_gdp_by_city` VALUES (86, '四川省', '眉山市', 738.99);
INSERT INTO `demo_gdp_by_city` VALUES (87, '四川省', '遂宁市', 718.16);
INSERT INTO `demo_gdp_by_city` VALUES (88, '四川省', '广安市', 655.90);
INSERT INTO `demo_gdp_by_city` VALUES (89, '四川省', '攀枝花市', 561.96);
INSERT INTO `demo_gdp_by_city` VALUES (90, '四川省', '广元市', 505.01);
INSERT INTO `demo_gdp_by_city` VALUES (91, '四川省', '资阳市', 426.90);
INSERT INTO `demo_gdp_by_city` VALUES (92, '四川省', '雅安市', 380.47);
INSERT INTO `demo_gdp_by_city` VALUES (93, '四川省', '巴中市', 377.87);
INSERT INTO `demo_gdp_by_city` VALUES (94, '四川省', '阿坝州', 196.06);
INSERT INTO `demo_gdp_by_city` VALUES (95, '四川省', '甘孜州', 181.07);
INSERT INTO `demo_gdp_by_city` VALUES (96, '福建省', '泉州市', 5362.71);
INSERT INTO `demo_gdp_by_city` VALUES (97, '福建省', '福州市', 5011.09);
INSERT INTO `demo_gdp_by_city` VALUES (98, '福建省', '厦门市', 3399.85);
INSERT INTO `demo_gdp_by_city` VALUES (99, '福建省', '漳州市', 2422.86);
INSERT INTO `demo_gdp_by_city` VALUES (100, '福建省', '龙岩市', 1487.09);
INSERT INTO `demo_gdp_by_city` VALUES (101, '福建省', '莆田市', 1486.50);
INSERT INTO `demo_gdp_by_city` VALUES (102, '福建省', '宁德市', 1467.20);
INSERT INTO `demo_gdp_by_city` VALUES (103, '福建省', '三明市', 1316.52);
INSERT INTO `demo_gdp_by_city` VALUES (104, '福建省', '南平市', 945.49);
INSERT INTO `demo_gdp_by_city` VALUES (105, '湖北省', '武汉市', 8251.50);
INSERT INTO `demo_gdp_by_city` VALUES (106, '湖北省', '襄阳市', 2377.58);
INSERT INTO `demo_gdp_by_city` VALUES (107, '湖北省', '宜昌市', 2166.61);
INSERT INTO `demo_gdp_by_city` VALUES (108, '湖北省', '荆州市', 1235.90);
INSERT INTO `demo_gdp_by_city` VALUES (109, '湖北省', '孝感市', 1169.08);
INSERT INTO `demo_gdp_by_city` VALUES (110, '湖北省', '黄冈市', 1082.32);
INSERT INTO `demo_gdp_by_city` VALUES (111, '湖北省', '十堰市', 1022.40);
INSERT INTO `demo_gdp_by_city` VALUES (112, '湖北省', '荆门市', 976.61);
INSERT INTO `demo_gdp_by_city` VALUES (113, '湖北省', '黄石市', 871.44);
INSERT INTO `demo_gdp_by_city` VALUES (114, '湖北省', '咸宁市', 802.81);
INSERT INTO `demo_gdp_by_city` VALUES (115, '湖北省', '随州市', 584.34);
INSERT INTO `demo_gdp_by_city` VALUES (116, '湖北省', '恩施市', 569.02);
INSERT INTO `demo_gdp_by_city` VALUES (117, '湖北省', '鄂州市', 539.59);
INSERT INTO `demo_gdp_by_city` VALUES (118, '湖北省', '潜江市', 396.68);
INSERT INTO `demo_gdp_by_city` VALUES (119, '湖北省', '仙桃市', 383.11);
INSERT INTO `demo_gdp_by_city` VALUES (120, '湖北省', '天门市', 303.11);
INSERT INTO `demo_gdp_by_city` VALUES (121, '湖南省', '长沙市', 6365.76);
INSERT INTO `demo_gdp_by_city` VALUES (122, '湖南省', '岳阳市', 1980.08);
INSERT INTO `demo_gdp_by_city` VALUES (123, '湖南省', '常德市', 1885.30);
INSERT INTO `demo_gdp_by_city` VALUES (124, '湖南省', '衡阳市', 1802.70);
INSERT INTO `demo_gdp_by_city` VALUES (125, '湖南省', '株洲市', 1604.50);
INSERT INTO `demo_gdp_by_city` VALUES (126, '湖南省', '郴州市', 1298.11);
INSERT INTO `demo_gdp_by_city` VALUES (127, '湖南省', '湘潭市', 1209.90);
INSERT INTO `demo_gdp_by_city` VALUES (128, '湖南省', '邵阳市', 1130.19);
INSERT INTO `demo_gdp_by_city` VALUES (129, '湖南省', '永州市', 1064.84);
INSERT INTO `demo_gdp_by_city` VALUES (130, '湖南省', '益阳市', 937.69);
INSERT INTO `demo_gdp_by_city` VALUES (131, '湖南省', '娄底市', 870.80);
INSERT INTO `demo_gdp_by_city` VALUES (132, '湖南省', '怀化市', 861.65);
INSERT INTO `demo_gdp_by_city` VALUES (133, '湖南省', '湘西州', 372.10);
INSERT INTO `demo_gdp_by_city` VALUES (134, '湖南省', '张家界市', 287.50);
INSERT INTO `demo_gdp_by_city` VALUES (135, '安徽省', '合肥市', 5203.46);
INSERT INTO `demo_gdp_by_city` VALUES (136, '安徽省', '芜湖市', 2038.15);
INSERT INTO `demo_gdp_by_city` VALUES (137, '安徽省', '滁州市', 1664.70);
INSERT INTO `demo_gdp_by_city` VALUES (138, '安徽省', '阜阳市', 1489.10);
INSERT INTO `demo_gdp_by_city` VALUES (139, '安徽省', '安庆市', 1314.30);
INSERT INTO `demo_gdp_by_city` VALUES (140, '安徽省', '马鞍山市', 1221.99);
INSERT INTO `demo_gdp_by_city` VALUES (141, '安徽省', '宿州市', 1074.80);
INSERT INTO `demo_gdp_by_city` VALUES (142, '安徽省', '蚌埠市', 1042.94);
INSERT INTO `demo_gdp_by_city` VALUES (143, '安徽省', '亳州市', 971.05);
INSERT INTO `demo_gdp_by_city` VALUES (144, '安徽省', '六安市', 912.70);
INSERT INTO `demo_gdp_by_city` VALUES (145, '安徽省', '宣城市', 877.20);
INSERT INTO `demo_gdp_by_city` VALUES (146, '安徽省', '淮南市', 693.70);
INSERT INTO `demo_gdp_by_city` VALUES (147, '安徽省', '淮北市', 588.00);
INSERT INTO `demo_gdp_by_city` VALUES (148, '安徽省', '铜陵市', 542.35);
INSERT INTO `demo_gdp_by_city` VALUES (149, '安徽省', '池州市', 480.40);
INSERT INTO `demo_gdp_by_city` VALUES (150, '安徽省', '黄山市', 450.93);
INSERT INTO `demo_gdp_by_city` VALUES (151, '上海市', '上海市', 20102.00);
INSERT INTO `demo_gdp_by_city` VALUES (152, '重庆市', '重庆市', 12903.00);
INSERT INTO `demo_gdp_by_city` VALUES (153, '北京市', '北京市', 19228.00);
INSERT INTO `demo_gdp_by_city` VALUES (154, '河北省', '唐山市', 3805.40);
INSERT INTO `demo_gdp_by_city` VALUES (155, '河北省', '石家庄市', 3164.10);
INSERT INTO `demo_gdp_by_city` VALUES (156, '河北省', '沧州市', 1992.20);
INSERT INTO `demo_gdp_by_city` VALUES (157, '河北省', '邯郸市', 1990.80);
INSERT INTO `demo_gdp_by_city` VALUES (158, '河北省', '保定市', 1929.00);
INSERT INTO `demo_gdp_by_city` VALUES (159, '河北省', '廊坊市', 1440.70);
INSERT INTO `demo_gdp_by_city` VALUES (160, '河北省', '邢台市', 1073.70);
INSERT INTO `demo_gdp_by_city` VALUES (161, '河北省', '秦皇岛市', 930.04);
INSERT INTO `demo_gdp_by_city` VALUES (162, '河北省', '衡水市', 762.40);
INSERT INTO `demo_gdp_by_city` VALUES (163, '河北省', '张家口市', 753.70);
INSERT INTO `demo_gdp_by_city` VALUES (164, '河北省', '承德市', 718.60);
INSERT INTO `demo_gdp_by_city` VALUES (165, '河北省', '定州市', 193.40);
INSERT INTO `demo_gdp_by_city` VALUES (166, '江西省', '南昌市', 3155.75);
INSERT INTO `demo_gdp_by_city` VALUES (167, '江西省', '赣州市', 1966.76);
INSERT INTO `demo_gdp_by_city` VALUES (168, '江西省', '九江市', 1780.56);
INSERT INTO `demo_gdp_by_city` VALUES (169, '江西省', '宜春市', 1517.09);
INSERT INTO `demo_gdp_by_city` VALUES (170, '江西省', '上饶市', 1411.99);
INSERT INTO `demo_gdp_by_city` VALUES (171, '江西省', '吉安市', 1179.88);
INSERT INTO `demo_gdp_by_city` VALUES (172, '江西省', '抚州市', 827.60);
INSERT INTO `demo_gdp_by_city` VALUES (173, '江西省', '新余市', 542.58);
INSERT INTO `demo_gdp_by_city` VALUES (174, '江西省', '鹰潭市', 540.79);
INSERT INTO `demo_gdp_by_city` VALUES (175, '江西省', '萍乡市', 529.16);
INSERT INTO `demo_gdp_by_city` VALUES (176, '江西省', '景德镇市', 525.06);
INSERT INTO `demo_gdp_by_city` VALUES (177, '陕西省', '西安市', 5099.62);
INSERT INTO `demo_gdp_by_city` VALUES (178, '陕西省', '榆林市', 2212.76);
INSERT INTO `demo_gdp_by_city` VALUES (179, '陕西省', '宝鸡市', 1144.28);
INSERT INTO `demo_gdp_by_city` VALUES (180, '陕西省', '咸阳市', 1118.44);
INSERT INTO `demo_gdp_by_city` VALUES (181, '陕西省', '渭南市', 937.51);
INSERT INTO `demo_gdp_by_city` VALUES (182, '陕西省', '延安市', 861.87);
INSERT INTO `demo_gdp_by_city` VALUES (183, '陕西省', '汉中市', 848.74);
INSERT INTO `demo_gdp_by_city` VALUES (184, '陕西省', '安康市', 577.20);
INSERT INTO `demo_gdp_by_city` VALUES (185, '陕西省', '商洛市', 375.27);
INSERT INTO `demo_gdp_by_city` VALUES (186, '陕西省', '铜川市', 201.47);
INSERT INTO `demo_gdp_by_city` VALUES (187, '陕西省', '杨凌区', 77.57);
INSERT INTO `demo_gdp_by_city` VALUES (188, '云南省', '昆明市', 3620.37);
INSERT INTO `demo_gdp_by_city` VALUES (189, '云南省', '曲靖市', 1497.73);
INSERT INTO `demo_gdp_by_city` VALUES (190, '云南省', '红河州', 1299.71);
INSERT INTO `demo_gdp_by_city` VALUES (191, '云南省', '玉溪市', 1139.82);
INSERT INTO `demo_gdp_by_city` VALUES (192, '云南省', '楚雄州', 725.31);
INSERT INTO `demo_gdp_by_city` VALUES (193, '云南省', '大理州', 710.74);
INSERT INTO `demo_gdp_by_city` VALUES (194, '云南省', '昭通市', 660.91);
INSERT INTO `demo_gdp_by_city` VALUES (195, '云南省', '文山州', 580.70);
INSERT INTO `demo_gdp_by_city` VALUES (196, '云南省', '保山市', 514.21);
INSERT INTO `demo_gdp_by_city` VALUES (197, '云南省', '普洱市', 449.18);
INSERT INTO `demo_gdp_by_city` VALUES (198, '云南省', '临沧市', 383.82);
INSERT INTO `demo_gdp_by_city` VALUES (199, '云南省', '西双版纳州', 307.87);
INSERT INTO `demo_gdp_by_city` VALUES (200, '云南省', '德宏州', 284.23);
INSERT INTO `demo_gdp_by_city` VALUES (201, '云南省', '丽江市', 264.16);
INSERT INTO `demo_gdp_by_city` VALUES (202, '云南省', '迪庆州', 143.27);
INSERT INTO `demo_gdp_by_city` VALUES (203, '云南省', '怒江州', 98.19);
INSERT INTO `demo_gdp_by_city` VALUES (204, '辽宁省', '大连市', 3632.40);
INSERT INTO `demo_gdp_by_city` VALUES (205, '辽宁省', '沈阳市', 3303.60);
INSERT INTO `demo_gdp_by_city` VALUES (206, '辽宁省', '鞍山市', 879.60);
INSERT INTO `demo_gdp_by_city` VALUES (207, '辽宁省', '盘锦市', 674.70);
INSERT INTO `demo_gdp_by_city` VALUES (208, '辽宁省', '营口市', 660.20);
INSERT INTO `demo_gdp_by_city` VALUES (209, '辽宁省', '锦州市', 533.60);
INSERT INTO `demo_gdp_by_city` VALUES (210, '辽宁省', '朝阳市', 422.40);
INSERT INTO `demo_gdp_by_city` VALUES (211, '辽宁省', '本溪市', 417.60);
INSERT INTO `demo_gdp_by_city` VALUES (212, '辽宁省', '抚顺市', 398.80);
INSERT INTO `demo_gdp_by_city` VALUES (213, '辽宁省', '辽阳市', 387.30);
INSERT INTO `demo_gdp_by_city` VALUES (214, '辽宁省', '丹东市', 381.60);
INSERT INTO `demo_gdp_by_city` VALUES (215, '辽宁省', '葫芦岛市', 375.10);
INSERT INTO `demo_gdp_by_city` VALUES (216, '辽宁省', '铁岭市', 322.40);
INSERT INTO `demo_gdp_by_city` VALUES (217, '辽宁省', '阜新市', 251.90);
INSERT INTO `demo_gdp_by_city` VALUES (218, '广西壮族自治区', '南宁市', 1199.24);
INSERT INTO `demo_gdp_by_city` VALUES (219, '广西壮族自治区', '柳州市', 742.12);
INSERT INTO `demo_gdp_by_city` VALUES (220, '广西壮族自治区', '桂林市', 541.48);
INSERT INTO `demo_gdp_by_city` VALUES (221, '广西壮族自治区', '玉林市', 441.76);
INSERT INTO `demo_gdp_by_city` VALUES (222, '广西壮族自治区', '贵港市', 368.41);
INSERT INTO `demo_gdp_by_city` VALUES (223, '广西壮族自治区', '百色市', 338.81);
INSERT INTO `demo_gdp_by_city` VALUES (224, '广西壮族自治区', '钦州市', 328.68);
INSERT INTO `demo_gdp_by_city` VALUES (225, '广西壮族自治区', '北海市', 326.35);
INSERT INTO `demo_gdp_by_city` VALUES (226, '广西壮族自治区', '梧州市', 255.16);
INSERT INTO `demo_gdp_by_city` VALUES (227, '广西壮族自治区', '河池市', 225.03);
INSERT INTO `demo_gdp_by_city` VALUES (228, '广西壮族自治区', '崇左市', 207.51);
INSERT INTO `demo_gdp_by_city` VALUES (229, '广西壮族自治区', '防城港市', 199.82);
INSERT INTO `demo_gdp_by_city` VALUES (230, '广西壮族自治区', '贺州市', 183.58);
INSERT INTO `demo_gdp_by_city` VALUES (231, '广西壮族自治区', '来宾市', 167.14);
INSERT INTO `demo_gdp_by_city` VALUES (232, '山西省', '太原市', 2181.81);
INSERT INTO `demo_gdp_by_city` VALUES (233, '山西省', '长治市', 978.10);
INSERT INTO `demo_gdp_by_city` VALUES (234, '山西省', '运城市', 869.60);
INSERT INTO `demo_gdp_by_city` VALUES (235, '山西省', '吕梁市', 837.50);
INSERT INTO `demo_gdp_by_city` VALUES (236, '山西省', '晋城市', 820.80);
INSERT INTO `demo_gdp_by_city` VALUES (237, '山西省', '临汾市', 798.10);
INSERT INTO `demo_gdp_by_city` VALUES (238, '山西省', '晋中市', 785.60);
INSERT INTO `demo_gdp_by_city` VALUES (239, '山西省', '大同市', 760.10);
INSERT INTO `demo_gdp_by_city` VALUES (240, '山西省', '朔州市', 592.30);
INSERT INTO `demo_gdp_by_city` VALUES (241, '山西省', '忻州市', 580.80);
INSERT INTO `demo_gdp_by_city` VALUES (242, '山西省', '阳泉市', 402.10);
INSERT INTO `demo_gdp_by_city` VALUES (243, '内蒙古自治区', '鄂尔多斯', 2039.89);
INSERT INTO `demo_gdp_by_city` VALUES (244, '内蒙古自治区', '包头', 1627.00);
INSERT INTO `demo_gdp_by_city` VALUES (245, '内蒙古自治区', '呼和浩特', 1539.00);
INSERT INTO `demo_gdp_by_city` VALUES (246, '内蒙古自治区', '赤峰', 848.30);
INSERT INTO `demo_gdp_by_city` VALUES (247, '内蒙古自治区', '呼伦贝尔', 502.42);
INSERT INTO `demo_gdp_by_city` VALUES (248, '内蒙古自治区', '锡林郭勒', 408.42);
INSERT INTO `demo_gdp_by_city` VALUES (249, '内蒙古自治区', '巴彦淖尔', 399.90);
INSERT INTO `demo_gdp_by_city` VALUES (250, '内蒙古自治区', '乌兰察布', 397.40);
INSERT INTO `demo_gdp_by_city` VALUES (251, '内蒙古自治区', '乌海', 319.45);
INSERT INTO `demo_gdp_by_city` VALUES (252, '内蒙古自治区', '兴安', 231.06);
INSERT INTO `demo_gdp_by_city` VALUES (253, '贵州省', '贵阳市', 2042.15);
INSERT INTO `demo_gdp_by_city` VALUES (254, '贵州省', '遵义市', 1971.75);
INSERT INTO `demo_gdp_by_city` VALUES (255, '贵州省', '毕节市', 1019.23);
INSERT INTO `demo_gdp_by_city` VALUES (256, '贵州省', '黔南市', 838.60);
INSERT INTO `demo_gdp_by_city` VALUES (257, '贵州省', '六盘水市', 728.22);
INSERT INTO `demo_gdp_by_city` VALUES (258, '贵州省', '铜仁市', 702.01);
INSERT INTO `demo_gdp_by_city` VALUES (259, '贵州省', '黔西南', 683.28);
INSERT INTO `demo_gdp_by_city` VALUES (260, '贵州省', '黔东南', 549.44);
INSERT INTO `demo_gdp_by_city` VALUES (261, '贵州省', '安顺市', 540.80);
INSERT INTO `demo_gdp_by_city` VALUES (262, '天津市', '天津市', 7309.00);
INSERT INTO `demo_gdp_by_city` VALUES (263, '吉林省', '长春市', 3435.51);
INSERT INTO `demo_gdp_by_city` VALUES (264, '吉林省', '吉林市', 698.05);
INSERT INTO `demo_gdp_by_city` VALUES (265, '吉林省', '延边州', 379.85);
INSERT INTO `demo_gdp_by_city` VALUES (266, '吉林省', '松原市', 342.26);
INSERT INTO `demo_gdp_by_city` VALUES (267, '吉林省', '通化市', 337.79);
INSERT INTO `demo_gdp_by_city` VALUES (268, '吉林省', '四平市', 255.27);
INSERT INTO `demo_gdp_by_city` VALUES (269, '吉林省', '白城市', 220.47);
INSERT INTO `demo_gdp_by_city` VALUES (270, '吉林省', '辽源市', 209.80);
INSERT INTO `demo_gdp_by_city` VALUES (271, '吉林省', '白山市', 196.01);
INSERT INTO `demo_gdp_by_city` VALUES (272, '黑龙江省', NULL, 5990.00);
INSERT INTO `demo_gdp_by_city` VALUES (273, '甘肃省', '兰州市', 1628.39);
INSERT INTO `demo_gdp_by_city` VALUES (274, '甘肃省', '庆阳市', 373.30);
INSERT INTO `demo_gdp_by_city` VALUES (275, '甘肃省', '天水市', 333.80);
INSERT INTO `demo_gdp_by_city` VALUES (276, '甘肃省', '酒泉市', 326.10);
INSERT INTO `demo_gdp_by_city` VALUES (277, '甘肃省', '白银市', 263.08);
INSERT INTO `demo_gdp_by_city` VALUES (278, '甘肃省', '武威市', 256.12);
INSERT INTO `demo_gdp_by_city` VALUES (279, '甘肃省', '陇南市', 238.00);
INSERT INTO `demo_gdp_by_city` VALUES (280, '甘肃省', '平凉市', 233.20);
INSERT INTO `demo_gdp_by_city` VALUES (281, '甘肃省', '张掖市', 232.95);
INSERT INTO `demo_gdp_by_city` VALUES (282, '甘肃省', '定西市', 215.10);
INSERT INTO `demo_gdp_by_city` VALUES (283, '甘肃省', '金昌市', 207.11);
INSERT INTO `demo_gdp_by_city` VALUES (284, '甘肃省', '临夏州', 169.90);
INSERT INTO `demo_gdp_by_city` VALUES (285, '甘肃省', '嘉峪关市', 158.00);
INSERT INTO `demo_gdp_by_city` VALUES (286, '甘肃省', '甘南州', 113.12);
INSERT INTO `demo_gdp_by_city` VALUES (287, '海南省', '海口市', 861.81);
INSERT INTO `demo_gdp_by_city` VALUES (288, '海南省', '三亚市', 399.80);
INSERT INTO `demo_gdp_by_city` VALUES (289, '海南省', '儋州市', 188.57);
INSERT INTO `demo_gdp_by_city` VALUES (290, '海南省', '洋浦', 185.03);
INSERT INTO `demo_gdp_by_city` VALUES (291, '海南省', '澄迈县', 184.92);
INSERT INTO `demo_gdp_by_city` VALUES (292, '海南省', '琼海市', 147.97);
INSERT INTO `demo_gdp_by_city` VALUES (293, '海南省', '文昌市', 145.41);
INSERT INTO `demo_gdp_by_city` VALUES (294, '海南省', '万宁市', 126.81);
INSERT INTO `demo_gdp_by_city` VALUES (295, '海南省', '临高县', 106.13);
INSERT INTO `demo_gdp_by_city` VALUES (296, '海南省', '东方市', 104.26);
INSERT INTO `demo_gdp_by_city` VALUES (297, '海南省', '陵水县', 101.74);
INSERT INTO `demo_gdp_by_city` VALUES (298, '海南省', '乐东县', 79.02);
INSERT INTO `demo_gdp_by_city` VALUES (299, '海南省', '昌江县', 65.56);
INSERT INTO `demo_gdp_by_city` VALUES (300, '海南省', '定安县', 52.37);
INSERT INTO `demo_gdp_by_city` VALUES (301, '海南省', '屯昌县', 42.14);
INSERT INTO `demo_gdp_by_city` VALUES (302, '海南省', '保亭县', 26.87);
INSERT INTO `demo_gdp_by_city` VALUES (303, '海南省', '琼中县', 25.16);
INSERT INTO `demo_gdp_by_city` VALUES (304, '海南省', '白沙县', 24.87);
INSERT INTO `demo_gdp_by_city` VALUES (305, '海南省', '五指山市', 15.87);
INSERT INTO `demo_gdp_by_city` VALUES (306, '青海省', NULL, 1557.00);
INSERT INTO `demo_gdp_by_city` VALUES (307, '新疆维吾尔自治区', NULL, 7329.00);
INSERT INTO `demo_gdp_by_city` VALUES (308, '西藏自治区', NULL, 926.05);
COMMIT;

-- ----------------------------
-- Table structure for demo_gdp_by_city_top10
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_by_city_top10`;
CREATE TABLE `demo_gdp_by_city_top10` (
  `id` int(11) NOT NULL DEFAULT '0',
  `province` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `gdp` double(255,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_by_city_top10
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_by_city_top10` VALUES (151, '上海市', '上海市', 20102.00);
INSERT INTO `demo_gdp_by_city_top10` VALUES (153, '北京市', '北京市', 19228.00);
INSERT INTO `demo_gdp_by_city_top10` VALUES (1, '广东省', '深圳市', 14324.47);
INSERT INTO `demo_gdp_by_city_top10` VALUES (2, '广东省', '广州市', 13101.89);
INSERT INTO `demo_gdp_by_city_top10` VALUES (152, '重庆市', '重庆市', 12903.00);
INSERT INTO `demo_gdp_by_city_top10` VALUES (22, '江苏省', '苏州市', 10684.66);
INSERT INTO `demo_gdp_by_city_top10` VALUES (75, '四川省', '成都市', 9602.72);
INSERT INTO `demo_gdp_by_city_top10` VALUES (51, '浙江省', '杭州市', 8656.03);
INSERT INTO `demo_gdp_by_city_top10` VALUES (105, '湖北省', '武汉市', 8251.50);
INSERT INTO `demo_gdp_by_city_top10` VALUES (23, '江苏省', '南京市', 7622.77);
COMMIT;

-- ----------------------------
-- Table structure for demo_gdp_by_industry
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_by_industry`;
CREATE TABLE `demo_gdp_by_industry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `industry` varchar(255) DEFAULT NULL,
  `GDP` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_by_industry
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_by_industry` VALUES (1, '第一产业', '28402');
INSERT INTO `demo_gdp_by_industry` VALUES (2, '第二产业', '207154');
INSERT INTO `demo_gdp_by_industry` VALUES (3, '第三产业', '296611');
COMMIT;

-- ----------------------------
-- Table structure for demo_gdp_district_top100
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_district_top100`;
CREATE TABLE `demo_gdp_district_top100` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `province` varchar(255) DEFAULT NULL,
  `num` int(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_district_top100
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_district_top100` VALUES (1, '江苏省', 26);
INSERT INTO `demo_gdp_district_top100` VALUES (2, '山东省', 13);
INSERT INTO `demo_gdp_district_top100` VALUES (3, '福建省', 9);
INSERT INTO `demo_gdp_district_top100` VALUES (4, '浙江省', 18);
INSERT INTO `demo_gdp_district_top100` VALUES (5, '广东省', 4);
INSERT INTO `demo_gdp_district_top100` VALUES (6, '湖南省', 5);
INSERT INTO `demo_gdp_district_top100` VALUES (7, '湖北省', 7);
INSERT INTO `demo_gdp_district_top100` VALUES (8, '河南省', 7);
INSERT INTO `demo_gdp_district_top100` VALUES (9, '新疆维吾尔族自治区', 1);
INSERT INTO `demo_gdp_district_top100` VALUES (10, '安徽省', 2);
INSERT INTO `demo_gdp_district_top100` VALUES (11, '河北省', 2);
INSERT INTO `demo_gdp_district_top100` VALUES (12, '内蒙古自治区', 2);
INSERT INTO `demo_gdp_district_top100` VALUES (13, '辽宁省', 1);
INSERT INTO `demo_gdp_district_top100` VALUES (14, '江西省', 1);
INSERT INTO `demo_gdp_district_top100` VALUES (15, '陕西省', 1);
INSERT INTO `demo_gdp_district_top100` VALUES (16, '贵州省', 1);
COMMIT;

-- ----------------------------
-- Table structure for demo_gdp_history
-- ----------------------------
DROP TABLE IF EXISTS `demo_gdp_history`;
CREATE TABLE `demo_gdp_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `year` varchar(255) DEFAULT NULL,
  `gdp` double(255,2) DEFAULT NULL,
  `percent` double(255,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of demo_gdp_history
-- ----------------------------
BEGIN;
INSERT INTO `demo_gdp_history` VALUES (1, '2020', 14.72, 17.38);
INSERT INTO `demo_gdp_history` VALUES (2, '2019', 14.28, 16.46);
INSERT INTO `demo_gdp_history` VALUES (3, '2018', 13.89, 16.41);
INSERT INTO `demo_gdp_history` VALUES (4, '2017', 12.31, 15.59);
INSERT INTO `demo_gdp_history` VALUES (5, '2016', 11.23, 15.29);
INSERT INTO `demo_gdp_history` VALUES (6, '2015', 11.06, 15.44);
INSERT INTO `demo_gdp_history` VALUES (7, '2014', 10.48, 13.97);
INSERT INTO `demo_gdp_history` VALUES (8, '2013', 9.57, 13.24);
INSERT INTO `demo_gdp_history` VALUES (9, '2012', 8.53, 12.26);
INSERT INTO `demo_gdp_history` VALUES (10, '2011', 7.55, 11.20);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO `panel_group` (`id`, `name`, `pid`, `level`, `node_type`, `create_by`, `create_time`, `panel_type`, `panel_style`, `panel_data`, `source`, `extend1`, `extend2`, `remark`) VALUES ('117f679e-8355-4645-a692-47e2009cbc0d', '2021年全国GDP数据', '308b5d60-ceb4-465d-9bad-cf901f2d38f1', 1, 'panel', 'admin', 1629365845683, 'self', '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":false,\"openCommonStyle\":true,\"panel\":{\"color\":\"#f2f2f2\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\"},\"chart\":{\"xaxis\":\"[]\",\"yaxis\":\"[]\",\"show\":true,\"type\":\"panel\",\"title\":\"\",\"customAttr\":\"{\\\"color\\\":{\\\"value\\\":\\\"default\\\",\\\"colors\\\":[\\\"#5470c6\\\",\\\"#91cc75\\\",\\\"#fac858\\\",\\\"#ee6666\\\",\\\"#73c0de\\\",\\\"#3ba272\\\",\\\"#fc8452\\\",\\\"#9a60b4\\\",\\\"#ea7ccc\\\"],\\\"alpha\\\":100,\\\"tableHeaderBgColor\\\":\\\"#4e81bb\\\",\\\"tableItemBgColor\\\":\\\"#c6d9f0\\\",\\\"tableFontColor\\\":\\\"#000000\\\",\\\"tableStripe\\\":true,\\\"dimensionColor\\\":\\\"#000000\\\",\\\"quotaColor\\\":\\\"#000000\\\"},\\\"tableColor\\\":{\\\"value\\\":\\\"default\\\",\\\"colors\\\":[\\\"#5470c6\\\",\\\"#91cc75\\\",\\\"#fac858\\\",\\\"#ee6666\\\",\\\"#73c0de\\\",\\\"#3ba272\\\",\\\"#fc8452\\\",\\\"#9a60b4\\\",\\\"#ea7ccc\\\"],\\\"alpha\\\":100,\\\"tableHeaderBgColor\\\":\\\"#4e81bb\\\",\\\"tableItemBgColor\\\":\\\"#c6d9f0\\\",\\\"tableFontColor\\\":\\\"#000000\\\",\\\"tableStripe\\\":true,\\\"dimensionColor\\\":\\\"#000000\\\",\\\"quotaColor\\\":\\\"#000000\\\"},\\\"size\\\":{\\\"barDefault\\\":true,\\\"barWidth\\\":40,\\\"barGap\\\":0.4,\\\"lineWidth\\\":1,\\\"lineType\\\":\\\"solid\\\",\\\"lineSymbol\\\":\\\"emptyCircle\\\",\\\"lineSymbolSize\\\":4,\\\"lineSmooth\\\":false,\\\"lineArea\\\":false,\\\"pieInnerRadius\\\":0,\\\"pieOuterRadius\\\":60,\\\"pieRoseType\\\":\\\"radius\\\",\\\"pieRoseRadius\\\":5,\\\"funnelWidth\\\":80,\\\"radarShape\\\":\\\"polygon\\\",\\\"tableTitleFontSize\\\":12,\\\"tableItemFontSize\\\":12,\\\"gaugeMin\\\":0,\\\"gaugeMax\\\":100,\\\"gaugeStartAngle\\\":225,\\\"gaugeEndAngle\\\":-45,\\\"dimensionFontSize\\\":18,\\\"quotaFontSize\\\":18,\\\"spaceSplit\\\":10,\\\"dimensionShow\\\":true,\\\"quotaShow\\\":true},\\\"label\\\":{\\\"show\\\":false,\\\"position\\\":\\\"top\\\",\\\"color\\\":\\\"#909399\\\",\\\"fontSize\\\":\\\"10\\\",\\\"formatter\\\":\\\"{c}\\\",\\\"gaugeFormatter\\\":\\\"{value}\\\"},\\\"tooltip\\\":{\\\"show\\\":true,\\\"trigger\\\":\\\"item\\\",\\\"confine\\\":true,\\\"textStyle\\\":{\\\"fontSize\\\":\\\"10\\\",\\\"color\\\":\\\"#909399\\\"},\\\"formatter\\\":\\\"\\\"}}\",\"customStyle\":\"{\\\"text\\\":{\\\"show\\\":true,\\\"fontSize\\\":\\\"18\\\",\\\"color\\\":\\\"#303133\\\",\\\"hPosition\\\":\\\"center\\\",\\\"vPosition\\\":\\\"top\\\",\\\"isItalic\\\":false},\\\"legend\\\":{\\\"show\\\":true,\\\"hPosition\\\":\\\"center\\\",\\\"vPosition\\\":\\\"bottom\\\",\\\"orient\\\":\\\"horizontal\\\",\\\"icon\\\":\\\"rect\\\",\\\"textStyle\\\":{\\\"color\\\":\\\"#333333\\\",\\\"fontSize\\\":\\\"12\\\"}},\\\"xAxis\\\":{\\\"show\\\":true,\\\"position\\\":\\\"bottom\\\",\\\"name\\\":\\\"\\\",\\\"axisLabel\\\":{\\\"rotate\\\":0,\\\"formatter\\\":\\\"{value}\\\"}},\\\"yAxis\\\":{\\\"show\\\":true,\\\"position\\\":\\\"left\\\",\\\"name\\\":\\\"\\\",\\\"axisLabel\\\":{\\\"rotate\\\":0,\\\"formatter\\\":\\\"{value}\\\"}},\\\"background\\\":{\\\"color\\\":\\\"#ffffff\\\",\\\"alpha\\\":100}}\",\"customFilter\":\"[]\"}}', '[{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869200-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"aeaab270-00d1-11ec-80e7-bdd9acf54588\",\"viewId\":\"84b444e1-0088-44f9-acdc-cc39018413bc\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":666.6666666666666,\"height\":700,\"borderRadius\":\"\",\"top\":199.12012290502793,\"left\":400},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869201-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"065b0190-017d-11ec-9d73-ddd3819ba45c\",\"viewId\":\"c68db172-2df2-4aa2-aad6-077cf1684e14\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":533.3333333333334,\"height\":437.5,\"borderRadius\":\"\",\"top\":462.5,\"left\":1066.6666666666667},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869202-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"9707c630-0185-11ec-9d73-ddd3819ba45c\",\"viewId\":\"f8d62b2b-b99a-4b6c-8378-d7c2ec4ea766\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":400,\"height\":375.00000000000006,\"borderRadius\":\"\",\"top\":87.5,\"left\":0},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869203-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"cdf75d00-0198-11ec-9d73-ddd3819ba45c\",\"viewId\":\"c4943403-4960-4ad8-a9c5-12c46c538c34\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":400,\"height\":437.5,\"borderRadius\":\"\",\"top\":462.5,\"left\":0},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869204-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"c941a7b0-0199-11ec-9d73-ddd3819ba45c\",\"viewId\":\"f257452d-6fc1-4499-bdce-bd10b3e1c520\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":222.06896551724134,\"height\":112.5,\"borderRadius\":\"\",\"top\":87.48603351955308,\"left\":401.37931034482756},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869205-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"ca215950-0199-11ec-9d73-ddd3819ba45c\",\"viewId\":\"8271c4e4-43ab-48c6-b7b4-67ccaba3f80b\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":221.1494252873563,\"height\":111.49441340782123,\"borderRadius\":\"\",\"top\":87.50001117318438,\"left\":623.448275862069},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869206-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"cb8f8f50-0199-11ec-9d73-ddd3819ba45c\",\"viewId\":\"a0058881-b29f-4b5c-911f-7f1480b07eb0\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":222.06896551724137,\"height\":109.4832402234637,\"borderRadius\":\"\",\"top\":87.50001117318438,\"left\":844.5977011494252},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869207-0589-11ec-a520-7bcd081cd375\",\"component\":\"user-view\",\"label\":\"用户视图\",\"propValue\":{\"id\":\"8a63b510-01a3-11ec-a6e2-cd9d021b3db5\",\"viewId\":\"c36cd358-0501-4f83-a323-f754485d00b1\"},\"icon\":\"juxing\",\"type\":\"view\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":533.3333333333334,\"height\":375.00000000000006,\"borderRadius\":\"\",\"top\":87.5,\"left\":1066.6666666666667},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869208-0589-11ec-a520-7bcd081cd375\",\"component\":\"rect-shape\",\"label\":\"矩形\",\"propValue\":\"\",\"icon\":\"juxing\",\"type\":\"rect-shape\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":1598.16091954023,\"height\":87.5,\"borderStyle\":\"solid\",\"borderWidth\":\"0\",\"borderColor\":\"#000000\",\"backgroundColor\":\"#FFFFFF\",\"borderRadius\":\"10\",\"top\":0.000011173184367439641,\"left\":1.839080459770115},\"filters\":[],\"linkageFilters\":[]},{\"animations\":[],\"events\":{},\"groupStyle\":{},\"isLock\":false,\"id\":\"97869209-0589-11ec-a520-7bcd081cd375\",\"component\":\"v-text\",\"label\":\"文字\",\"propValue\":\"2021年全国GDP数据\",\"icon\":\"wenben\",\"type\":\"v-text\",\"style\":{\"rotate\":0,\"opacity\":1,\"width\":332.8735632183908,\"height\":37.50000000000001,\"fontSize\":\"28\",\"fontWeight\":\"400\",\"lineHeight\":\"\",\"letterSpacing\":0,\"textAlign\":\"center\",\"color\":\"#000000\",\"top\":22.39423508379888,\"left\":603.2183908045977},\"filters\":[],\"linkageFilters\":[]}]', NULL, NULL, NULL, NULL);


INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583dc0d-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', '84b444e1-0088-44f9-acdc-cc39018413bc', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583deb5-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'c68db172-2df2-4aa2-aad6-077cf1684e14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583df2c-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'f8d62b2b-b99a-4b6c-8378-d7c2ec4ea766', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583df74-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'c4943403-4960-4ad8-a9c5-12c46c538c34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583dfbe-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'f257452d-6fc1-4499-bdce-bd10b3e1c520', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583e00f-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', '8271c4e4-43ab-48c6-b7b4-67ccaba3f80b', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583e053-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'a0058881-b29f-4b5c-911f-7f1480b07eb0', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `panel_view` (`id`, `panel_id`, `chart_view_id`, `content`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('7583e091-058d-11ec-a2b0-0242ac130003', '117f679e-8355-4645-a692-47e2009cbc0d', 'c36cd358-0501-4f83-a323-f754485d00b1', NULL, NULL, NULL, NULL, NULL);



INSERT INTO `chart_group` (`id`, `name`, `pid`, `level`, `type`, `create_by`, `create_time`) VALUES ('bc7542d8-2b7e-4909-81ff-3627b0227501', '全国GDP', '3f551269-d985-4633-884d-d118704da2db', 1, 'group', 'admin', 1629365979872);

INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('84b444e1-0088-44f9-acdc-cc39018413bc', '今年上半年GDP（亿元）', 'bc7542d8-2b7e-4909-81ff-3627b0227501', 'caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb', 'map', '今年上半年GDP（亿元）', '[{\"id\":\"680e8e5a-58c8-4760-b151-9e3c0b998621\",\"tableId\":\"caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb\",\"originName\":\"province\",\"name\":\"province\",\"dataeaseName\":\"C_53aad639aca4b5c010927cf610c3ff9c\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":0,\"lastSyncTime\":1629365603090,\"dateStyle\":\"y_M_d\",\"datePattern\":\"date_sub\",\"sort\":\"none\",\"filter\":[]}]', '[{\"id\":\"4042c7a1-d3c0-4877-9146-b4af2da86a84\",\"tableId\":\"caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb\",\"originName\":\"gdp\",\"name\":\"gdp\",\"dataeaseName\":\"C_e785c0883d7a104330e69aee73d4f235\",\"groupType\":\"q\",\"type\":\"FLOAT\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":3,\"extField\":0,\"checked\":true,\"columnIndex\":2,\"lastSyncTime\":1629365603090,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[]}]', '[]', '[]', '{\"color\":{\"value\":\"energy\",\"colors\":[\"#FCF4E5\",\"#F8E7C6\",\"#FFE2AD\",\"#FACB75\",\"#FABC4B\",\"#FCB022\",\"#FFA500\",\"#E29201\",\"#B77702\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":18,\"quotaFontSize\":18,\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":true,\"position\":\"top\",\"color\":\"#000000\",\"fontSize\":\"10\",\"formatter\":\"{b}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#070707\"},\"formatter\":\"\"},\"areaCode\":\"100000\"}', '{\"text\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"今年上半年GDP（亿元）\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[{\"id\":\"680e8e5a-58c8-4760-b151-9e3c0b998621\",\"tableId\":\"caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb\",\"originName\":\"province\",\"name\":\"province\",\"dataeaseName\":\"C_53aad639aca4b5c010927cf610c3ff9c\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":0,\"lastSyncTime\":1629365603090},{\"id\":\"df68fb8f-0e7e-4b69-90e6-28550f955ebf\",\"tableId\":\"caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb\",\"originName\":\"city\",\"name\":\"city\",\"dataeaseName\":\"C_4ed5d2eaed1a1fadcc41ad1d58ed603e\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":1,\"lastSyncTime\":1629365603090}]', 'admin', 1629366111909, 1629883473724, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('c68db172-2df2-4aa2-aad6-077cf1684e14', '历年GDP数据', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '4441c859-2e1b-44a0-a0af-4775c93300ee', 'line', '历年GDP数据', '[{\"id\":\"187443f5-9a64-4e9c-9fd5-6aa993c48352\",\"tableId\":\"4441c859-2e1b-44a0-a0af-4775c93300ee\",\"originName\":\"year\",\"name\":\"year\",\"dataeaseName\":\"C_84cdc76cabf41bd7c961f6ab12f117d8\",\"groupType\":\"d\",\"type\":\"INT\",\"size\":10,\"deType\":2,\"deTypeFormat\":null,\"deExtractType\":2,\"extField\":0,\"checked\":true,\"columnIndex\":0,\"lastSyncTime\":1629365603157,\"dateStyle\":\"y_M_d\",\"datePattern\":\"date_sub\",\"sort\":\"none\",\"filter\":[]}]', '[{\"id\":\"d9ff23cc-6303-4653-a1a6-130cd8d3f305\",\"tableId\":\"4441c859-2e1b-44a0-a0af-4775c93300ee\",\"originName\":\"gdp\",\"name\":\"GDP（万亿）\",\"dataeaseName\":\"C_e785c0883d7a104330e69aee73d4f235\",\"groupType\":\"q\",\"type\":\"FLOAT\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":3,\"extField\":0,\"checked\":true,\"columnIndex\":1,\"lastSyncTime\":1629365603157,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[]', '{\"color\":{\"value\":\"energy\",\"colors\":[\"#ef8b07\",\"#2a83a2\",\"#f07474\",\"#c55784\",\"#274a78\",\"#7058a3\",\"#0095d9\",\"#75c24b\",\"#808080\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":true,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":18,\"quotaFontSize\":18,\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":true,\"position\":\"top\",\"color\":\"#000000\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"历年GDP数据\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629424803096, 1629875503544, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('f8d62b2b-b99a-4b6c-8378-d7c2ec4ea766', '今年上半年GDP产业分布（亿元）', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '99f30713-29ab-4378-941c-bb99182d927a', 'pie', '今年上半年GDP产业分布（亿元）', '[{\"id\":\"bafcb837-24a2-4f94-9d6b-7ce163bd7fbc\",\"tableId\":\"99f30713-29ab-4378-941c-bb99182d927a\",\"originName\":\"industry\",\"name\":\"industry\",\"dataeaseName\":\"C_94074ce331b5db47060fd2d8009503a8\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":1,\"lastSyncTime\":1629442921534,\"dateStyle\":\"y_M_d\",\"datePattern\":\"date_sub\",\"sort\":\"none\",\"filter\":[]}]', '[{\"id\":\"8c216055-17c7-4a8e-8fce-a66ffe9e5bfc\",\"tableId\":\"99f30713-29ab-4378-941c-bb99182d927a\",\"originName\":\"GDP\",\"name\":\"GDP\",\"dataeaseName\":\"C_35c9f5040cfbb586586ac8cb8f0b06a6\",\"groupType\":\"q\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":2,\"lastSyncTime\":1629442921534,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[]}]', '[]', '[]', '{\"color\":{\"value\":\"energy\",\"colors\":[\"#ef8b07\",\"#F5C021\",\"#FAE37C\",\"#c55784\",\"#274a78\",\"#7058a3\",\"#0095d9\",\"#75c24b\",\"#808080\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":38,\"pieOuterRadius\":55,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":18,\"quotaFontSize\":18,\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":true,\"position\":\"outside\",\"color\":\"#000000\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"今年上半年GDP产业分布（亿元）\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629442969160, 1629875435989, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('c4943403-4960-4ad8-a9c5-12c46c538c34', 'GDP前十强城市', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '5d91825c-8a0a-4c90-aba1-f58cff216561', 'bar-horizontal', 'GDP前十强城市', '[{\"id\":\"30d6a427-95af-4478-93ff-81511023d193\",\"tableId\":\"5d91825c-8a0a-4c90-aba1-f58cff216561\",\"originName\":\"city\",\"name\":\"city\",\"dataeaseName\":\"C_4ed5d2eaed1a1fadcc41ad1d58ed603e\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":2,\"lastSyncTime\":1629451475515,\"dateStyle\":\"y_M_d\",\"datePattern\":\"date_sub\",\"sort\":\"none\",\"filter\":[]}]', '[{\"id\":\"b30e9982-91fb-41ae-9e69-2ce7e2717cf6\",\"tableId\":\"5d91825c-8a0a-4c90-aba1-f58cff216561\",\"originName\":\"gdp\",\"name\":\"GDP（亿元）\",\"dataeaseName\":\"C_e785c0883d7a104330e69aee73d4f235\",\"groupType\":\"q\",\"type\":\"DOUBLE\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":3,\"extField\":0,\"checked\":true,\"columnIndex\":3,\"lastSyncTime\":1629451475515,\"summary\":\"sum\",\"sort\":\"asc\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[]', '{\"color\":{\"value\":\"energy\",\"colors\":[\"#F8B65E\",\"#2a83a2\",\"#f07474\",\"#c55784\",\"#274a78\",\"#7058a3\",\"#0095d9\",\"#75c24b\",\"#808080\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":18,\"quotaFontSize\":18,\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":true,\"position\":\"right\",\"color\":\"#000000\",\"fontSize\":\"12\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"GDP前十强城市\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":false,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629451634257, 1629883342892, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('f257452d-6fc1-4499-bdce-bd10b3e1c520', '2021年GDP总值', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'text', '2021年GDP总值', '[]', '[{\"id\":\"58a7ee7d-6753-400a-a22b-1aa7d16b1648\",\"tableId\":\"3e1cf23c-5620-4a59-93b0-e4cae81b47ae\",\"originName\":\"2021GDP）\",\"name\":\"今年上半年GDP（亿元）\",\"dataeaseName\":\"C_e62745cee7a4ffbf5dbfa70cdd73c7b7\",\"groupType\":\"q\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":1,\"lastSyncTime\":1629442921465,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[]', '{\"color\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#807F7F\",\"quotaColor\":\"#FF0000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":\"16\",\"quotaFontSize\":\"22\",\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":false,\"position\":\"top\",\"color\":\"#909399\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":false,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"center\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"2021年GDP总值\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629448071499, 1629883000567, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('8271c4e4-43ab-48c6-b7b4-67ccaba3f80b', '2021年GDP总值-copy(1)', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'text', '2021年GDP总值-copy(1)', '[]', '[{\"id\":\"34199811-e74d-4f71-b88a-2716b18ac41f\",\"tableId\":\"3e1cf23c-5620-4a59-93b0-e4cae81b47ae\",\"originName\":\"2020GDP\",\"name\":\"去年上半年GDP（亿元）\",\"dataeaseName\":\"C_2ad1fe944da8f2d91df2181ed189d3df\",\"groupType\":\"q\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":2,\"lastSyncTime\":1629442921465,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[]', '{\"color\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#807F7F\",\"quotaColor\":\"#FF9900\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":\"16\",\"quotaFontSize\":\"22\",\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":false,\"position\":\"top\",\"color\":\"#909399\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":false,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"center\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"2021年GDP总值\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629448071499, 1629882965236, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('a0058881-b29f-4b5c-911f-7f1480b07eb0', '2021年GDP总值-copy(1)-copy(1)', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'text', '2021年GDP总值-copy(1)-copy(1)', '[]', '[{\"id\":\"5358bb6f-9ddc-4f8d-8e68-c5ea030af0bb\",\"tableId\":\"3e1cf23c-5620-4a59-93b0-e4cae81b47ae\",\"originName\":\"increase\",\"name\":\"同比增长率（%）\",\"dataeaseName\":\"C_11198b294adbcf089f9d27990258fd22\",\"groupType\":\"q\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":3,\"lastSyncTime\":1629442921465,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[]', '{\"color\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#807F7F\",\"quotaColor\":\"#00CC00\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":\"16\",\"quotaFontSize\":\"22\",\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":false,\"position\":\"top\",\"color\":\"#909399\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"\"}}', '{\"text\":{\"show\":false,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"center\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false,\"title\":\"2021年GDP总值\"},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629448071499, 1629883021765, NULL, 'view');
INSERT INTO `chart_view` (`id`, `name`, `scene_id`, `table_id`, `type`, `title`, `x_axis`, `y_axis`, `ext_stack`, `ext_bubble`, `custom_attr`, `custom_style`, `custom_filter`, `drill_fields`, `create_by`, `create_time`, `update_time`, `snapshot`, `style_priority`) VALUES ('c36cd358-0501-4f83-a323-f754485d00b1', '全国百强县分布', 'bc7542d8-2b7e-4909-81ff-3627b0227501', '4593fccd-d4f5-49d5-a5eb-c3203910530e', 'scatter', '全国百强县分布', '[{\"id\":\"e5241686-2614-4aed-b018-ef80ad3621fc\",\"tableId\":\"4593fccd-d4f5-49d5-a5eb-c3203910530e\",\"originName\":\"province\",\"name\":\"province\",\"dataeaseName\":\"C_53aad639aca4b5c010927cf610c3ff9c\",\"groupType\":\"d\",\"type\":\"VARCHAR\",\"size\":255,\"deType\":0,\"deTypeFormat\":null,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":0,\"lastSyncTime\":1629456058536,\"dateStyle\":\"y_M_d\",\"datePattern\":\"date_sub\",\"sort\":\"none\",\"filter\":[]}]', '[{\"id\":\"3c895946-a611-4110-9a46-df7291b55159\",\"tableId\":\"4593fccd-d4f5-49d5-a5eb-c3203910530e\",\"originName\":\"num\",\"name\":\"GDP（亿元）\",\"dataeaseName\":\"C_0fc3cfbc27e91ea60a787de13dae3e3c\",\"groupType\":\"q\",\"type\":\"INT\",\"size\":10,\"deType\":2,\"deTypeFormat\":null,\"deExtractType\":2,\"extField\":0,\"checked\":true,\"columnIndex\":1,\"lastSyncTime\":1629456058536,\"summary\":\"sum\",\"sort\":\"none\",\"filter\":[],\"index\":0,\"renameType\":\"quota\"}]', '[]', '[{\"id\":\"ffbea0d3-bf90-4ccd-8f34-55a8c4651525\",\"tableId\":\"4593fccd-d4f5-49d5-a5eb-c3203910530e\",\"originName\":\"gdp\",\"name\":\"gdp\",\"dataeaseName\":\"C_e785c0883d7a104330e69aee73d4f235\",\"groupType\":\"q\",\"type\":\"DOUBLE\",\"size\":19,\"deType\":3,\"deTypeFormat\":null,\"deExtractType\":3,\"extField\":0,\"checked\":true,\"columnIndex\":2,\"lastSyncTime\":1629456058536,\"summary\":\"sum\"}]', '{\"color\":{\"value\":\"fast\",\"colors\":[\"#F4E526\",\"#00c039\",\"#0482dc\",\"#bb9581\",\"#ff7701\",\"#9c5ec3\",\"#00ccdf\",\"#00c039\",\"#ff7701\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"tableColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#4e81bb\",\"tableItemBgColor\":\"#c6d9f0\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#000000\"},\"size\":{\"barDefault\":true,\"barWidth\":40,\"barGap\":0.4,\"lineWidth\":1,\"lineType\":\"solid\",\"lineSymbol\":\"emptyCircle\",\"lineSymbolSize\":4,\"lineSmooth\":false,\"lineArea\":false,\"pieInnerRadius\":0,\"pieOuterRadius\":80,\"pieRoseType\":\"radius\",\"pieRoseRadius\":5,\"funnelWidth\":80,\"radarShape\":\"polygon\",\"tableTitleFontSize\":12,\"tableItemFontSize\":12,\"tableTitleHeight\":36,\"tableItemHeight\":36,\"gaugeMin\":0,\"gaugeMax\":100,\"gaugeStartAngle\":225,\"gaugeEndAngle\":-45,\"dimensionFontSize\":18,\"quotaFontSize\":18,\"spaceSplit\":10,\"dimensionShow\":true,\"quotaShow\":true,\"scatterSymbol\":\"circle\",\"scatterSymbolSize\":20,\"treemapWidth\":80,\"treemapHeight\":80},\"label\":{\"show\":false,\"position\":\"top\",\"color\":\"#909399\",\"fontSize\":\"10\",\"formatter\":\"{c}\",\"gaugeFormatter\":\"{value}\",\"labelLine\":{\"show\":true}},\"tooltip\":{\"show\":true,\"trigger\":\"item\",\"confine\":true,\"textStyle\":{\"fontSize\":\"10\",\"color\":\"#909399\"},\"formatter\":\"{c}\"}}', '{\"text\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#303133\",\"hPosition\":\"center\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":false},\"legend\":{\"show\":true,\"hPosition\":\"center\",\"vPosition\":\"bottom\",\"orient\":\"horizontal\",\"icon\":\"rect\",\"textStyle\":{\"color\":\"#333333\",\"fontSize\":\"12\"}},\"xAxis\":{\"show\":true,\"position\":\"bottom\",\"name\":\"\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":false,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"yAxis\":{\"show\":true,\"position\":\"left\",\"name\":\"百强县个数\",\"nameTextStyle\":{\"color\":\"#333333\",\"fontSize\":12},\"axisLabel\":{\"show\":true,\"color\":\"#333333\",\"fontSize\":\"12\",\"rotate\":0,\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#cccccc\",\"width\":1,\"style\":\"solid\"}}},\"background\":{\"color\":\"#ffffff\",\"alpha\":100,\"borderRadius\":10},\"split\":{\"name\":{\"show\":true,\"color\":\"#999999\",\"fontSize\":\"12\"},\"splitNumber\":5,\"axisLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisTick\":{\"show\":false,\"length\":5,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"axisLabel\":{\"show\":false,\"rotate\":0,\"margin\":8,\"color\":\"#999999\",\"fontSize\":\"12\",\"formatter\":\"{value}\"},\"splitLine\":{\"show\":true,\"lineStyle\":{\"color\":\"#999999\",\"width\":1,\"type\":\"solid\"}},\"splitArea\":{\"show\":true}}}', '[]', '[]', 'admin', 1629456113105, 1629875460716, NULL, 'view');


INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb', 'demo_demo_gdp_by_city', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'db', 0, '{\"table\":\"demo_gdp_by_city\"}', 'admin', 1629365603057, NULL, NULL, NULL);
INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('4441c859-2e1b-44a0-a0af-4775c93300ee', 'demo_demo_gdp_history', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'db', 0, '{\"table\":\"demo_gdp_history\"}', 'admin', 1629365603122, NULL, NULL, NULL);
INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('99f30713-29ab-4378-941c-bb99182d927a', 'demo_demo_gdp_by_industry', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'db', 0, '{\"table\":\"demo_gdp_by_industry\"}', 'admin', 1629442921499, NULL, NULL, NULL);
INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('5d91825c-8a0a-4c90-aba1-f58cff216561', 'demo_demo_gdp_by_city_top10', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'db', 0, '{\"table\":\"demo_gdp_by_city_top10\"}', 'admin', 1629451475459, NULL, NULL, NULL);
INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'demo_demo_gdp_2021', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'db', 0, '{\"table\":\"demo_gdp_2021\"}', 'admin', 1629442921428, NULL, NULL, NULL);
INSERT INTO `dataset_table` (`id`, `name`, `scene_id`, `data_source_id`, `type`, `mode`, `info`, `create_by`, `create_time`, `qrtz_instance`, `sync_status`, `last_update_time`) VALUES ('4593fccd-d4f5-49d5-a5eb-c3203910530e', 'demo_gdp_district_top10', '8045c7b0-8cf5-4b6a-87f8-53180bbee657', '76026997-94f9-4a35-96ca-151084638969', 'sql', 0, '{\"sql\":\"select a.province,a.num,b.gdp from demo_gdp_district_top100 as a\\nleft join (select province,sum(gdp) gdp from demo_gdp_by_city group by province) as b\\non a.province=b.province\"}', 'admin', 1629456058501, NULL, NULL, NULL);

INSERT INTO `dataset_group` (`id`, `name`, `pid`, `level`, `type`, `create_by`, `create_time`) VALUES ('8045c7b0-8cf5-4b6a-87f8-53180bbee657', '全国GDP', 'f0728785-cc9a-4c9f-94ca-5bb106534916', 1, 'group', 'admin', 1629365580319);



INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('4042c7a1-d3c0-4877-9146-b4af2da86a84', 'caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb', 'gdp', 'gdp', 'C_e785c0883d7a104330e69aee73d4f235', 'q', 'FLOAT', 255, 3, NULL, 3, 0, 1, 2, 1629365603090);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('680e8e5a-58c8-4760-b151-9e3c0b998621', 'caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb', 'province', 'province', 'C_53aad639aca4b5c010927cf610c3ff9c', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 0, 1629365603090);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('df68fb8f-0e7e-4b69-90e6-28550f955ebf', 'caf2de01-1dd6-4ea5-a0aa-a53a6bb84ebb', 'city', 'city', 'C_4ed5d2eaed1a1fadcc41ad1d58ed603e', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 1, 1629365603090);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('187443f5-9a64-4e9c-9fd5-6aa993c48352', '4441c859-2e1b-44a0-a0af-4775c93300ee', 'year', 'year', 'C_84cdc76cabf41bd7c961f6ab12f117d8', 'd', 'INT', 10, 2, NULL, 2, 0, 1, 0, 1629365603157);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('300c5117-7ea3-4f06-bb45-600b8c42179c', '4441c859-2e1b-44a0-a0af-4775c93300ee', 'percent', 'percent', 'C_354f047ba64552895b016bbdd60ab174', 'q', 'FLOAT', 255, 3, NULL, 3, 0, 1, 2, 1629365603157);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('d9ff23cc-6303-4653-a1a6-130cd8d3f305', '4441c859-2e1b-44a0-a0af-4775c93300ee', 'gdp', 'gdp', 'C_e785c0883d7a104330e69aee73d4f235', 'q', 'FLOAT', 255, 3, NULL, 3, 0, 1, 1, 1629365603157);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('32642c98-ce65-482c-b6be-6a177297276a', '99f30713-29ab-4378-941c-bb99182d927a', 'id', 'id', 'C_b80bb7740288fda1f201890375a60c8f', 'q', 'INT', 10, 2, NULL, 2, 0, 1, 0, 1629442921534);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('8c216055-17c7-4a8e-8fce-a66ffe9e5bfc', '99f30713-29ab-4378-941c-bb99182d927a', 'GDP', 'GDP', 'C_35c9f5040cfbb586586ac8cb8f0b06a6', 'q', 'VARCHAR', 255, 3, NULL, 0, 0, 1, 2, 1629442921534);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('bafcb837-24a2-4f94-9d6b-7ce163bd7fbc', '99f30713-29ab-4378-941c-bb99182d927a', 'industry', 'industry', 'C_94074ce331b5db47060fd2d8009503a8', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 1, 1629442921534);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('30d6a427-95af-4478-93ff-81511023d193', '5d91825c-8a0a-4c90-aba1-f58cff216561', 'city', 'city', 'C_4ed5d2eaed1a1fadcc41ad1d58ed603e', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 2, 1629451475515);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('5205fbba-d3a1-41f7-a76e-b1837c3735b3', '5d91825c-8a0a-4c90-aba1-f58cff216561', 'id', 'id', 'C_b80bb7740288fda1f201890375a60c8f', 'q', 'INT', 10, 2, NULL, 2, 0, 1, 0, 1629451475515);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('7457904c-e8fe-4095-ab3c-1a868012c837', '5d91825c-8a0a-4c90-aba1-f58cff216561', 'province', 'province', 'C_53aad639aca4b5c010927cf610c3ff9c', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 1, 1629451475515);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('b30e9982-91fb-41ae-9e69-2ce7e2717cf6', '5d91825c-8a0a-4c90-aba1-f58cff216561', 'gdp', 'gdp', 'C_e785c0883d7a104330e69aee73d4f235', 'q', 'DOUBLE', 255, 3, NULL, 3, 0, 1, 3, 1629451475515);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('1c78e746-48aa-4e83-b3c8-e54296777467', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'id', 'id', 'C_b80bb7740288fda1f201890375a60c8f', 'q', 'INT', 10, 2, NULL, 2, 0, 1, 0, 1629442921465);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('34199811-e74d-4f71-b88a-2716b18ac41f', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', '2020GDP', '2020GDP', 'C_2ad1fe944da8f2d91df2181ed189d3df', 'q', 'VARCHAR', 255, 3, NULL, 0, 0, 1, 2, 1629442921465);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('5358bb6f-9ddc-4f8d-8e68-c5ea030af0bb', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', 'increase', 'increase', 'C_11198b294adbcf089f9d27990258fd22', 'q', 'VARCHAR', 255, 3, NULL, 0, 0, 1, 3, 1629442921465);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('58a7ee7d-6753-400a-a22b-1aa7d16b1648', '3e1cf23c-5620-4a59-93b0-e4cae81b47ae', '2021GDP）', '2021GDP）', 'C_e62745cee7a4ffbf5dbfa70cdd73c7b7', 'q', 'VARCHAR', 255, 3, NULL, 0, 0, 1, 1, 1629442921465);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('3c895946-a611-4110-9a46-df7291b55159', '4593fccd-d4f5-49d5-a5eb-c3203910530e', 'num', 'num', 'C_0fc3cfbc27e91ea60a787de13dae3e3c', 'q', 'INT', 10, 2, NULL, 2, 0, 1, 1, 1629456058536);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('e5241686-2614-4aed-b018-ef80ad3621fc', '4593fccd-d4f5-49d5-a5eb-c3203910530e', 'province', 'province', 'C_53aad639aca4b5c010927cf610c3ff9c', 'd', 'VARCHAR', 255, 0, NULL, 0, 0, 1, 0, 1629456058536);
INSERT INTO `dataset_table_field` (`id`, `table_id`, `origin_name`, `name`, `dataease_name`, `group_type`, `type`, `size`, `de_type`, `de_type_format`, `de_extract_type`, `ext_field`, `checked`, `column_index`, `last_sync_time`) VALUES ('ffbea0d3-bf90-4ccd-8f34-55a8c4651525', '4593fccd-d4f5-49d5-a5eb-c3203910530e', 'gdp', 'gdp', 'C_e785c0883d7a104330e69aee73d4f235', 'q', 'DOUBLE', 19, 3, NULL, 3, 0, 1, 2, 1629456058536);


