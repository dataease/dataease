CREATE TABLE IF NOT EXISTS license
(
    id bigint,
    update_time bigint default NULL,
    license longtext default NULL,
    f2c_license longtext default NULL,
    PRIMARY KEY(id)
);