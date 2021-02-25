package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
public class SqlServerConfigration extends JdbcDTO {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public String getJdbc(){
        return "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE".replace("HOSTNAME", getHost()).replace("PORT", getPort().toString()).replace("DATABASE", getDataBase());
    }
}
