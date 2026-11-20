package io.dataease.ai;

import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.PhysicalTableCatalog;
import io.dataease.extensions.datasource.dto.*;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.dbcp2.BasicDataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Count the actual built-in provider enumerations and JDBC column probes for a whole editor turn. */
public final class DatasetSqlProviderRegression {
    private static final class ObservedPool extends BasicDataSource {
        int catalogs, types;

        @Override public Connection getConnection() throws SQLException {
            var connection = super.getConnection();
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        try {
                            var value = method.invoke(connection, args);
                            if (method.getName().equals("createStatement")) return observe((Statement) value, null);
                            if (method.getName().equals("prepareStatement")) return observe((Statement) value, (String) args[0]);
                            return value;
                        } catch (InvocationTargetException wrapped) { throw wrapped.getCause(); }
                    });
        }

        private Statement observe(Statement stmt, String preparedSql) {
            Class<?> api = preparedSql == null ? Statement.class : PreparedStatement.class;
            return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{api}, (proxy, method, args) -> {
                try {
                    var value = method.invoke(stmt, args);
                    if (method.getName().equals("executeQuery")) {
                        var query = (preparedSql == null ? (String) args[0] : preparedSql).toUpperCase(Locale.ROOT);
                        if (query.contains("INFORMATION_SCHEMA.COLUMNS")) catalogs++;
                        if (query.contains("ZZ_CHANNEL_") && (query.contains("LIMIT 0") || query.contains("FETCH NEXT 0")
                                || query.contains("FETCH FIRST 0"))) types++;
                    }
                    return value;
                } catch (InvocationTargetException wrapped) { throw wrapped.getCause(); }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        boolean mysql = Arrays.asList(args).contains("--mysql");
        try (var pool = new ObservedPool();
             var calcite = DriverManager.getConnection("jdbc:calcite:lex=MYSQL").unwrap(CalciteConnection.class)) {
            if (mysql) {
                int port = Integer.parseInt(System.getenv("FIXTURE_MYSQL_PORT"));
                pool.setUrl("jdbc:mysql://127.0.0.1:" + port + "/physical_1022981?useSSL=false&allowPublicKeyRetrieval=true");
                pool.setUsername("fixture"); pool.setPassword(System.getenv("MYSQL_PASSWORD"));
            } else {
                pool.setUrl("jdbc:h2:mem:physical_" + UUID.randomUUID());
            }
            try (var anchor = pool.getConnection(); var statement = anchor.createStatement()) {
                if (!mysql) {
                    for (int n = 0; n < 300; n++) statement.execute("CREATE TABLE T" + n + "(ID INT)");
                    statement.execute("CREATE TABLE \"ZZ_Channel_Live\"(CHANNEL_ID INT)");
                    statement.execute("CREATE TABLE \"ZZ_Channel_Archive\"(CHANNEL_ID INT)");
                    statement.execute("CREATE SCHEMA OTHER_SCOPE");
                    statement.execute("CREATE TABLE OTHER_SCOPE.\"ZZ_Channel_Live\"(OUTSIDE_SCOPE INT)");
                }
                var root = calcite.getRootSchema();
                root.add("s_a_11", JdbcSchema.create(root, "s_a_11", pool, null, null));
                var enumerations = new AtomicInteger();
                var provider = new CalciteProvider() {
                    @Override public Connection take() { return calcite; }
                    @Override public List<DatasetTableDTO> getTables(DatasourceRequest request) {
                        enumerations.incrementAndGet();
                        return super.getTables(request);
                    }
                };
                var schema = new DatasourceSchemaDTO();
                schema.setId(11L); schema.setSchemaAlias("s_a_11"); schema.setType(mysql ? "mysql" : "h2");
                schema.setConfiguration(mysql ? "{\"dataBase\":\"physical_1022981\"}" : "{}");
                var metadata = new AiDatasourceTools(null, null, null, null) {
                    @Override public Datasource resolve(String id) { return new Datasource(id, "fixture", "1", schema.getType()); }
                    @Override public Datasource authorize(Scope scope) { return resolve(scope.resourceId()); }
                    @Override protected PhysicalTableCatalog openCatalog(long id) { return provider.openCatalog(schema, (name, label) -> true); }
                };
                var scene = new AiDatasetSqlScene(metadata, new DatasetSqlGuide(new EngineManage(), null), new DatasetSqlSemanticTools(metadata, DatasetSqlSemanticRegression.registry()), DatasetSqlRelationRegression.registry(java.util.List.of()));
                var turn = scene.beginTurn(scene.resolve("11", ""));
                var candidates = turn.execute("search_physical_tables", Map.of("terms", "Channel"), () -> {}, Long.MAX_VALUE);
                if (!candidates.contains("ZZ_Channel_Live") || !candidates.contains("ZZ_Channel_Archive") || pool.catalogs != 0 || pool.types != 0)
                    throw new AssertionError(candidates);
                for (String table : List.of("ZZ_Channel_Live", "ZZ_Channel_Archive", "ZZ_Channel_Live")) {
                    String evidence = turn.execute("get_table_fields", Map.of("table", table), () -> {}, Long.MAX_VALUE);
                    if (!evidence.contains("CHANNEL_ID") || evidence.contains("OUTSIDE_SCOPE")) throw new AssertionError(evidence);
                }
                if (enumerations.get() != 1 || pool.catalogs != 2 || pool.types != 2)
                    throw new AssertionError("Provider.getTables=" + enumerations + " column-catalog reads=" + pool.catalogs + " zero-row type reads=" + pool.types);
                System.out.println("PASS " + schema.getType() + " 302-table catalog; 2 candidates, 1 Provider.getTables, 2 column-catalog queries + 2 zero-row type queries for 2 distinct tables");
            }
        }
    }
}
