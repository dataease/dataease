package io.dataease.ai;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.manage.DatasetSQLManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.extensions.datasource.dto.ConnectionObj;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.view.dto.ChartExtRequest;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Exercise the built-in provider and Calcite against the same synthetic dates.
 * --mysql uses the read-only fixture_orders.orders fixture on FIXTURE_MYSQL_PORT (user previewer, MYSQL_PASSWORD).
 */
public final class CalcFieldPreviewRegression {
    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        boolean mysql = List.of(args).contains("--mysql");
        var data = new DatasetDataManage();
        var schema = new DatasourceSchemaDTO();
        schema.setId(11L);
        schema.setType("mysql");
        schema.setSchemaAlias("s_a_11");
        String database = mysql ? "fixture_orders" : "test";
        schema.setConfiguration("{\"dataBase\":\"" + database + "\",\"queryTimeout\":10}");
        var raw = field(101, 0, "price");
        raw.setDataeaseName("price");
        var text = field(102, 0, "label");
        text.setDataeaseName("label");
        text.setDeType(0);
        text.setDeExtractType(0);
        var total = field(201, 2, "SUM([101])");
        var candidate = field(202, 2, "[201] + 1");
        var draft = new DatasetGroupInfoDTO();
        draft.setIsCross(false);
        draft.setAllFields(List.of(raw, text, total, candidate));

        ReflectionTestUtils.setField(data, "datasetTableFieldManage", new DatasetTableFieldManage(null) {
            @Override public DatasetTableFieldDTO selectById(Long id) { return null; }
        });
        var empty = new java.util.concurrent.atomic.AtomicBoolean();
        ReflectionTestUtils.setField(data, "datasetSQLManage", new DatasetSQLManage() {
            @Override public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dto, ChartExtRequest request) {
                return Map.of("sql", "SELECT 7 AS price, '-' AS label" + (empty.get() ? " WHERE 1 = 0" : ""), "field", List.of(raw, text),
                        "dsMap", Map.of(11L, schema), "isFullJoin", false);
            }
        });

        try (var context = new GenericApplicationContext();
             var pool = new BasicDataSource();
             var calcite = DriverManager.getConnection("jdbc:calcite:lex=JAVA;caseSensitive=false").unwrap(CalciteConnection.class)) {
            if (mysql) {
                int port = Integer.parseInt(System.getenv("FIXTURE_MYSQL_PORT"));
                pool.setUrl("jdbc:mariadb://127.0.0.1:" + port + "/fixture_orders?allowPublicKeyRetrieval=true");
                pool.setUsername("previewer");
                pool.setPassword(System.getenv("MYSQL_PASSWORD"));
            } else {
                pool.setUrl("jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
                try (var conn = pool.getConnection(); var statement = conn.createStatement()) {
                    statement.execute("CREATE TABLE orders (order_day DATE)");
                    statement.execute("INSERT INTO orders VALUES (DATE '2026-02-22'), (DATE '2026-11-03')");
                }
            }
            var root = calcite.getRootSchema();
            root.add("s_a_11", JdbcSchema.create(root, "s_a_11", pool, mysql ? database : null, null));
            context.getBeanFactory().registerSingleton("calciteProvider", new CalciteProvider() {
                @Override public Connection take() { return calcite; }
                @Override public ConnectionObj getConnection(DatasourceDTO ignored) throws SQLException {
                    var borrowed = new ConnectionObj();
                    borrowed.setConnection(pool.getConnection());
                    return borrowed;
                }
            });
            context.refresh();
            new SpringContextUtil().setApplicationContext(context);
            var expired = new DatasourceRequest();
            expired.setIsCross(true);
            expired.setQuery("VALUES (1)");
            expired.setExecutionDeadline(System.currentTimeMillis() - 1);
            try {
                context.getBean(CalciteProvider.class).fetchResultField(expired);
                throw new AssertionError("Calcite executed an expired preview");
            } catch (DEException expected) {
                if (!(expected.getCause() instanceof java.sql.SQLTimeoutException)) throw expected;
            }
            draft.setPreviewFieldId(201L);
            if (!"AGGREGATE_PLACEHOLDER".equals(data.previewDataWithLimit(draft, 0, 100, false, false).get("validation")))
                throw new AssertionError("The manual preview must identify the selected aggregate");
            draft.setPreviewFieldId(null);
            assertVerdict(data, draft, 201, "AGGREGATE_PLACEHOLDER");
            assertVerdict(data, draft, 202, "AGGREGATE_PLACEHOLDER");

            candidate.setOriginName("[102]");
            candidate.setDeType(0);
            empty.set(true);
            assertVerdict(data, draft, 202, "NO_PREVIEW_ROWS");
            empty.set(false);
            assertVerdict(data, draft, 202, "DETAIL_PASS");
            var result = data.previewDataWithLimit(draft, 0, 1, false, false);
            var page = (Map<?, ?>) result.get("data");
            var row = (Map<?, ?>) ((List<?>) page.get("data")).getFirst();
            if (!"-".equals(row.get(candidate.getDataeaseName()))) throw new AssertionError("ordinary dash fixture");

            var divisor = new io.dataease.extensions.datasource.dto.CalParam();
            divisor.setId("params_9210000000000000003"); divisor.setName("Scale"); divisor.setValue("100.0");
            var converted = field(203, 2, "[101] / [params_9210000000000000003]");
            converted.setParams(List.of(divisor));
            var rate = new io.dataease.extensions.datasource.dto.CalParam();
            rate.setId("params_9210000000000000004"); rate.setName("Rate"); rate.setValue("2.0");
            candidate.setParams(List.of(rate));
            candidate.setDeType(3);
            draft.setAllFields(List.of(raw, text, total, converted, candidate));
            for (var unit : Map.of("[101] / 100.0 * [params_9210000000000000004]", "0.14",
                    "[101] * [params_9210000000000000004]", "14",
                    "[203] * [params_9210000000000000004]", "0.14").entrySet()) {
                candidate.setOriginName(unit.getKey());
                assertVerdict(data, draft, 202, "DETAIL_PASS");
                var preview = (Map<?, ?>) data.previewDataWithLimit(draft, 0, 1, false, false).get("data");
                var projected = (Map<?, ?>) ((List<?>) preview.get("data")).getFirst();
                var value = new java.math.BigDecimal(projected.get(candidate.getDataeaseName()).toString());
                if (value.compareTo(new java.math.BigDecimal(unit.getValue())) != 0)
                    throw new AssertionError("Unit conversion produced " + value + " for " + unit.getKey());
            }
            System.out.println("PASS JDBC cents, yuan and already-converted amount with two independent calculation parameters");

            candidate.setOriginName("UNKNOWN_FUNCTION([101])");
            try {
                data.previewCalcField(draft, 201, System.currentTimeMillis() + 10000);
                throw new AssertionError("failed query accepted an aggregate placeholder");
            } catch (DEException expected) {
                if (!(expected.getCause() instanceof SQLException) || expected.getMessage().contains("UNKNOWN_FUNCTION"))
                    throw new AssertionError("preview must preserve a typed driver cause and expose a safe message", expected);
            }
            verifyMonths(data, schema);
            System.out.println("PASS " + (mysql ? "MySQL" : "H2 MySQL mode") + " and Calcite return months 2 and 11");
            System.out.println("PASS JDBC preview distinguishes direct/indirect aggregates, ordinary dash and failed query");
        }
    }

    private static void verifyMonths(DatasetDataManage data, DatasourceSchemaDTO schema) throws Exception {
        var date = field(101, 0, "order_day");
        date.setDeType(1);
        date.setDeExtractType(1);
        date.setDataeaseName("order_day");
        ReflectionTestUtils.setField(data, "datasetSQLManage", new DatasetSQLManage() {
            @Override public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dto, ChartExtRequest request) {
                String table = dto.getIsCross() ? "`s_a_11`.`orders`" : "`orders`";
                return Map.of("sql", "SELECT order_day FROM " + table, "field", List.of(date),
                        "dsMap", Map.of(11L, schema), "isFullJoin", false);
            }
        });
        for (boolean cross : List.of(false, true)) {
            var draft = new DatasetGroupInfoDTO();
            draft.setIsCross(cross);
            var month = field(202, 2, cross ? "EXTRACT(MONTH FROM [101])" : "MONTH([101])");
            draft.setAllFields(List.of(date, month));
            assertVerdict(data, draft, 202, "DETAIL_PASS");
            var result = data.previewDataWithLimit(draft, 0, 100, false, false);
            var rows = (List<?>) ((Map<?, ?>) result.get("data")).get("data");
            var values = rows.stream().map(row -> ((Map<?, ?>) row).get(month.getDataeaseName()))
                    .map(value -> new java.math.BigDecimal(value.toString()).intValueExact()).sorted().toList();
            if (!values.equals(List.of(2, 11))) throw new AssertionError("preview month values: " + values);
        }
    }

    private static void assertVerdict(DatasetDataManage data, DatasetGroupInfoDTO draft,
                                      long fieldId, String expected) throws Exception {
        String verdict = data.previewCalcField(draft, fieldId, System.currentTimeMillis() + 10000);
        if (!expected.equals(verdict)) throw new AssertionError("field " + fieldId + ": " + verdict);
    }

    private static DatasetTableFieldDTO field(long id, int extField, String expression) {
        var f = new DatasetTableFieldDTO();
        f.setId(id);
        f.setDatasetTableId(1L);
        f.setOriginName(expression);
        f.setExtField(extField);
        f.setChecked(true);
        f.setDeType(2);
        f.setDeExtractType(2);
        return f;
    }
}
