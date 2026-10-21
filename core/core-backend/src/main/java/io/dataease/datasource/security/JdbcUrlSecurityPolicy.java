package io.dataease.datasource.security;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.provider.JdbcDriverPolicy;
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
            Map.entry("gaussdb", "jdbc:gaussdb"),
            Map.entry("pg", "jdbc:postgresql"),
            Map.entry("redshift", "jdbc:redshift"),
            Map.entry("h2", "jdbc:h2"),
            Map.entry("ck", "jdbc:clickhouse"),
            Map.entry("sqlite", "jdbc:sqlite:")
    );
    private static final List<String> mysqlType = Arrays.asList("mysql", "mongo", "mariadb", "starrocks", "doris", "tidb");

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
            "allowurlinlocalinfile",
            "allowloadlocalinfileinpath",
            "allowmultiqueries"
    );

    private static final Map<String, Set<String>> TYPE_DANGEROUS_FRAGMENTS = Map.ofEntries(
            Map.entry("mysql", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("mongo", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("mariadb", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("starrocks", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("doris", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("tidb", Set.of("maxallowedpacket", "allowurlinlocalinfile", "allowloadlocalinfileinpath", "allowmultiqueries")),
            Map.entry("impala", Set.of("krbjaasfile", "krb5.conf", "socketfactory", "dnsresolver")),
            Map.entry("sqlserver", Set.of("socketfactoryclass", "socketfactoryconstructorarg", "accesstokencallbackclass")),
            Map.entry("oracle", Set.of()),
            Map.entry("db2", Set.of()),
            Map.entry("gaussdb", Set.of("socketfactory", "socketfactoryarg", "sslfactory", "sslhostnameverifier", "sslpasswordcallback", "authenticationpluginclassname", "logger")),
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
        // allowLoadLocalInfile 允许显式关闭，但禁止开启或赋其他值
        validateAllowLoadLocalInfile(normalizedUrl);
        validateAllowLoadLocalInfile(normalizedExtraParams);
        return jdbcUrl;
    }

    public static String trustedDriverClass(String type) {
        return JdbcDriverPolicy.trustedDriverClass(type);
    }

    public static String resolveDriverClass(String type, String requestedDriver) {
        return JdbcDriverPolicy.resolveDriverClass(type, requestedDriver);
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

    private static void validateAllowLoadLocalInfile(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String key = "allowloadlocalinfile";
        int idx = 0;
        while (true) {
            int start = indexOfIgnoreCase(value, key, idx);
            if (start < 0) {
                return;
            }
            if (start > 0) {
                char prev = value.charAt(start - 1);
                if (prev != '?' && prev != '&' && prev != ';') {
                    idx = start + key.length();
                    continue;
                }
            }
            int pos = start + key.length();
            while (pos < value.length() && Character.isWhitespace(value.charAt(pos))) {
                pos++;
            }
            if (pos >= value.length() || value.charAt(pos) != '=') {
                DEException.throwException("Illegal parameter: " + key);
            }
            pos++;
            while (pos < value.length() && Character.isWhitespace(value.charAt(pos))) {
                pos++;
            }
            int end = pos;
            while (end < value.length() && value.charAt(end) != '&' && value.charAt(end) != ';') {
                end++;
            }
            String paramValue = value.substring(pos, end).trim();
            if (!"false".equalsIgnoreCase(paramValue)) {
                DEException.throwException("Illegal parameter: " + key);
            }
            idx = Math.max(end, start + key.length());
        }
    }

    private static int indexOfIgnoreCase(String value, String fragment, int fromIndex) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(fragment) || fromIndex < 0) {
            return -1;
        }
        for (int i = fromIndex; i <= value.length() - fragment.length(); i++) {
            if (value.regionMatches(true, i, fragment, 0, fragment.length())) {
                return i;
            }
        }
        return -1;
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
