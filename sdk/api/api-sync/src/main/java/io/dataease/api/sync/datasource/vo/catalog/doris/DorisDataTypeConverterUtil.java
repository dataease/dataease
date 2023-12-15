package io.dataease.api.sync.datasource.vo.catalog.doris;



import io.dataease.api.sync.datasource.vo.catalog.data.type.DorisDataType;
import io.dataease.api.sync.datasource.vo.model.SyncDatasourceConfiguration;
import io.dataease.exception.DEException;

import java.util.*;

import static io.dataease.result.ResultCode.SYSTEM_INNER_ERROR;

/**
 * @author fit2cloud
 * @date 2023/9/1 12:54
 **/
public class DorisDataTypeConverterUtil {
    /**
     * mysql to doris type map
     */
    private static final Map<String, DorisDataType> MYSQL_TO_DORIS_TYPE_MAP = new HashMap<>();
    /**
     * db2 to doris type map
     */
    private static final Map<String, DorisDataType> DB2_TO_DORIS_TYPE_MAP = new HashMap<>();
    /**
     * oracle to doris type map
     */
    private static final Map<String, DorisDataType> ORACLE_TO_DORIS_TYPE_MAP = new HashMap<>();
    /**
     * sqlserver to doris type map
     */
    private static final Map<String, DorisDataType> SQLSERVER_TO_DORIS_TYPE_MAP = new HashMap<>();

    static {
        // 初始化数据类型转换
        MYSQL_TO_DORIS_TYPE_MAP.put("CHAR", DorisDataType.CHAR);
        MYSQL_TO_DORIS_TYPE_MAP.put("VARCHAR", DorisDataType.VARCHAR);
        MYSQL_TO_DORIS_TYPE_MAP.put("DATE", DorisDataType.DATE);
        MYSQL_TO_DORIS_TYPE_MAP.put("TIME", DorisDataType.DATETIME);
        MYSQL_TO_DORIS_TYPE_MAP.put("DATETIME", DorisDataType.DATETIME);
        MYSQL_TO_DORIS_TYPE_MAP.put("TIMESTAMP", DorisDataType.TIMESTAMP);
        MYSQL_TO_DORIS_TYPE_MAP.put("DECIMAL", DorisDataType.DECIMAL);
        MYSQL_TO_DORIS_TYPE_MAP.put("NUMERIC", DorisDataType.DECIMAL);
        MYSQL_TO_DORIS_TYPE_MAP.put("BIT", DorisDataType.BOOLEAN);
        MYSQL_TO_DORIS_TYPE_MAP.put("BOOLEAN", DorisDataType.BOOLEAN);
        MYSQL_TO_DORIS_TYPE_MAP.put("TINYINT", DorisDataType.INT);
        MYSQL_TO_DORIS_TYPE_MAP.put("SMALLINT", DorisDataType.INT);
        MYSQL_TO_DORIS_TYPE_MAP.put("INTEGER", DorisDataType.INT);
        MYSQL_TO_DORIS_TYPE_MAP.put("FLOAT", DorisDataType.DOUBLE);
        MYSQL_TO_DORIS_TYPE_MAP.put("REAL", DorisDataType.DOUBLE);
        MYSQL_TO_DORIS_TYPE_MAP.put("DOUBLE", DorisDataType.DOUBLE);
        MYSQL_TO_DORIS_TYPE_MAP.put("BLOB", DorisDataType.CHAR);
        MYSQL_TO_DORIS_TYPE_MAP.put("CLOB", DorisDataType.CHAR);
        MYSQL_TO_DORIS_TYPE_MAP.put("BIGINT", DorisDataType.BIGINT);
        MYSQL_TO_DORIS_TYPE_MAP.put("INT", DorisDataType.INT);
        MYSQL_TO_DORIS_TYPE_MAP.put("JSON", DorisDataType.CHAR);
        MYSQL_TO_DORIS_TYPE_MAP.put("LONGTEXT", DorisDataType.STRING);
        MYSQL_TO_DORIS_TYPE_MAP.put("TEXT", DorisDataType.STRING);

        DB2_TO_DORIS_TYPE_MAP.put("CHAR", DorisDataType.CHAR);
        DB2_TO_DORIS_TYPE_MAP.put("VARCHAR", DorisDataType.VARCHAR);
        DB2_TO_DORIS_TYPE_MAP.put("DATE", DorisDataType.DATE);
        DB2_TO_DORIS_TYPE_MAP.put("TIME", DorisDataType.DATETIME);
        DB2_TO_DORIS_TYPE_MAP.put("TIMESTAMP", DorisDataType.DATETIME);
        DB2_TO_DORIS_TYPE_MAP.put("DECIMAL",  DorisDataType.DECIMAL);
        DB2_TO_DORIS_TYPE_MAP.put("NUMERIC",  DorisDataType.DECIMAL);
        DB2_TO_DORIS_TYPE_MAP.put("BIT", DorisDataType.BOOLEAN);
        DB2_TO_DORIS_TYPE_MAP.put("BOOLEAN", DorisDataType.BOOLEAN);
        DB2_TO_DORIS_TYPE_MAP.put("TINYINT", DorisDataType.INT);
        DB2_TO_DORIS_TYPE_MAP.put("SMALLINT", DorisDataType.INT);
        DB2_TO_DORIS_TYPE_MAP.put("INTEGER", DorisDataType.INT);
        DB2_TO_DORIS_TYPE_MAP.put("FLOAT", DorisDataType.DOUBLE);
        DB2_TO_DORIS_TYPE_MAP.put("REAL", DorisDataType.DOUBLE);
        DB2_TO_DORIS_TYPE_MAP.put("DOUBLE", DorisDataType.DOUBLE);
        DB2_TO_DORIS_TYPE_MAP.put("BLOB", DorisDataType.CHAR);
        DB2_TO_DORIS_TYPE_MAP.put("CLOB", DorisDataType.CHAR);

        ORACLE_TO_DORIS_TYPE_MAP.put("CHAR", DorisDataType.CHAR);
        ORACLE_TO_DORIS_TYPE_MAP.put("VARCHAR2", DorisDataType.CHAR);
        ORACLE_TO_DORIS_TYPE_MAP.put("DATE", DorisDataType.DATE);
        ORACLE_TO_DORIS_TYPE_MAP.put("TIMESTAMP", DorisDataType.DATETIME);
        ORACLE_TO_DORIS_TYPE_MAP.put("DECIMAL",  DorisDataType.DECIMAL);
        ORACLE_TO_DORIS_TYPE_MAP.put("NUMBER",  DorisDataType.DECIMAL);
        ORACLE_TO_DORIS_TYPE_MAP.put("BIT", DorisDataType.BOOLEAN);
        ORACLE_TO_DORIS_TYPE_MAP.put("BOOLEAN", DorisDataType.BOOLEAN);
        ORACLE_TO_DORIS_TYPE_MAP.put("INTEGER", DorisDataType.INT);
        ORACLE_TO_DORIS_TYPE_MAP.put("FLOAT", DorisDataType.DOUBLE);

        SQLSERVER_TO_DORIS_TYPE_MAP.put("CHAR", DorisDataType.CHAR);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("NVARCHAR", DorisDataType.VARCHAR);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("DATE", DorisDataType.DATE);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("DATETIME", DorisDataType.DATETIME);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("DECIMAL",  DorisDataType.DECIMAL);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("FLOAT", DorisDataType.DOUBLE);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("BIT", DorisDataType.BOOLEAN);
        SQLSERVER_TO_DORIS_TYPE_MAP.put("INT", DorisDataType.INT);
    }

    /**
     * 将数据库类型转换为Doris类型
     * @param databaseType 数据库类型
     * @param dataType 数据类型
     * @return DorisDataType转换后的Doris类型
     * @throws DEException 如果不支持的数据库类型，则抛出BackendException异常
     */
    public static DorisDataType convertDatabaseTypeToDorisType(String databaseType,String dataType) {
        DorisDataType dorisDataType = null;
        switch (databaseType.toUpperCase()) {
            case "MYSQL" -> dorisDataType = MYSQL_TO_DORIS_TYPE_MAP.get(dataType.toUpperCase());
            case "DB2" -> dorisDataType = DB2_TO_DORIS_TYPE_MAP.get(dataType.toUpperCase());
            case "ORACLE" -> dorisDataType = ORACLE_TO_DORIS_TYPE_MAP.get(dataType.toUpperCase());
            case "SQLSERVER" -> dorisDataType = SQLSERVER_TO_DORIS_TYPE_MAP.get(dataType.toUpperCase());
            default -> DEException.throwException("不支持的数据库类型: " + databaseType);
        };
        if(Objects.isNull(dorisDataType)){
            DEException.throwException(String.format("Doris数据类型不支持[%s]的数据类型[%s]",databaseType,dataType.toUpperCase()));
        }
        return dorisDataType;
    }

    /**
     * 根据数据源类型获取支持的类型
     * @param databaseType 数据源类型
     * @return 支持的类型列表
     */
    public static List<String> getDataTypeByDsType(SyncDatasourceConfiguration.DatasourceType databaseType){
        switch (databaseType) {
            case doris -> {
                return Arrays.stream(DorisDataType.values()).toList().stream().map(DorisDataType::name).toList();
            }
            case mysql -> {
                return MYSQL_TO_DORIS_TYPE_MAP.keySet().stream().toList();
            }
            case db2 -> {
                return DB2_TO_DORIS_TYPE_MAP.keySet().stream().toList();
            }
            case oracle -> {
                return ORACLE_TO_DORIS_TYPE_MAP.keySet().stream().toList();
            }
            case sqlServer -> {
                return SQLSERVER_TO_DORIS_TYPE_MAP.keySet().stream().toList();
            }
            default -> throw new DEException(SYSTEM_INNER_ERROR.code(),"不支持的数据库类型: " + databaseType.getType());
        }
    }
}
