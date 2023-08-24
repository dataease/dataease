UPDATE `my_plugin`
SET `version` = '1.18.10'
where `plugin_id` > 0
  and `version` = '1.18.9';

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('桑基图插件', 'default', 0, 0, 'view', 'AntV G2Plot 桑基图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-sankey-backend', NULL, NULL);

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('AntV 组合图插件', 'default', 0, 0, 'view', 'AntV G2Plot 组合图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-chartmix-backend', NULL, NULL);

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('ECharts 动态排序图插件', 'default', 0, 0, 'view', 'ECharts 动态排序图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-racebar-backend', NULL, NULL);
