package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MysqlConfigrationDTO extends JdbcDTO {

    private String driver = "com.mysql.cj.jdbc.Driver";

    public String getJdbc(){
        return "jdbc:mysql://HOSTNAME:PORT/DATABASE".replace("HOSTNAME", getHost()).replace("PORT", getPort().toString()).replace("DATABASE", getDataBase());
    }
}
