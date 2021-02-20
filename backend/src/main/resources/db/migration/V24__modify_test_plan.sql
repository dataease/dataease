ALTER TABLE test_plan ADD planned_start_time bigint(13) NULL COMMENT 'Planned start time timestamp';
ALTER TABLE test_plan ADD planned_end_time bigint(13) NULL COMMENT 'Planned end time timestamp';
ALTER TABLE test_plan ADD actual_start_time bigint(13) NULL COMMENT 'Actual start time timestamp';
ALTER TABLE test_plan ADD actual_end_time bigint(13) NULL COMMENT 'Actual end time timestamp';
