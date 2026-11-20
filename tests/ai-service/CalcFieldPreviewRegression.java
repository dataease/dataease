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

/** Execute the preview projection through the built-in JDBC provider against one synthetic row. */
public final class CalcFieldPreviewRegression {
    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        var data = new DatasetDataManage();
        var schema = new DatasourceSchemaDTO();
        schema.setId(11L);
        schema.setType("mysql");
        schema.setSchemaAlias("s_a_11");
        schema.setConfiguration("{\"dataBase\":\"test\",\"queryTimeout\":10}");
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
        ReflectionTestUtils.setField(data, "datasetSQLManage", new DatasetSQLManage() {
            @Override public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dto, ChartExtRequest request) {
                return Map.of("sql", "SELECT 7 AS price, '-' AS label", "field", List.of(raw, text),
                        "dsMap", Map.of(11L, schema), "isFullJoin", false);
            }
        });

        try (var context = new GenericApplicationContext();
             var pool = new BasicDataSource();
             var calcite = DriverManager.getConnection("jdbc:calcite:").unwrap(CalciteConnection.class)) {
            pool.setUrl("jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
            var root = calcite.getRootSchema();
            root.add("s_a_11", JdbcSchema.create(root, "s_a_11", pool, null, null));
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
            assertVerdict(data, draft, 201, "AGGREGATE_PLACEHOLDER");
            assertVerdict(data, draft, 202, "AGGREGATE_PLACEHOLDER");

            candidate.setOriginName("[102]");
            candidate.setDeType(0);
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
            System.out.println("PASS JDBC preview distinguishes direct/indirect aggregates, ordinary dash and failed query");
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
