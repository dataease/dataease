alter table issues drop primary key;
alter table issues
	add constraint issues_pk
		primary key (id, platform);