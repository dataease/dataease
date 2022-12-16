ALTER TABLE `panel_app_template_log`
    ADD COLUMN `datasource_from` varchar(255) NULL DEFAULT 'new' COMMENT '数据源来源' AFTER `datasource_id`;

UPDATE `panel_subject`
SET `details` = '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":true,\"openCommonStyle\":true,\"panel\":{\"themeColor\":\"light\",\"color\":\"#F1F3F5\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\",\"resultMode\":\"all\",\"resultCount\":1000},\"aidedDesign\":{\"showGrid\":false,\"matrixBase\":4},\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"themeId\":\"251a25d0-7ac5-11ed-9e50-5f9360ac1250\",\"chartInfo\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#000000\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true},\"chartColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#6D9A49\",\"tableItemBgColor\":\"#FFFFFF\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#4E81BB\",\"tableBorderColor\":\"#E6E7E4\",\"seriesColors\":[],\"tableHeaderFontColor\":\"#000000\"},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"color\":\"#FFFFFF\",\"alpha\":100,\"borderRadius\":5,\"innerPadding\":0},\"filterStyle\":{\"horizontal\":\"left\",\"vertical\":\"top\",\"color\":\"#000000\",\"brColor\":\"#DCDFE6\",\"wordColor\":\"#606266\",\"innerBgColor\":\"#FFFFFF\"}}}'
WHERE `id` = 'system_1';


ALTER TABLE `sys_task`
    ADD COLUMN `status` tinyint(1) NULL DEFAULT 1 COMMENT '运行状态' AFTER `create_time`;
