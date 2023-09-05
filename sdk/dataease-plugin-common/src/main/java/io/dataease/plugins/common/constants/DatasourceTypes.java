package io.dataease.plugins.common.constants;

import java.util.Arrays;
import java.util.List;

public enum DatasourceTypes {
    //jdbc
    mysql("mysql", "MySQL", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null,null, true, DatabaseClassification.OLTP),
    TiDB("TiDB", "TiDB", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null,null, true, DatabaseClassification.OLTP),
    hive("hive", "Apache Hive", "`", "`", "", "", "", true, DatasourceCalculationMode.DIRECT, null, null,true, DatabaseClassification.DL),
    impala("impala", "Apache Impala", "`", "`", "'", "'", "AuthMech=0", true, DatasourceCalculationMode.DIRECT, null, null,true, DatabaseClassification.OLAP),
    mariadb("mariadb", "MariaDB", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null,true, DatabaseClassification.OLTP),
    StarRocks("StarRocks", "StarRocks", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null,null, true, DatabaseClassification.OLAP),
    ds_doris("ds_doris", "Doris", "`", "`", "'", "'", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null,true, DatabaseClassification.OLAP),
    pg("pg", "PostgreSQL", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null,true, DatabaseClassification.OLTP),
    kingbase("kingbase", "KingBase", "\"", "\"", "\"", "\"", "", false, DatasourceCalculationMode.DIRECT, null,null,true, DatabaseClassification.OLTP),
    sqlServer("sqlServer", "SQL Server", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null,true, DatabaseClassification.OLTP),
    oracle("oracle", "Oracle", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, Arrays.asList("Default", "GBK", "BIG5", "ISO-8859-1", "UTF-8", "UTF-16", "CP850", "EUC_JP", "EUC_KR"), Arrays.asList("Default", "GBK", "UTF-8"),true, DatabaseClassification.OLTP),
    mongo("mongo", "MongoDB", "`", "`", "\"", "\"", "rebuildschema=true&authSource=admin", true, DatasourceCalculationMode.DIRECT, null, null,true, DatabaseClassification.OLTP),
    ck("ck", "ClickHouse", "`", "`", "", "", "", true, DatasourceCalculationMode.DIRECT, null, null,true, DatabaseClassification.OLAP),
    db2("db2", "Db2", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null, null,true, DatabaseClassification.OLTP),
    redshift("redshift", "AWS Redshift", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT, null, null,true, DatabaseClassification.DL),
    es("es", "Elasticsearch", "\"", "\"", "\"", "\"", "", true, DatasourceCalculationMode.DIRECT, null, null,false, DatabaseClassification.OLAP),
    api("api", "API", "\"", "\"", "\"", "\"", "rebuildschema=true&authSource=admin", true, DatasourceCalculationMode.SYNC, null, null,false, DatabaseClassification.OTHER),
    excel("excel", "Excel", "", "", "", "", "", false, DatasourceCalculationMode.SYNC, null, null,false, DatabaseClassification.OLTP),
    //engine
    engine_doris("engine_doris", "engine_doris", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", false, null, null,null, true, DatabaseClassification.OLAP),
    engine_mysql("engine_mysql", "engine_mysql", "`", "`", "", "", "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true", false, null, null, null,true, DatabaseClassification.OLTP);


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

    DatasourceTypes(String type, String name, String keywordPrefix, String keywordSuffix, String aliasPrefix, String aliasSuffix, String extraParams, boolean isDatasource, DatasourceCalculationMode calculationMode, List<String> charset, List<String> targetCharset, boolean isJdbc, DatabaseClassification databaseClassification) {
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

