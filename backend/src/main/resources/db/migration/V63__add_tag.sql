ALTER TABLE api_definition
    ADD tags VARCHAR(1000) NULL;

ALTER TABLE api_test_case
    ADD tags VARCHAR(1000) NULL;

ALTER TABLE test_case
    ADD tags VARCHAR(1000) NULL;