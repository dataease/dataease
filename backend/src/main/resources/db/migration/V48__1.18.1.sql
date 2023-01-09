UPDATE `my_plugin`
SET `version` = '1.18.1'
where `plugin_id` > 0 and `store` = 'default' and `version` = '1.18.0';