create table swagger_url_project
(
    id          varchar(30) not null,
    project_id  varchar(30) null,
    swagger_url varchar(255) null,
    module_id   varchar(30) null,
    module_path varchar(255) null,
    mode_id varchar(30) null,
    primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

