ALTER TABLE `xpack_webhook`
    ADD COLUMN `msg_template` longtext NULL AFTER `ssl`;
