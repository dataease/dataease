package io.dataease.extensions.datasource.provider;

import io.dataease.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * 核心数据源与同步模块共用的内置 JDBC 驱动白名单。
 */
public final class JdbcDriverPolicy {

    private static final Map<String, String> DEFAULT_DRIVERS = Map.ofEntries(
            Map.entry("mysql", "org.mariadb.jdbc.Driver"),
            Map.entry("mongo", "org.mariadb.jdbc.Driver"),
            Map.entry("mariadb", "org.mariadb.jdbc.Driver"),
            Map.entry("starrocks", "org.mariadb.jdbc.Driver"),
            Map.entry("doris", "org.mariadb.jdbc.Driver"),
            Map.entry("tidb", "org.mariadb.jdbc.Driver"),
            Map.entry("impala", "com.cloudera.impala.jdbc.Driver"),
            Map.entry("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
            Map.entry("oracle", "oracle.jdbc.driver.OracleDriver"),
            Map.entry("db2", "com.ibm.db2.jcc.DB2Driver"),
            Map.entry("gaussdb", "com.huawei.gaussdb.jdbc.Driver"),
            Map.entry("pg", "org.postgresql.Driver"),
            Map.entry("redshift", "com.amazon.redshift.jdbc42.Driver"),
            Map.entry("h2", "org.h2.Driver"),
            Map.entry("ck", "com.clickhouse.jdbc.ClickHouseDriver"),
            Map.entry("sqlite", "org.sqlite.JDBC")
    );

    private JdbcDriverPolicy() {
    }

    public static String trustedDriverClass(String type) {
        String driverClass = DEFAULT_DRIVERS.get(StringUtils.defaultString(type).toLowerCase(Locale.ROOT));
        if (StringUtils.isBlank(driverClass)) {
            DEException.throwException("invalid driver");
        }
        return driverClass;
    }

    public static String resolveDriverClass(String type, String requestedDriver) {
        String driverClass = trustedDriverClass(type);
        if (StringUtils.isNotBlank(requestedDriver) && !StringUtils.equals(driverClass, requestedDriver)) {
            DEException.throwException("invalid driver");
        }
        return driverClass;
    }
}
