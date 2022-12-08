ALTER TABLE `panel_app_template_log`
    ADD COLUMN `datasource_from` varchar(255) NULL DEFAULT 'new' COMMENT '数据源来源' AFTER `datasource_id`;
