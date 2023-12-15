package io.dataease.api.sync.datasource.vo.model.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Configuration {
    private String type;
    private String name;
    private String catalog;
    private String catalogDesc;
    private String extraParams;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";
    private String jdbc;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String dataBase;
    private String schema;
    private String customDriver = "default";
    private String authMethod = "passwd";
    private String connectionType;
    private String charset;
    private String targetCharset;
    private String driver;
    private int initialPoolSize = 5;
    private int minPoolSize = 5;
    private int maxPoolSize = 50;
    private int queryTimeout = 30;

    public String getSeaTunnelSource(String configTemplateContent,String querySql) {
        return configTemplateContent
                .replace("${source.db.username}", getUsername())
                .replace("${source.db.password}", getPassword())
                .replace("${source.db.host}", getHost())
                .replace("${source.db.port}", String.valueOf(getPort()))
                .replace("${source.db.name}", getDataBase())
                .replace("${source.db.query}", querySql);
    }

}
