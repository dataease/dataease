UPDATE `my_plugin`
SET `version` = '1.18.2'
where `plugin_id` > 0
  and `version` = '1.18.1';

UPDATE sys_menu
SET i_frame = 1
WHERE menu_id = 800;

UPDATE `panel_subject`
SET `details` = '{\"width\":1600,\"height\":900,\"scale\":100,\"scaleWidth\":100,\"scaleHeight\":100,\"selfAdaption\":true,\"auxiliaryMatrix\":true,\"openCommonStyle\":true,\"panel\":{\"themeColor\":\"light\",\"color\":\"#F1F3F5\",\"imageUrl\":{},\"backgroundType\":\"color\",\"gap\":\"yes\",\"resultMode\":\"all\",\"resultCount\":1000},\"aidedDesign\":{\"showGrid\":false,\"matrixBase\":4},\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"themeId\":\"e846db60-9619-11ed-b973-39e0420a3eeb\",\"chartInfo\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"color\":\"#000000\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true,\"remarkShow\":false,\"remark\":\"\",\"remarkBackgroundColor\":\"#ffffffff\",\"fontFamily\":\"Microsoft YaHei\",\"letterSpace\":\"0\",\"fontShadow\":false},\"chartColor\":{\"value\":\"default\",\"colors\":[\"#5470c6\",\"#91cc75\",\"#fac858\",\"#ee6666\",\"#73c0de\",\"#3ba272\",\"#fc8452\",\"#9a60b4\",\"#ea7ccc\"],\"alpha\":100,\"tableHeaderBgColor\":\"#6D9A49\",\"tableItemBgColor\":\"#FFFFFF\",\"tableHeaderFontColor\":\"#000000\",\"tableFontColor\":\"#000000\",\"tableStripe\":true,\"dimensionColor\":\"#000000\",\"quotaColor\":\"#5470c6\",\"tableBorderColor\":\"#E6E7E4\",\"seriesColors\":[],\"areaBorderColor\":\"#303133\",\"gradient\":false,\"areaBaseColor\":\"#FFFFFF\",\"tableScrollBarColor\":\"rgba(0, 0, 0, 0.15)\",\"tableScrollBarHoverColor\":\"rgba(0, 0, 0, 0.4)\"},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"color\":\"#FFFFFF\",\"alpha\":100,\"borderRadius\":5,\"innerPadding\":0,\"enable\":false,\"innerImageColor\":\"#1E90FF\",\"backgroundType\":\"outerImage\",\"outerImage\":null},\"filterStyle\":{\"horizontal\":\"left\",\"vertical\":\"top\",\"color\":\"#000000\",\"brColor\":\"\",\"wordColor\":\"\",\"innerBgColor\":\"\"},\"tabStyle\":{\"headFontColor\":\"#OOOOOO\",\"headFontActiveColor\":\"#OOOOOO\",\"headBorderColor\":\"#OOOOOO\",\"headBorderActiveColor\":\"#OOOOOO\",\"headPosition\":\"left\"}}}'
WHERE `id` = 'system_1';
