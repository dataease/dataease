package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.ai.conversation.AiConversationDtos;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration.DatasourceType;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
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
        var dm = new XpackPluginsDatasourceVO();
        dm.setType("dm");
        dm.setName("达梦");
        var hive = new XpackPluginsDatasourceVO();
        hive.setType("hive");
        hive.setName("Apache Hive");
        var pluginGuide = new DatasetSqlGuide(engine, () -> List.of(dm, hive));
        var scene = new AiDatasetSqlScene(source, pluginGuide, new io.dataease.ai.tool.datasource.DatasetSqlSemanticTools(source, DatasetSqlSemanticRegression.registry()), DatasetSqlRelationRegression.registry(List.of()));
        var context = scene.resolve("11", "SELECT oid FROM orders");
        var json = new ObjectMapper();
        var cross = scene.resolve(new AiConversationDtos.Open(
                "dataset-sql", "11", "node", "", null, null, true));
        var monthly = json.readTree(scene.execute(cross, "read_sql_guide", Map.of("topic", "monthly")));
        if (!"Apache Calcite".equals(monthly.path("dialect").asText()) || !monthly.path("example").asText().contains("TO MONTH")
                || monthly.toString().contains("DATE_FORMAT") || !monthly.path("rules").asText().contains("canvas"))
            throw new AssertionError("Cross-source mode needs Calcite monthly buckets and one-node rules: " + monthly);
        var nativeMonthly = json.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", "monthly")));
        if (!nativeMonthly.path("example").asText().contains("DATE_FORMAT"))
            throw new AssertionError("MySQL needs a native month bucket: " + nativeMonthly);
        var projection = json.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", "projection")));
        if (!DatasourceType.mysql.getName().equals(projection.path("dialect").asText()) || projection.toString().contains("DATE_FORMAT")
                || !projection.path("example").asText().contains("SELECT"))
            throw new AssertionError("Simple projection must read only the native projection guide: " + projection);
        var parameters = json.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", "parameters")));
        var syntax = parameters.path("example").asText();
        for (String text : List.of("$DE_PARAM{", "$[", "${", "$f2cde[", "preview", "dashboard"))
            if (!syntax.contains(text)) throw new AssertionError("Missing editor parameter semantics: " + text);
        for (var binding : List.of(
                List.of("dm", "达梦"),
                List.of("hive", "Apache Hive"),
                List.of("pg", "PostgreSQL"),
                List.of("ck", DatasourceType.ck.getName()),
                List.of("es", "Elasticsearch"),
                List.of("custom_vendor", "Unspecified plugin dialect"))) {
            connector.set(binding.getFirst());
            for (boolean isCross : List.of(false, true)) {
                var editor = scene.resolve(new AiConversationDtos.Open("dataset-sql", "11", "node", "", null, null, isCross));
                var guide = json.readTree(scene.execute(editor, "read_sql_guide", Map.of("topic", "monthly")));
                var dialect = isCross ? "Apache Calcite" : binding.get(1);
                if (!editor.instruction().contains("\"sourceConnector\":" + json.writeValueAsString(binding.get(1)))
                        || !editor.instruction().contains("\"executionDialect\":" + json.writeValueAsString(dialect))
                        || !dialect.equals(guide.path("dialect").asText()))
                    throw new AssertionError("Prompt and guide must name the execution dialect for " + binding + ": " + guide);
                if ("dm".equals(binding.getFirst()) && !isCross && !guide.path("example").asText().contains("TRUNC(date_column, 'MM')"))
                    throw new AssertionError("Dameng needs its native monthly example: " + guide);
            }
        }
        for (var type : DatasourceType.values()) {
            if (type == DatasourceType.folder) continue;
            connector.set(type.getType());
            var nativeContext = scene.resolve("11", "");
            var guide = json.readTree(scene.execute(nativeContext, "read_sql_guide", Map.of("topic", "monthly")));
            boolean imported = List.of("API", "Excel", "ExcelRemote").contains(type.getType());
            String expected = imported ? DatasourceType.pg.getName() : type.getName();
            if (!expected.equals(guide.path("dialect").asText()) || !guide.path("example").asText().contains("GROUP BY")
                    || guide.path("identifiers").asText().isBlank())
                throw new AssertionError("Missing native engine guide for " + type + ": " + guide);
            if (imported && (!nativeContext.instruction().contains("\"executionDialect\":\"PostgreSQL\"")
                    || !guide.path("example").asText().contains("DATE_TRUNC")))
                throw new AssertionError("Imported data must use the configured DE engine: " + guide);
        }
    }
}
