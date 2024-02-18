package io.dataease.plugins.common.constants;

import java.util.Arrays;
import java.util.List;

public enum DatasourceTypes {
    //jdbc
    mysql("mysql", "MySQL", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "5,8"),
    TiDB("TiDB", "TiDB", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "5,6,7"),
    hive("hive", "Apache Hive", "`", "`", "", "", "", true, DatasourceCalculationMode.DIRECT, null, null, true, DatabaseClassification.DL, "2,3"),
    impala("impala", "Apache Impala", "`", "`", "'", "'", "AuthMech=0", true, DatasourceCalculationMode.DIRECT, null, null, true, DatabaseClassification.OLAP, "3,4"),
    mariadb("mariadb", "MariaDB", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "10,11"),
    StarRocks("StarRocks", "StarRocks", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLAP, ""),
    ds_doris("ds_doris", "Doris", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLAP, ""),
    pg("pg", "PostgreSQL", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "9,10,11,12,13,14,15,16"),
    sqlServer("sqlServer", "SQL Server", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "10,12,13,14,15,16"),
    oracle("oracle", "Oracle", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, Arrays.asList("Default", "GBK", "BIG5", "ISO-8859-1", "UTF-8", "UTF-16", "CP850", "EUC_JP", "EUC_KR", "US7ASCII", "AL32UTF8"), Arrays.asList("Default", "GBK", "UTF-8"), true, DatabaseClassification.OLTP, "8,9,10,11,12"),
    mongo("mongo", "MongoDB", "`", "`", "\"", "\"", "rebuildschema=true&authSource=admin", true, DatasourceCalculationMode.DIRECT, null, null, true, DatabaseClassification.OLTP, "3,4"),
    ck("ck", "ClickHouse", "`", "`", "", "", "", true, DatasourceCalculationMode.DIRECT, null, null, true, DatabaseClassification.OLAP, "18,19,20,21,22,23"),
    db2("db2", "Db2", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null, true, DatabaseClassification.OLTP, "9,10,11,12"),
    redshift("redshift", "AWS Redshift", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT, null, null, true, DatabaseClassification.DL, ""),
    es("es", "Elasticsearch", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT, null, null, false, DatabaseClassification.OLAP, ""),
    api("api", "API", "\"", "\"", "\"", "\"", "rebuildschema=true&authSource=admin", true, DatasourceCalculationMode.SYNC, null, null, false, DatabaseClassification.OTHER, ""),
    excel("excel", "Excel", "", "", "", "", "", false, DatasourceCalculationMode.SYNC, null, null, false, DatabaseClassification.OLTP, ""),
    //engine
    engine_doris("engine_doris", "engine_doris", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", false, null, null, null, true, DatabaseClassification.OLAP, ""),
    engine_mysql("engine_mysql", "engine_mysql", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", false, null, null, null, true, DatabaseClassification.OLTP, "");


    private String type;
    private String name;
    private String keywordPrefix;
    private String keywordSuffix;
    private String aliasPrefix;
    private String aliasSuffix;
    private String extraParams;
    private boolean isDatasource;
    private boolean isJdbc;
    private DatasourceCalculationMode calculationMode;
    private DatabaseClassification databaseClassification;
    private List<String> charset;
    private List<String> targetCharset;
    private String surpportVersions;

    DatasourceTypes(String type, String name, String keywordPrefix, String keywordSuffix, String aliasPrefix, String aliasSuffix, String extraParams, boolean isDatasource, DatasourceCalculationMode calculationMode, List<String> charset, List<String> targetCharset, boolean isJdbc, DatabaseClassification databaseClassification, String surpportVersions) {
        this.type = type;
        this.name = name;
        this.keywordPrefix = keywordPrefix;
        this.keywordSuffix = keywordSuffix;
        this.aliasPrefix = aliasPrefix;
        this.aliasSuffix = aliasSuffix;
        this.extraParams = extraParams;
        this.isDatasource = isDatasource;
        this.calculationMode = calculationMode;
        this.charset = charset;
        this.targetCharset = targetCharset;
        this.isJdbc = isJdbc;
        this.databaseClassification = databaseClassification;
        this.surpportVersions = surpportVersions;
    }

    public String getSurpportVersions() {
        return surpportVersions;
    }

    public void setSurpportVersions(String surpportVersions) {
        this.surpportVersions = surpportVersions;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
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

    public String getExtraParams() {
        return extraParams;
    }

    public List<String> getCharset() {
        return charset;
    }

    public List<String> getTargetCharset() {
        return targetCharset;
    }

    public DatasourceCalculationMode getCalculationMode() {
        return calculationMode;
    }

    public boolean isDatasource() {
        return isDatasource;
    }

    public boolean isJdbc() {
        return isJdbc;
    }

    public DatabaseClassification getDatabaseClassification() {
        return databaseClassification;
    }

}

