create table if not exists test_case_file
(
    case_id varchar(64) null,
    file_id varchar(64) null,
    constraint test_case_file_unique_key
        unique (case_id, file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
