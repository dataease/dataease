package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MysqlConfigrationDTO {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String jdbc;
    private String dataBase;
    private String driver = "com.mysql.cj.jdbc.Driver";

    public String getJdbc(){
       if(StringUtils.isNotEmpty(jdbc)){
           return jdbc;
       }else {
           return "jdbc:mysql://HOSTNAME:PORT/DATABASE".replace("HOSTNAME", host).replace("PORT", port.toString()).replace("DATABASE", dataBase);
       }
    }
}
