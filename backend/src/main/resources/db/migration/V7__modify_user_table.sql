alter table user add source varchar(50) null;

update user set source = 'LOCAL' where source is null;