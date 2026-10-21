package io.dataease.extensions.datasource.provider;

import io.dataease.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * 内置 JDBC 驱动类白名单。
 * 数据源配置中的 driver 字段来自请求侧，不能直接交给 ClassLoader 加载；
 * 必须按数据源类型解析为服务端内置的驱动类。
 */
public final class JdbcDriverPolicy {

    private static final String DEFAULT_CUSTOM_DRIVER = "default";

    private static final Map<String, String> TRUSTED_DRIVERS = Map.ofEntries(
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
            Map.entry("kingbase", "com.kingbase8.Driver"),
            Map.entry("redshift", "com.amazon.redshift.jdbc42.Driver"),
            Map.entry("h2", "org.h2.Driver"),
            Map.entry("ck", "com.clickhouse.jdbc.ClickHouseDriver"),
            Map.entry("sqlite", "org.sqlite.JDBC")
    );

    private JdbcDriverPolicy() {
    }

    public static String trustedDriverClass(String type) {
        String driverClass = TRUSTED_DRIVERS.get(normalizeType(type));
        if (StringUtils.isBlank(driverClass)) {
            DEException.throwException("invalid driver");
        }
        return driverClass;
    }

    /**
     * 解析实际使用的驱动类。请求侧声明的 driver 只有在与服务端内置驱动一致时才被接受，
     * 否则直接拒绝，避免加载任意类。
     */
    public static String resolveDriverClass(String type, String requestedDriver) {
        String trustedDriver = trustedDriverClass(type);
        if (StringUtils.isBlank(requestedDriver) || StringUtils.equalsIgnoreCase(requestedDriver, DEFAULT_CUSTOM_DRIVER)) {
            return trustedDriver;
        }
        if (StringUtils.equalsIgnoreCase(trustedDriver, requestedDriver)) {
            return trustedDriver;
        }
        DEException.throwException("Illegal driver class: " + requestedDriver);
        return trustedDriver;
    }

    private static String normalizeType(String type) {
        return StringUtils.defaultString(type).toLowerCase(Locale.ROOT);
    }
}
