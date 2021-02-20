ALTER TABLE schedule
    MODIFY COLUMN resource_id VARCHAR(255);
ALTER TABLE message_task
    MODIFY COLUMN test_id VARCHAR(255);
