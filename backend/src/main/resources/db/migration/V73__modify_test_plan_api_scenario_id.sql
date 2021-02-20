ALTER TABLE api_scenario MODIFY COLUMN id VARCHAR (255);
ALTER TABLE test_plan_api_scenario MODIFY COLUMN api_scenario_id VARCHAR (255);
ALTER TABLE api_scenario_report MODIFY COLUMN scenario_id VARCHAR (255);
