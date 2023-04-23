package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration {
    private String type;
    private String name;
    private DatasourceCatalog catalog;
    private String catalogDesc;
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
        API("API", "API"),
        Excel("Excel", "Excel"),
        mysql("mysql", "Mysql"),
        oracle("oracle", "ORACLE"),
        sqlserver("sqlserver", "Sqlserver");

        private String type;
        private String name;

        DatasourceType(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    static public enum DatasourceCatalog {
        OLAP("OLAP", "OLAP"),
        OLTP("OLTP", "OLTP"),
        DATALAKE("DATALAKE", "DATALAKE"),
        LOCALFILE("LOCALFILE", "LOCALFILE"),
        API("API", "API");

        private String type;
        private String desc;

        DatasourceCatalog(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public String getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }
    }
}
