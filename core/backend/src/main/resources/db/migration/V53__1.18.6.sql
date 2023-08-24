UPDATE `my_plugin`
SET `version` = '1.18.6'
where `plugin_id` > 0
  and `version` = '1.18.5';
