ALTER TABLE message_task
    ADD identification varchar(255) NOT NULL;
ALTER TABLE message_task
    ADD is_set tinyint(1) NOT NULL;