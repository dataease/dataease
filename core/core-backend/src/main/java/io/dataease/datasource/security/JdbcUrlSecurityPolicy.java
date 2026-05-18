package io.dataease.datasource.security;

import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class JdbcUrlSecurityPolicy {

    private static final String DEFAULT_CUSTOM_DRIVER = "default";

    private static final Map<String, String> JDBC_PREFIXES = Map.ofEntries(
            Map.entry("mysql", "jdbc:mysql"),
            Map.entry("mongo", "jdbc:mysql"),
            Map.entry("mariadb", "jdbc:mysql"),
            Map.entry("starrocks", "jdbc:mysql"),
            Map.entry("doris", "jdbc:mysql"),
            Map.entry("tidb", "jdbc:mysql"),
            Map.entry("impala", "jdbc:impala"),
            Map.entry("sqlserver", "jdbc:sqlserver"),
            Map.entry("oracle", "jdbc:oracle"),
            Map.entry("db2", "jdbc:db2"),
            Map.entry("pg", "jdbc:postgresql"),
            Map.entry("redshift", "jdbc:redshift"),
            Map.entry("h2", "jdbc:h2"),
            Map.entry("ck", "jdbc:clickhouse"),
            Map.entry("sqlite", "jdbc:sqlite:")
    );

    private static final Map<String, String> DEFAULT_DRIVERS = Map.ofEntries(
            Map.entry("mysql", "com.mysql.cj.jdbc.Driver"),
            Map.entry("mongo", "com.mysql.cj.jdbc.Driver"),
            Map.entry("mariadb", "com.mysql.cj.jdbc.Driver"),
            Map.entry("starrocks", "com.mysql.cj.jdbc.Driver"),
            Map.entry("doris", "com.mysql.cj.jdbc.Driver"),
            Map.entry("tidb", "com.mysql.cj.jdbc.Driver"),
            Map.entry("impala", "com.cloudera.impala.jdbc.Driver"),
            Map.entry("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
            Map.entry("oracle", "oracle.jdbc.driver.OracleDriver"),
            Map.entry("db2", "com.ibm.db2.jcc.DB2Driver"),
            Map.entry("pg", "org.postgresql.Driver"),
            Map.entry("redshift", "com.amazon.redshift.jdbc42.Driver"),
            Map.entry("h2", "org.h2.Driver"),
            Map.entry("ck", "com.clickhouse.jdbc.ClickHouseDriver"),
            Map.entry("sqlite", "org.sqlite.JDBC")
    );

    private static final Set<String> COMMON_DANGEROUS_FRAGMENTS = Set.of(
            "jndi:",
            "rmi:",
            "ldap:",
            "ldaps:",
            "dns:",
            "file:",
            "ftp:",
            "nis:",
            "corba:",
            "corbaname",
            "iiop",
            "iiopname",
            "java.naming.factory.initial",
            "java.naming.provider.url",
            "java.naming.factory.object",
            "java.naming.factory.state",
            "autodeserialize",
            "queryinterceptors",
            "statementinterceptors",
            "detectcustomcollations",
            "connectionproperties",
            "initsql"
    );

    private static final Map<String, Set<String>> TYPE_DANGEROUS_FRAGMENTS = Map.ofEntries(
            Map.entry("mysql", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("mongo", Set.of()),
            Map.entry("mariadb", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("starrocks", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("doris", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("tidb", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("impala", Set.of("krbjaasfile", "krb5.conf")),
            Map.entry("sqlserver", Set.of()),
            Map.entry("oracle", Set.of()),
            Map.entry("db2", Set.of()),
            Map.entry("pg", Set.of("socketfactory", "socketfactoryarg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationpluginclassname")),
            Map.entry("redshift", Set.of("socketfactory", "socketfactoryarg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationpluginclassname", "inifile")),
            Map.entry("h2", Set.of("init=", "runscript")),
            Map.entry("ck", Set.of())
    );

    private JdbcUrlSecurityPolicy() {
    }

    public static String validate(String type, String driver, String jdbcUrl, String extraParams) {
        if (StringUtils.isBlank(jdbcUrl)) {
            DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
        }
        String normalizedType = normalizeType(type);
        String normalizedUrl = canonicalize(jdbcUrl);
        String normalizedExtraParams = canonicalize(extraParams);
        String expectedPrefix = JDBC_PREFIXES.get(normalizedType);
        if (StringUtils.isBlank(expectedPrefix) || !normalizedUrl.startsWith(expectedPrefix)) {
            DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
        }
        Set<String> dangerousFragments = new LinkedHashSet<>(COMMON_DANGEROUS_FRAGMENTS);
        dangerousFragments.addAll(TYPE_DANGEROUS_FRAGMENTS.getOrDefault(normalizedType, Set.of()));
        for (String fragment : dangerousFragments) {
            if (normalizedUrl.contains(fragment) || normalizedExtraParams.contains(fragment)) {
                DEException.throwException("Illegal parameter: " + fragment);
            }
        }
        return jdbcUrl;
    }

    public static String trustedDriverClass(String type) {
        String driverClass = DEFAULT_DRIVERS.get(normalizeType(type));
        if (StringUtils.isBlank(driverClass)) {
            DEException.throwException("invalid driver");
        }
        return driverClass;
    }

    public static String resolveDriverClass(String type, String requestedDriverClass, String customDriver, CoreDriver registeredDriver) {
        if (!isDefaultCustomDriver(customDriver)) {
            if (registeredDriver == null
                    || StringUtils.isBlank(registeredDriver.getDriverClass())
                    || !StringUtils.equalsIgnoreCase(normalizeType(type), normalizeType(registeredDriver.getType()))) {
                DEException.throwException("invalid driver");
            }
            return registeredDriver.getDriverClass();
        }
        String trustedDriverClass = trustedDriverClass(type);
        if (StringUtils.isNotBlank(requestedDriverClass) && !StringUtils.equalsIgnoreCase(requestedDriverClass, trustedDriverClass)) {
            DEException.throwException("invalid driver");
        }
        return trustedDriverClass;
    }

    public static boolean isDefaultCustomDriver(String customDriver) {
        return StringUtils.isBlank(customDriver) || StringUtils.equalsIgnoreCase(customDriver, DEFAULT_CUSTOM_DRIVER);
    }

    private static String canonicalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String normalized = value;
        for (int i = 0; i < 3; i++) {
            try {
                String decoded = URLDecoder.decode(normalized, StandardCharsets.UTF_8);
                if (StringUtils.equals(decoded, normalized)) {
                    normalized = decoded;
                    break;
                }
                normalized = decoded;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);
        normalized = normalized.replace("\\", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeType(String type) {
        return StringUtils.defaultString(type).toLowerCase(Locale.ROOT);
    }
}
