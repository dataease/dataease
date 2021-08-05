ALTER TABLE `chart_view` ADD COLUMN `ext_stack` LONGTEXT COMMENT '堆叠项' AFTER `y_axis`;
UPDATE `chart_view` SET `ext_stack` = '[]';

ALTER TABLE `dataset_table_field` MODIFY COLUMN `origin_name` LONGTEXT;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dataset_table_function
-- ----------------------------
DROP TABLE IF EXISTS `dataset_table_function`;
CREATE TABLE `dataset_table_function` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '函数名称',
  `func` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '函数表达式',
  `db_type` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '所属数据库',
  `func_type` int(10) DEFAULT NULL COMMENT '函数类型：0-聚合函数；1-快速计算函数；2-数学和三角函数；3-日期函数；4-文本函数；5-逻辑函数；6-其它函数',
  `desc` longtext COLLATE utf8mb4_bin COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of dataset_table_function
-- ----------------------------
BEGIN;
INSERT INTO `dataset_table_function` VALUES (1, 'ABS', 'ABS(x)', 'mysql', 2, '返回x的绝对值');
INSERT INTO `dataset_table_function` VALUES (2, 'PI', 'PI()', 'mysql', 2, '返回圆周率π，默认显示6位小数');
INSERT INTO `dataset_table_function` VALUES (3, 'SQRT', 'SQRT(x)', 'mysql', 2, '返回非负数的x的二次方根');
INSERT INTO `dataset_table_function` VALUES (4, 'MOD', 'MOD(x,y)', 'mysql', 2, '返回x被y除后的余数');
INSERT INTO `dataset_table_function` VALUES (5, 'CEIL', 'CEIL(x)', 'mysql', 2, '返回不小于x的最小整数');
INSERT INTO `dataset_table_function` VALUES (6, 'FLOOR', 'FLOOR(x)', 'mysql', 2, '返回不大于x的最大整数');
INSERT INTO `dataset_table_function` VALUES (7, 'ROUND', 'ROUND(x)', 'mysql', 2, '返回离x最近的整数');
INSERT INTO `dataset_table_function` VALUES (8, 'ROUND', 'ROUND(x,y)', 'mysql', 2, '保留x小数点后y位的值，但截断时要进行四舍五入');
INSERT INTO `dataset_table_function` VALUES (9, 'SIGN', 'SIGN(x)', 'mysql', 2, '返回参数x的符号，-1表示负数，0表示0，1表示正数');
INSERT INTO `dataset_table_function` VALUES (10, 'POW', 'POW(x,y)', 'mysql', 2, '返回x的y次乘方的值');
INSERT INTO `dataset_table_function` VALUES (11, 'EXP', 'EXP(x)', 'mysql', 2, '返回e的x乘方后的值');
INSERT INTO `dataset_table_function` VALUES (12, 'LOG', 'LOG(x)', 'mysql', 2, '返回x的自然对数，x相对于基数e的对数');
INSERT INTO `dataset_table_function` VALUES (13, 'LOG10', 'LOG10(x)', 'mysql', 2, '返回x的基数为10的对数');
INSERT INTO `dataset_table_function` VALUES (14, 'RADIANS', 'RADIANS(x)', 'mysql', 2, '返回x由角度转化为弧度的值');
INSERT INTO `dataset_table_function` VALUES (15, 'DEGREES', 'DEGREES(x)', 'mysql', 2, '返回x由弧度转化为角度的值');
INSERT INTO `dataset_table_function` VALUES (16, 'SIN', 'SIN(x)', 'mysql', 2, '返回x的正弦，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (17, 'ASIN', 'ASIN(x)', 'mysql', 2, '返回x的反正弦值');
INSERT INTO `dataset_table_function` VALUES (18, 'COS', 'COS(x)', 'mysql', 2, '返回x的余弦，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (19, 'ACOS', 'ACOS(x)', 'mysql', 2, '返回x的反余弦值');
INSERT INTO `dataset_table_function` VALUES (20, 'TAN', 'TAN(x)', 'mysql', 2, '返回x的正切，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (21, 'ATAN', 'ATAN(x)', 'mysql', 2, '返回x的反正切值');
INSERT INTO `dataset_table_function` VALUES (22, 'COT', 'COT(x)', 'mysql', 2, '返回给定弧度值x的余切');
INSERT INTO `dataset_table_function` VALUES (23, 'CHAR_LENGTH', 'CHAR_LENGTH(str)', 'mysql', 4, '计算字符串字符个数');
INSERT INTO `dataset_table_function` VALUES (24, 'TRIM', 'TRIM(s)', 'mysql', 4, '返回字符串s删除了两边空格之后的字符串');
INSERT INTO `dataset_table_function` VALUES (25, 'LTRIM', 'LTRIM(s)', 'mysql', 4, '返回字符串s，其左边所有空格被删除');
INSERT INTO `dataset_table_function` VALUES (26, 'RTRIM', 'RTRIM(s)', 'mysql', 4, '返回字符串s，其右边所有空格被删除');
INSERT INTO `dataset_table_function` VALUES (27, 'REPLACE', 'REPLACE(s,s1,s2)', 'mysql', 4, '返回一个字符串，用字符串s2替代字符串s中所有的字符串s1');
INSERT INTO `dataset_table_function` VALUES (28, 'SUBSTRING', 'SUBSTRING(s,n,len)', 'mysql', 4, '获取从字符串s中的第n个位置开始长度为len的字符串');
INSERT INTO `dataset_table_function` VALUES (29, 'CONCAT', 'CONCAT(s1,s2，...)', 'mysql', 4, '返回连接参数产生的字符串，一个或多个待拼接的内容，任意一个为NULL则返回值为NULL');
INSERT INTO `dataset_table_function` VALUES (30, 'INSERT', 'INSERT(s1,x,len,s2)', 'mysql', 4, '返回字符串s1，其子字符串起始于位置x，被字符串s2取代len个字符');
INSERT INTO `dataset_table_function` VALUES (31, 'LOWER', 'LOWER(str)', 'mysql', 4, '将str中的字母全部转换成小写');
INSERT INTO `dataset_table_function` VALUES (32, 'UPPER', 'UPPER(str)', 'mysql', 4, '将字符串中的字母全部转换成大写');
INSERT INTO `dataset_table_function` VALUES (33, 'LEFT', 'LEFT(s,n)', 'mysql', 4, '返回字符串s从最左边开始的n个字符');
INSERT INTO `dataset_table_function` VALUES (34, 'RIGHT', 'RIGHT(s,n)', 'mysql', 4, '返回字符串s从最右边开始的n个字符');
INSERT INTO `dataset_table_function` VALUES (35, 'REPEAT', 'REPEAT(s,n)', 'mysql', 4, '返回一个由重复字符串s组成的字符串，字符串s的数目等于n');
INSERT INTO `dataset_table_function` VALUES (36, 'SPACE', 'SPACE(n)', 'mysql', 4, '返回一个由n个空格组成的字符串');
INSERT INTO `dataset_table_function` VALUES (37, 'REVERSE', 'REVERSE(s)', 'mysql', 4, '将字符串s反转');
INSERT INTO `dataset_table_function` VALUES (38, 'CURDATE', 'CURDATE()', 'mysql', 3, '将当前日期按照\"YYYY-MM-DD\"或者\"YYYYMMDD\"格式的值返回，具体格式根据函数用在字符串或是数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (39, 'CURRENT_DATE', 'CURRENT_DATE()', 'mysql', 3, '将当前日期按照\"YYYY-MM-DD\"或者\"YYYYMMDD\"格式的值返回，具体格式根据函数用在字符串或是数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (40, 'NOW', 'NOW()', 'mysql', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (41, 'SYSDATE', 'SYSDATE()', 'mysql', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (42, 'LOCALTIME', 'LOCALTIME()', 'mysql', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (43, 'CURRENT_TIMESTAMP', 'CURRENT_TIMESTAMP()', 'mysql', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (44, 'UNIX_TIMESTAMP', 'UNIX_TIMESTAMP()', 'mysql', 3, '返回一个格林尼治标准时间1970-01-01 00:00:00到现在的秒数');
INSERT INTO `dataset_table_function` VALUES (45, 'UNIX_TIMESTAMP', 'UNIX_TIMESTAMP(date)', 'mysql', 3, '返回一个格林尼治标准时间1970-01-01 00:00:00到指定时间的秒数');
INSERT INTO `dataset_table_function` VALUES (46, 'FROM_UNIXTIME', 'FROM_UNIXTIME(date)', 'mysql', 3, '把UNIX时间戳转换为普通格式的时间');
INSERT INTO `dataset_table_function` VALUES (47, 'CASE', 'CASE expr WHEN v1 THEN r1 [WHEN v2 THEN v2] [ELSE rn] END', 'mysql', 5, '如果expr等于某个vn，则返回对应位置THEN后面的结果，如果与所有值都不想等，则返回ELSE后面的rn');
INSERT INTO `dataset_table_function` VALUES (48, 'IF', 'IF(expr,v1,v2)', 'mysql', 5, '如果expr是TRUE则返回v1，否则返回v2');
INSERT INTO `dataset_table_function` VALUES (49, 'IFNULL', 'IFNULL(v1,v2)', 'mysql', 5, '如果v1不为NULL，则返回v1，否则返回v2');
INSERT INTO `dataset_table_function` VALUES (50, 'ABS', 'ABS(x)', 'doris', 2, '返回x的绝对值');
INSERT INTO `dataset_table_function` VALUES (51, 'PI', 'PI()', 'doris', 2, '返回圆周率π，默认显示6位小数');
INSERT INTO `dataset_table_function` VALUES (52, 'SQRT', 'SQRT(x)', 'doris', 2, '返回非负数的x的二次方根');
INSERT INTO `dataset_table_function` VALUES (53, 'MOD', 'MOD(x,y)', 'doris', 2, '返回x被y除后的余数');
INSERT INTO `dataset_table_function` VALUES (54, 'CEIL', 'CEIL(x)', 'doris', 2, '返回不小于x的最小整数');
INSERT INTO `dataset_table_function` VALUES (55, 'FLOOR', 'FLOOR(x)', 'doris', 2, '返回不大于x的最大整数');
INSERT INTO `dataset_table_function` VALUES (56, 'ROUND', 'ROUND(x)', 'doris', 2, '返回离x最近的整数');
INSERT INTO `dataset_table_function` VALUES (57, 'ROUND', 'ROUND(x,y)', 'doris', 2, '保留x小数点后y位的值，但截断时要进行四舍五入');
INSERT INTO `dataset_table_function` VALUES (58, 'SIGN', 'SIGN(x)', 'doris', 2, '返回参数x的符号，-1表示负数，0表示0，1表示正数');
INSERT INTO `dataset_table_function` VALUES (59, 'POW', 'POW(x,y)', 'doris', 2, '返回x的y次乘方的值');
INSERT INTO `dataset_table_function` VALUES (60, 'EXP', 'EXP(x)', 'doris', 2, '返回e的x乘方后的值');
INSERT INTO `dataset_table_function` VALUES (61, 'LOG', 'LOG(x)', 'doris', 2, '返回x的自然对数，x相对于基数e的对数');
INSERT INTO `dataset_table_function` VALUES (62, 'LOG10', 'LOG10(x)', 'doris', 2, '返回x的基数为10的对数');
INSERT INTO `dataset_table_function` VALUES (63, 'RADIANS', 'RADIANS(x)', 'doris', 2, '返回x由角度转化为弧度的值');
INSERT INTO `dataset_table_function` VALUES (64, 'DEGREES', 'DEGREES(x)', 'doris', 2, '返回x由弧度转化为角度的值');
INSERT INTO `dataset_table_function` VALUES (65, 'SIN', 'SIN(x)', 'doris', 2, '返回x的正弦，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (66, 'ASIN', 'ASIN(x)', 'doris', 2, '返回x的反正弦值');
INSERT INTO `dataset_table_function` VALUES (67, 'COS', 'COS(x)', 'doris', 2, '返回x的余弦，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (68, 'ACOS', 'ACOS(x)', 'doris', 2, '返回x的反余弦值');
INSERT INTO `dataset_table_function` VALUES (69, 'TAN', 'TAN(x)', 'doris', 2, '返回x的正切，其中x为给定的弧度值');
INSERT INTO `dataset_table_function` VALUES (70, 'ATAN', 'ATAN(x)', 'doris', 2, '返回x的反正切值');
INSERT INTO `dataset_table_function` VALUES (71, 'COT', 'COT(x)', 'doris', 2, '返回给定弧度值x的余切');
INSERT INTO `dataset_table_function` VALUES (72, 'CHAR_LENGTH', 'CHAR_LENGTH(str)', 'doris', 4, '计算字符串字符个数');
INSERT INTO `dataset_table_function` VALUES (73, 'TRIM', 'TRIM(s)', 'doris', 4, '返回字符串s删除了两边空格之后的字符串');
INSERT INTO `dataset_table_function` VALUES (74, 'LTRIM', 'LTRIM(s)', 'doris', 4, '返回字符串s，其左边所有空格被删除');
INSERT INTO `dataset_table_function` VALUES (75, 'RTRIM', 'RTRIM(s)', 'doris', 4, '返回字符串s，其右边所有空格被删除');
INSERT INTO `dataset_table_function` VALUES (76, 'REPLACE', 'REPLACE(s,s1,s2)', 'doris', 4, '返回一个字符串，用字符串s2替代字符串s中所有的字符串s1');
INSERT INTO `dataset_table_function` VALUES (77, 'SUBSTRING', 'SUBSTRING(s,n,len)', 'doris', 4, '获取从字符串s中的第n个位置开始长度为len的字符串');
INSERT INTO `dataset_table_function` VALUES (78, 'CONCAT', 'CONCAT(s1,s2，...)', 'doris', 4, '返回连接参数产生的字符串，一个或多个待拼接的内容，任意一个为NULL则返回值为NULL');
INSERT INTO `dataset_table_function` VALUES (79, 'INSERT', 'INSERT(s1,x,len,s2)', 'doris', 4, '返回字符串s1，其子字符串起始于位置x，被字符串s2取代len个字符');
INSERT INTO `dataset_table_function` VALUES (80, 'LOWER', 'LOWER(str)', 'doris', 4, '将str中的字母全部转换成小写');
INSERT INTO `dataset_table_function` VALUES (81, 'UPPER', 'UPPER(str)', 'doris', 4, '将字符串中的字母全部转换成大写');
INSERT INTO `dataset_table_function` VALUES (82, 'LEFT', 'LEFT(s,n)', 'doris', 4, '返回字符串s从最左边开始的n个字符');
INSERT INTO `dataset_table_function` VALUES (83, 'RIGHT', 'RIGHT(s,n)', 'doris', 4, '返回字符串s从最右边开始的n个字符');
INSERT INTO `dataset_table_function` VALUES (84, 'REPEAT', 'REPEAT(s,n)', 'doris', 4, '返回一个由重复字符串s组成的字符串，字符串s的数目等于n');
INSERT INTO `dataset_table_function` VALUES (85, 'SPACE', 'SPACE(n)', 'doris', 4, '返回一个由n个空格组成的字符串');
INSERT INTO `dataset_table_function` VALUES (86, 'REVERSE', 'REVERSE(s)', 'doris', 4, '将字符串s反转');
INSERT INTO `dataset_table_function` VALUES (87, 'CURDATE', 'CURDATE()', 'doris', 3, '将当前日期按照\"YYYY-MM-DD\"或者\"YYYYMMDD\"格式的值返回，具体格式根据函数用在字符串或是数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (88, 'CURRENT_DATE', 'CURRENT_DATE()', 'doris', 3, '将当前日期按照\"YYYY-MM-DD\"或者\"YYYYMMDD\"格式的值返回，具体格式根据函数用在字符串或是数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (89, 'NOW', 'NOW()', 'doris', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (90, 'SYSDATE', 'SYSDATE()', 'doris', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (91, 'LOCALTIME', 'LOCALTIME()', 'doris', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (92, 'CURRENT_TIMESTAMP', 'CURRENT_TIMESTAMP()', 'doris', 3, '返回当前日期和时间值，格式为\"YYYY_MM-DD HH:MM:SS\"或\"YYYYMMDDHHMMSS\"，具体格式根据函数用在字符串或数字语境中而定');
INSERT INTO `dataset_table_function` VALUES (93, 'UNIX_TIMESTAMP', 'UNIX_TIMESTAMP()', 'doris', 3, '返回一个格林尼治标准时间1970-01-01 00:00:00到现在的秒数');
INSERT INTO `dataset_table_function` VALUES (94, 'UNIX_TIMESTAMP', 'UNIX_TIMESTAMP(date)', 'doris', 3, '返回一个格林尼治标准时间1970-01-01 00:00:00到指定时间的秒数');
INSERT INTO `dataset_table_function` VALUES (95, 'FROM_UNIXTIME', 'FROM_UNIXTIME(date)', 'doris', 3, '把UNIX时间戳转换为普通格式的时间');
INSERT INTO `dataset_table_function` VALUES (96, 'CASE', 'CASE expr WHEN v1 THEN r1 [WHEN v2 THEN v2] [ELSE rn] END', 'doris', 3, '如果expr等于某个vn，则返回对应位置THEN后面的结果，如果与所有值都不想等，则返回ELSE后面的rn');
INSERT INTO `dataset_table_function` VALUES (97, 'IF', 'IF(expr,v1,v2)', 'doris', 5, '如果expr是TRUE则返回v1，否则返回v2');
INSERT INTO `dataset_table_function` VALUES (98, 'IFNULL', 'IFNULL(v1,v2)', 'doris', 5, '如果v1不为NULL，则返回v1，否则返回v2');
INSERT INTO `dataset_table_function` VALUES (99, 'SUBSTR', 'SUBSTR(char, position, substring_length)', 'oracle', 4, '获取从字符串char中的第position个位置开始长度为substring_lenght的字符串');
INSERT INTO `dataset_table_function` VALUES (100, 'REPLACE', 'REPLACE(X,old,new)', 'oracle', 4, '在X中查找old，并替换成new');
INSERT INTO `dataset_table_function` VALUES (101, 'TRIM', 'TRIM([TRIM_STR  FROM]X)', 'oracle', 4, '把X的两边截去trim_str字符串，缺省截去空格');
INSERT INTO `dataset_table_function` VALUES (102, 'LOWER', 'LOWER(X)', 'oracle', 4, 'X转换成小写');
INSERT INTO `dataset_table_function` VALUES (103, 'UPPER', 'UPPER(X)', 'oracle', 4, 'X转换成大写');
INSERT INTO `dataset_table_function` VALUES (104, 'LENGTH', 'LENGTH(X)', 'oracle', 4, '返回X的长度');
INSERT INTO `dataset_table_function` VALUES (105, 'ABS', 'ABS(value)', 'oracle', 2, '返回value的绝对值');
INSERT INTO `dataset_table_function` VALUES (106, 'CEIL', 'CEIL(value)', 'oracle', 2, '返回大于等于value的最小整数');
INSERT INTO `dataset_table_function` VALUES (107, 'FLOOR', 'FLOOR(value)', 'oracle', 2, '返回小于等于value的最大整数');
INSERT INTO `dataset_table_function` VALUES (108, 'ROUND', 'ROUND(value,n)', 'oracle', 2, '对value进行四舍五入，保存小数点右侧的n位。如果n省略的话，相当于n=0的情况');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `chart_view` ADD COLUMN `drill_fields` LONGTEXT COMMENT '钻取字段' AFTER `custom_filter`;
UPDATE `chart_view` SET `drill_fields` = '[]';

