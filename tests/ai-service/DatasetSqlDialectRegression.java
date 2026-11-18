package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.ai.conversation.AiConversationDtos;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration.DatasourceType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Model-facing guide for the formal editor. The datasource metadata boundary is controlled. */
public final class DatasetSqlDialectRegression {
    public static void main(String[] args) throws Exception {
        var connector = new AtomicReference<>("mysql");
        var source = new AiDatasourceTools(null, null, null, null) {
            @Override public Datasource resolve(String id) { return new Datasource(id, "orders", "1", connector.get()); }
        };
        var engine = new EngineManage() {
            @Override public CoreDatasource getDeEngine() {
                var ds = new CoreDatasource();
                ds.setType("pg");
                return ds;
            }
        };
        var scene = new AiDatasetSqlScene(source, engine, new io.dataease.ai.tool.datasource.DatasetSqlSemanticTools(source, DatasetSqlSemanticRegression.registry()));
        var context = scene.resolve("11", "SELECT oid FROM orders");
        var json = new ObjectMapper();
        var cross = scene.resolve(new AiConversationDtos.Open(
                "dataset-sql", "11", "node", "", null, null, true));
        var monthly = json.readTree(scene.execute(cross, "read_sql_guide", Map.of("topic", "monthly")));
        if (!"calcite".equals(monthly.path("dialect").asText()) || !monthly.path("example").asText().contains("TO MONTH")
                || monthly.toString().contains("DATE_FORMAT") || !monthly.path("rules").asText().contains("canvas"))
            throw new AssertionError("Cross-source mode needs Calcite monthly buckets and one-node rules: " + monthly);
        var nativeMonthly = json.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", "monthly")));
        if (!nativeMonthly.path("example").asText().contains("DATE_FORMAT"))
            throw new AssertionError("MySQL needs a native month bucket: " + nativeMonthly);
        var projection = json.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", "projection")));
        if (!"mysql".equals(projection.path("dialect").asText()) || projection.toString().contains("DATE_FORMAT")
                || !projection.path("example").asText().contains("SELECT"))
            throw new AssertionError("Simple projection must read only the native projection guide: " + projection);
        for (var type : DatasourceType.values()) {
            if (type == DatasourceType.folder) continue;
            connector.set(type.getType());
            var nativeContext = scene.resolve("11", "");
            var guide = json.readTree(scene.execute(nativeContext, "read_sql_guide", Map.of("topic", "monthly")));
            String expected = List.of("API", "Excel", "ExcelRemote").contains(type.getType()) ? "pg" : type.getType();
            if (!expected.equals(guide.path("dialect").asText()) || !guide.path("example").asText().contains("GROUP BY")
                    || guide.path("identifiers").asText().isBlank())
                throw new AssertionError("Missing native engine guide for " + type + ": " + guide);
            if ("pg".equals(expected) && !guide.path("example").asText().contains("DATE_TRUNC"))
                throw new AssertionError("Imported data must use the configured DE engine: " + guide);
        }
    }
}
