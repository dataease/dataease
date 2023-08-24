package io.dataease.plugins.datasource.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JdbcConfiguration {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String dataBase;
    private String schema;
    private String dataSourceType = "jdbc";
    private String customDriver = "default";
    private String authMethod = "passwd";

    private String charset;
    private String targetCharset;
    private int initialPoolSize = 5;
    private int minPoolSize = 5;
    private int maxPoolSize = 50;
    private int queryTimeout = 30;
}
