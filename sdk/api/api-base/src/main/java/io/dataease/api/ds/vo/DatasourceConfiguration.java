package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration {
    private DatasourceType type;
    private String name;
    private String extraParams;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String dataBase;
    private String schema;
    private String customDriver = "default";
    private String authMethod = "passwd";
    private String charset;
    private String targetCharset;
    private String driver;
    private int initialPoolSize = 5;
    private int minPoolSize = 5;
    private int maxPoolSize = 50;
    private int queryTimeout = 30;
    private List<String> illegalParameters;
    private List<String> showTableSqls;

    public String getJdbc(){
        return "";
    }

    static public enum DatasourceType {
        mysql, oracle
    }
}
