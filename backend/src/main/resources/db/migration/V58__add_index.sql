create index project_workspace_id_index on project (workspace_id);
create index test_case_review_creator_index on test_case_review (creator);
create index test_case_review_project_project_id_index on test_case_review_project (project_id);