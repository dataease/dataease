create table if not exists test_case_comment
(
	id varchar(64) not null
		primary key,
	case_id varchar(64) null,
	description text null,
	author varchar(50) null,
	create_time bigint(13) null,
	update_time bigint(13) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists test_case_review
(
	id varchar(50) not null
		primary key,
	name varchar(200) null,
	creator varchar(50) null,
	status varchar(50) null,
	create_time bigint(13) null,
	update_time bigint(13) null,
	end_time bigint(13) null,
	description text null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists test_case_review_project
(
	review_id varchar(50) null,
	project_id varchar(50) null,
	constraint test_case_review_project_pk
		unique (review_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists test_case_review_test_case
(
	id varchar(64) not null
		primary key,
	review_id varchar(64) null,
	case_id varchar(64) null,
	status varchar(64) null,
	result varchar(50) null,
	reviewer varchar(64) null,
	create_time bigint(13) null,
	update_time bigint(13) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists test_case_review_users
(
	review_id varchar(50) null,
	user_id varchar(50) null,
	constraint test_case_review_users_pk
		unique (review_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


