CREATE TABLE IF NOT EXISTS `api_data_view`
(
    id varchar(50) NOT NULL primary key,
    report_id varchar(255) NOT NULL,
    api_name varchar(200) NULL,
    url varchar(255) NULL,
    response_code varchar(100) NULL,
    start_time varchar(20) NULL,
    response_time varchar(20) default '0' NULL,
    create_time timestamp default CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp default CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;