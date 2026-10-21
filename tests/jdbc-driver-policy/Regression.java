import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.provider.ExtendedJdbcClassLoader;
import io.dataease.extensions.datasource.provider.JdbcDriverPolicy;
import io.dataease.extensions.sync.model.datasource.ConnectionObj;
import io.dataease.extensions.sync.utils.ProviderUtil;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class Regression {
    private static int checks;
    private static String connectedUrl;

    public static void main(String[] args) throws Exception {
        Map<String, String> drivers = Map.ofEntries(
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
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            drivers.forEach((type, driver) -> {
                equal(driver, JdbcDriverPolicy.trustedDriverClass(type.toUpperCase(Locale.ROOT)));
                equal(driver, JdbcDriverPolicy.resolveDriverClass(type, null));
                equal(driver, JdbcDriverPolicy.resolveDriverClass(type, ""));
                equal(driver, JdbcDriverPolicy.resolveDriverClass(type, " \t"));
                equal(driver, JdbcDriverPolicy.resolveDriverClass(type, driver));
                equal(driver, JdbcUrlSecurityPolicy.trustedDriverClass(type));
                equal(driver, JdbcUrlSecurityPolicy.resolveDriverClass(type, driver));
                rejected(() -> JdbcDriverPolicy.resolveDriverClass(type, "java.lang.Runtime"));
                rejected(() -> JdbcUrlSecurityPolicy.resolveDriverClass(type, "java.lang.Runtime"));
            });
        } finally {
            Locale.setDefault(previous);
        }
        for (String type : new String[] {null, "", "unknown"}) {
            rejected(() -> JdbcDriverPolicy.trustedDriverClass(type));
            rejected(() -> JdbcDriverPolicy.resolveDriverClass(type, "org.mariadb.jdbc.Driver"));
        }
        rejected(() -> JdbcDriverPolicy.resolveDriverClass("mysql", "org.postgresql.Driver"));
        rejected(() -> JdbcDriverPolicy.resolveDriverClass("mysql", "org.mariadb.jdbc.Driver "));

        for (String type : new String[] {"mysql", "doris", "oracle", "db2", "sqlServer"}) {
            verifyConnection(type, null, drivers.get(type.toLowerCase(Locale.ROOT)));
            verifyConnection(type, drivers.get(type.toLowerCase(Locale.ROOT)), drivers.get(type.toLowerCase(Locale.ROOT)));
        }
        try (RecordingLoader loader = new RecordingLoader()) {
            DatasourceConfiguration configuration = configuration("mysql", "java.lang.Runtime");
            connectedUrl = null;
            rejected(() -> ProviderUtil.getConnection(configuration, loader));
            equal(null, loader.loadedClass);
            equal(null, connectedUrl);
        }
        System.out.println("PASS: " + checks + " checks; SDK/core driver policy and sync connection entry");
    }

    private static void verifyConnection(String type, String driver, String expected) throws Exception {
        try (RecordingLoader loader = new RecordingLoader();
             ConnectionObj connection = ProviderUtil.getConnection(configuration(type, driver), loader)) {
            equal(expected, loader.loadedClass);
            equal("jdbc:test:" + type, connectedUrl);
            equal(true, connection.getConnection() != null);
        }
    }

    private static DatasourceConfiguration configuration(String type, String driver) {
        DatasourceConfiguration configuration = new DatasourceConfiguration();
        configuration.setType(type);
        configuration.setDriver(driver);
        configuration.setJdbc("jdbc:test:" + type);
        return configuration;
    }

    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
        checks++;
    }

    private static void rejected(Runnable action) {
        try {
            action.run();
        } catch (DEException exception) {
            equal("invalid driver", exception.getMessage());
            return;
        }
        throw new AssertionError("Expected invalid driver rejection");
    }

    private static final class RecordingLoader extends ExtendedJdbcClassLoader {
        private String loadedClass;

        private RecordingLoader() {
            super(new URL[0], Regression.class.getClassLoader());
        }

        @Override
        public Class<?> loadClass(String name) {
            loadedClass = name;
            return RecordingDriver.class;
        }
    }

    public static class RecordingDriver implements Driver {
        @Override
        public Connection connect(String url, Properties info) {
            connectedUrl = url;
            return (Connection) Proxy.newProxyInstance(Regression.class.getClassLoader(),
                    new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                        if (method.getName().equals("close")) {
                            return null;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        @Override
        public boolean acceptsURL(String url) { return true; }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }

        @Override
        public int getMajorVersion() { return 1; }

        @Override
        public int getMinorVersion() { return 0; }

        @Override
        public boolean jdbcCompliant() { return false; }

        @Override
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
