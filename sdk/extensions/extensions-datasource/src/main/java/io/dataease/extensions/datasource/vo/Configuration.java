package io.dataease.extensions.datasource.vo;

import lombok.Data;

@Data
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
    private String jdbcUrl;
    private String urlType;
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
    private boolean useSSH = false;
    private String sshHost;
    private Integer sshPort;
    private Integer lPort;
    private String sshUserName;
    private String sshType = "password";
    private String sshPassword;
    private String sshKey;
    private String sshKeyPassword;


    public String getLHost(){
        if(useSSH){
            return "127.0.0.1";
        }else {
            return this.host;
        }
    }

    public Integer getLPort(){
        if(useSSH && lPort != null){
            return lPort;
        }else {
            return this.port;
        }
    }

}
