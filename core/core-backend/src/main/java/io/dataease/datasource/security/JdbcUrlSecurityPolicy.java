package io.dataease.datasource.security;

import io.dataease.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

public final class JdbcUrlSecurityPolicy {

    private static final String DEFAULT_CUSTOM_DRIVER = "default";

    private static final Map<String, String> JDBC_PREFIXES = Map.ofEntries(
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
    private static final List<String> mysqlType = Arrays.asList("mysql", "mongo", "mariadb", "starrocks", "doris", "tidb");

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
            "corbaloc",
            "corbaname",
            "iiop",
            "iiopname",
            "java.naming.factory.initial",
            "java.naming.provider.url",
            "java.naming.factory.object",
            "java.naming.factory.state",
            "autodeserialize",
            "queryinterceptors",
            "socketfactoryclass",
            "socketfactoryconstructorarg",
            "accesstokencallbackclass",
            "statementinterceptors",
            "detectcustomcollations",
            "connectionproperties",
            "initsql",
            "allowloadlocalinfile",
            "allowurlinlocalinfile",
            "allowloadlocalinfileinpath",
            "allowmultiqueries"
    );

    private static final Map<String, Set<String>> TYPE_DANGEROUS_FRAGMENTS = Map.ofEntries(
            Map.entry("mysql", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("mongo", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("mariadb", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("starrocks", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("doris", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("tidb", Set.of("maxallowedpacket", "allowloadlocalinfile", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("impala", Set.of("krbjaasfile", "krb5.conf")),
            Map.entry("sqlserver", Set.of("socketfactoryclass", "socketfactoryconstructorarg", "accesstokencallbackclass")),
            Map.entry("oracle", Set.of()),
            Map.entry("db2", Set.of()),
            Map.entry("pg", Set.of("socketfactory", "socketfactoryarg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationpluginclassname")),
            Map.entry("redshift", Set.of("socketfactory", "socketfactoryarg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationpluginclassname", "inifile")),
            Map.entry("h2", Set.of("init=", "runscript")),
            Map.entry("ck", Set.of())
    );

    private static final Set<String> H2_ALLOWED_SETTINGS = Set.of(
            "AUTO_SERVER",
            "AUTO_RECONNECT",
            "MODE",
            "CASE_INSENSITIVE_IDENTIFIERS",
            "DATABASE_TO_UPPER"
    );

    private static final Set<String> H2_ALLOWED_MODES = Set.of(
            "MySQL",
            "REGULAR",
            "PostgreSQL",
            "MSSQLServer",
            "MariaDB",
            "Oracle",
            "DB2",
            "Derby",
            "HSQLDB",
            "Ignite"
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
        if (!mysqlType.contains(normalizedType) && (StringUtils.isBlank(expectedPrefix) || !startsWithIgnoreCase(normalizedUrl, expectedPrefix))) {
            DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
        }
        if (mysqlType.contains(normalizedType)) {
            if (!startsWithIgnoreCase(normalizedUrl, "jdbc:mysql") && !startsWithIgnoreCase(normalizedUrl, "jdbc:mariadb")) {
                DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
            }

        }
        if (StringUtils.equals(normalizedType, "h2")) {
            if (StringUtils.contains(jdbcUrl, '\\') || StringUtils.contains(extraParams, '\\')
                    || containsIgnoreCase(jdbcUrl, "%5c") || containsIgnoreCase(extraParams, "%5c")) {
                DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
            }
            validateH2Settings(jdbcUrl, extraParams);
        }
        Set<String> dangerousFragments = new LinkedHashSet<>(COMMON_DANGEROUS_FRAGMENTS);
        dangerousFragments.addAll(TYPE_DANGEROUS_FRAGMENTS.getOrDefault(normalizedType, Set.of()));
        for (String fragment : dangerousFragments) {
            if (containsIgnoreCase(normalizedUrl, fragment) || containsIgnoreCase(normalizedExtraParams, fragment)) {
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
        return normalized;
    }

    private static String normalizeType(String type) {
        return StringUtils.defaultString(type).toLowerCase(Locale.ROOT);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return StringUtils.length(value) >= StringUtils.length(prefix)
                && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean containsIgnoreCase(String value, String fragment) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(fragment) || fragment.length() > value.length()) {
            return false;
        }
        for (int i = 0; i <= value.length() - fragment.length(); i++) {
            if (value.regionMatches(true, i, fragment, 0, fragment.length())) {
                return true;
            }
        }
        return false;
    }

    private static void validateH2Settings(String jdbcUrl, String extraParams) {
        String urlSettings = StringUtils.substringAfter(jdbcUrl, "jdbc:h2:");
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(urlSettings)) {
            parts.addAll(Arrays.asList(urlSettings.split(";", -1)));
        }
        if (StringUtils.isNotBlank(extraParams)) {
            parts.addAll(Arrays.asList(extraParams.split(";", -1)));
        }
        boolean firstPart = true;
        for (String part : parts) {
            String setting = part.trim();
            if (StringUtils.isBlank(setting)) {
                continue;
            }
            int equalIndex = setting.indexOf('=');
            if (equalIndex < 0) {
                if (firstPart) {
                    firstPart = false;
                    continue;
                }
                DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
            }
            firstPart = false;
            String key = setting.substring(0, equalIndex).trim().toUpperCase(Locale.ROOT);
            String value = setting.substring(equalIndex + 1).trim();
            if (!H2_ALLOWED_SETTINGS.contains(key)) {
                DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
            }
            if (StringUtils.equals(key, "MODE")) {
                if (!H2_ALLOWED_MODES.contains(value)) {
                    DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
                }
            } else if (!StringUtils.equalsIgnoreCase(value, "TRUE") && !StringUtils.equalsIgnoreCase(value, "FALSE")) {
                DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
            }
        }
    }
}
