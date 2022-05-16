ALTER TABLE `panel_group`
ADD COLUMN `status` varchar(255) NULL DEFAULT 'publish' COMMENT '1.publish--发布 2.unpublished--未发布' AFTER `mobile_layout`;
