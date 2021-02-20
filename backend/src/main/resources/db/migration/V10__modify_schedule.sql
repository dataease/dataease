ALTER TABLE `schedule` ADD `workspace_id` varchar(50) NOT NULL COMMENT 'Workspace ID this schedule belongs to';
ALTER TABLE `schedule` ADD `create_time`  bigint(13)  NULL COMMENT 'Create timestamp';
ALTER TABLE `schedule` ADD  `update_time`  bigint(13)  NULL COMMENT 'Update timestamp';

UPDATE schedule INNER JOIN (
SELECT project.workspace_id AS workspace_id, api_test.id AS resource_id,
api_test.create_time AS create_time, api_test.update_time AS update_time
FROM schedule
LEFT JOIN api_test
ON schedule.resource_id = api_test.id
LEFT JOIN project
ON api_test.project_id = project.id
) result
SET schedule.workspace_id = result.workspace_id,
schedule.create_time = result.create_time,
schedule.update_time = result.update_time
WHERE schedule.resource_id = result.resource_id;