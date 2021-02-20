ALTER TABLE notice
    ADD user_id VARCHAR(50) NULL;
UPDATE notice
SET notice.user_id = (
    SELECT id
    FROM user
    WHERE notice.NAME = user.name
);
ALTER TABLE notice
    DROP COLUMN name;