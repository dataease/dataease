package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SqlServerConfigration extends JdbcDTO {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public String getJdbc(){
        return "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE".replace("HOSTNAME", getHost()).replace("PORT", getPort().toString()).replace("DATABASE", getDataBase());
    }
}
