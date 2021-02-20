create table if not exists test_plan_project
(
	test_plan_id varchar(50) null,
	project_id varchar(50) null,
	constraint test_plan_project_pk
		unique (test_plan_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

insert into test_plan_project(test_plan_id, project_id) select id test_plan_id, project_id project_id from test_plan;