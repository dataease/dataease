package io.dataease.datasource.constants;

public enum DatasourceTypes {
    excel("excel", "excel", "", "", "", "", ""),
    mysql("mysql", "mysql", "com.mysql.jdbc.Driver", "`", "`", "'", "'"),
    pg("pg", "pg", "org.postgresql.Driver", "\"", "\"", "\"", "\""),
    sqlServer("sqlServer", "sqlServer", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "\"", "\"", "\"", "\""),
    doris("doris", "doris", "com.mysql.jdbc.Driver", "`", "`", "", ""),
    oracle("oracle", "oracle", "oracle.jdbc.driver.OracleDriver", "\"", "\"", "\"", "\""),
    ck("ch", "ch", "ru.yandex.clickhouse.ClickHouseDriver", "`", "`", "'", "'"),
    es("es", "es", "", "\"", "\"", "\"", "\"");

    private String feature;
    private String desc;
    private String driver;
    private String keywordPrefix;
    private String keywordSuffix;
    private String aliasPrefix;
    private String aliasSuffix;

    DatasourceTypes(String feature, String desc, String driver, String keywordPrefix, String keywordSuffix, String aliasPrefix, String aliasSuffix) {
        this.feature = feature;
        this.desc = desc;
        this.driver = driver;
        this.keywordPrefix = keywordPrefix;
        this.keywordSuffix = keywordSuffix;
        this.aliasPrefix = aliasPrefix;
        this.aliasSuffix = aliasSuffix;
    }

    public String getFeature() {
        return feature;
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver() {
        return driver;
    }

    public String getKeywordPrefix() {
        return keywordPrefix;
    }

    public String getKeywordSuffix() {
        return keywordSuffix;
    }

    public String getAliasPrefix() {
        return aliasPrefix;
    }

    public String getAliasSuffix() {
        return aliasSuffix;
    }
}
