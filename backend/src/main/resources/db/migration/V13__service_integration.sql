create table service_integration
(
	id varchar(50) not null,
	organization_id varchar(50) not null,
	platform varchar(50) not null,
	configuration text not null,
	constraint service_integration_pk
		primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table if not exists test_case_issues
(
	id varchar(50) not null
		primary key,
	test_case_id varchar(50) not null,
	issues_id varchar(100) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table if not exists issues
(
	id varchar(50) not null
		primary key,
	title varchar(64) null,
	description text null,
	status varchar(50) null,
	create_time bigint(13) null,
	update_time bigint(13) null,
	reporter varchar(50) null comment 'case issues creator',
	lastmodify varchar(50) null,
	platform varchar(50) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;