package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dataease.ai.engine.conversation.AiConversationScene.Context;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalFields;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalStep;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.ai.conversation.AiConversationDtos;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.commons.utils.SqlparserUtils;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration.DatasourceType;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.JsonUtil;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.sql.parser.SqlParser;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.support.StaticMessageSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/** Model-facing guide for the formal editor. The datasource metadata boundary is controlled. */
public final class DatasetSqlDialectRegression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String UNSUPPORTED = "UNSUPPORTED_DIALECT";
    private static final Map<String, String> PROPOSAL = Map.of("sql", "SELECT oid FROM orders");

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        var connector = new AtomicReference<>("mysql");
        var source = new AiDatasourceTools(null, null, null, null) {
            @Override public Datasource resolve(String id) { return new Datasource(id, "orders", "1", connector.get()); }
        };
        var engineType = new AtomicReference<>("pg");
        var engine = new EngineManage() {
            @Override public CoreDatasource getDeEngine() {
                var ds = new CoreDatasource();
                ds.setType(engineType.get());
                return ds;
            }
        };
        String oldConfig = """
                {"type":"MySQL","name":"MySQL","prefix":"`","suffix":"`"}
                """;
        var plugin = JsonUtil.parseObject(oldConfig, XpackPluginsDatasourceVO.class);
        var registeredPlugin = new AtomicReference<>(plugin);
        var pluginGuide = new DatasetSqlGuide(engine, () -> List.of(registeredPlugin.get()));
        var scene = new AiDatasetSqlScene(source, pluginGuide, new io.dataease.ai.tool.datasource.DatasetSqlSemanticTools(source, DatasetSqlSemanticRegression.registry()), DatasetSqlRelationRegression.registry(List.of()));
        var unselected = scene.resolve("", "");
        if (!unselected.instruction().contains("\"sqlGenerationSupported\":false") || !scene.tools(unselected).isEmpty())
            throw new AssertionError("An editor without a datasource cannot offer SQL generation");
        var context = scene.resolve("11", PROPOSAL.get("sql"));
        var cross = scene.resolve(new AiConversationDtos.Open("dataset-sql", "11", "node", "", null, null, true));
        var mysql = monthlyGuide(scene, context);
        var calcite = monthlyGuide(scene, cross);
        if (!mysql.path("example").asText().contains("DATE_FORMAT(date_column, '%Y-%m')")
                || !calcite.path("example").asText().contains("FLOOR(date_column TO MONTH)")
                || !calcite.path("rules").asText().contains("画布"))
            throw new AssertionError("MySQL and Calcite must receive their own monthly expression");
        checkTopics(scene, context, DatasourceType.mysql.getName());
        checkTopics(scene, cross, "Apache Calcite");
        try {
            scene.execute(context, "read_sql_guide", Map.of("topic", "unknown"));
            throw new AssertionError("An unknown topic must be rejected");
        } catch (AiErrors.Failure error) {
            if (!"invalid".equals(error.safeCode())) throw error;
        }
        for (var type : DatasourceType.values()) {
            if (type == DatasourceType.folder) continue;
            connector.set(type.getType());
            boolean imported = List.of(DatasourceType.API, DatasourceType.Excel, DatasourceType.ExcelRemote).contains(type);
            checkBinding(scene, false, type.getName(), imported ? DatasourceType.pg.getName() : type.getName(),
                    type == DatasourceType.mysql ? mysql : null);
            checkBinding(scene, true, type.getName(), "Apache Calcite", calcite);
        }
        engineType.set("mysql");
        for (var imported : List.of(DatasourceType.API, DatasourceType.Excel, DatasourceType.ExcelRemote)) {
            connector.set(imported.getType());
            checkBinding(scene, false, imported.getName(), DatasourceType.mysql.getName(), mysql);
        }

        connector.set("unregistered_mysql");
        checkBinding(scene, false, "Unspecified plugin dialect", "Unspecified plugin dialect", null);
        connector.set(plugin.getType());
        checkBinding(scene, false, plugin.getName(), plugin.getName(), null);
        plugin.setAiSqlBaseDialect("pg");
        checkBinding(scene, false, plugin.getName(), plugin.getName(), null);
        plugin.setAiSqlBaseDialect("calcite");
        checkBinding(scene, false, plugin.getName(), plugin.getName(), calcite);
        var config = JSON.readValue(oldConfig, ObjectNode.class).put("aiSqlBaseDialect", "mysql");
        registeredPlugin.set(JsonUtil.parseObject(config.toString(), XpackPluginsDatasourceVO.class));
        var enabledEditor = checkBinding(scene, false, plugin.getName(), plugin.getName(), mysql);
        connector.set("MYSQL");
        checkBinding(scene, false, "Unspecified plugin dialect", "Unspecified plugin dialect", null);
        connector.set(plugin.getType());
        registeredPlugin.set(JsonUtil.parseObject(oldConfig, XpackPluginsDatasourceVO.class));
        checkUnsupportedGuide(monthlyGuide(scene, enabledEditor));
        checkProposal(scene, enabledEditor, false);
        checkBinding(scene, true, plugin.getName(), "Apache Calcite", calcite);
    }

    private static void checkTopics(AiDatasetSqlScene scene, Context context, String dialect) throws Exception {
        var examples = Map.of("projection", "SELECT column_name", "monthly", context.isCross() ? "TO MONTH" : "DATE_FORMAT(",
                "aggregate", "GROUP BY dimension", "join", "LEFT JOIN", "parameters", "$DE_PARAM{");
        for (var topic : examples.keySet()) {
            var guide = JSON.readTree(scene.execute(context, "read_sql_guide", Map.of("topic", topic)));
            if (!topic.equals(guide.path("topic").asText()) || !dialect.equals(guide.path("dialect").asText()))
                throw new AssertionError("Wrong guide for " + topic + ": " + guide);
            String example = guide.path("example").asText();
            for (var entry : examples.entrySet()) {
                if (example.contains(entry.getValue()) != entry.getKey().equals(topic))
                    throw new AssertionError("Wrong SQL example in " + topic + " guide: " + entry.getValue());
            }
            var queries = Pattern.compile("(?ms)^```sql\\s*\\R(.*?)^```").matcher(example).results()
                    .map(match -> match.group(1).strip()).toList();
            if (queries.isEmpty()) throw new AssertionError("Missing SQL sample for " + topic);
            if (topic.equals("parameters")) {
                checkParameterExamples(queries, context.isCross());
            } else {
                for (var query : queries) {
                    if (context.isCross()) SqlParser.create(query, SqlParser.config().withLex(Lex.JAVA)).parseQuery();
                    else CCJSqlParserUtil.parse(query);
                }
            }
        }
    }

    private static JsonNode monthlyGuide(AiDatasetSqlScene scene, Context editor) throws Exception {
        return JSON.readTree(scene.execute(editor, "read_sql_guide", Map.of("topic", "monthly")));
    }

    private static Context checkBinding(AiDatasetSqlScene scene, boolean cross, String source, String dialect,
                                        JsonNode template) throws Exception {
        var editor = scene.resolve(new AiConversationDtos.Open("dataset-sql", "11", "node", "", null, null, cross));
        boolean supported = template != null;
        if (!editor.instruction().contains("\"sourceConnector\":" + JSON.writeValueAsString(source))
                || !editor.instruction().contains("\"executionDialect\":" + JSON.writeValueAsString(dialect))
                || !editor.instruction().contains("\"sqlGenerationSupported\":" + supported))
            throw new AssertionError("Wrong model context for " + source + " (cross=" + cross + ", supported=" + supported + ")");
        var guide = monthlyGuide(scene, editor);
        if (!dialect.equals(guide.path("dialect").asText())) throw new AssertionError("Wrong dialect: " + guide);
        if (supported) {
            if (guide.path("identifiers").asText().isBlank() || !template.path("identifiers").equals(guide.path("identifiers"))
                    || !template.path("example").equals(guide.path("example")))
                throw new AssertionError("Expected the declared base dialect's identifiers and example: " + guide);
        } else {
            checkUnsupportedGuide(guide);
        }
        checkProposal(scene, editor, supported);
        return editor;
    }

    private static void checkUnsupportedGuide(JsonNode guide) {
        if (!UNSUPPORTED.equals(guide.path("status").asText()) || guide.has("example") || guide.has("identifiers"))
            throw new AssertionError("Unsupported guides must omit SQL and identifier examples: " + guide);
    }

    private static void checkProposal(AiDatasetSqlScene scene, Context editor, boolean supported) throws Exception {
        var payload = scene.execute(editor, "propose_sql", PROPOSAL);
        var proposal = JSON.readTree(payload);
        var tool = new AiConversationDtos.Tool("offer", "propose_sql", "SUCCEEDED", PROPOSAL, payload);
        var result = scene.result(editor, "", List.of(tool), "node", "draft");
        if (supported) {
            if (!"NOT_RUN".equals(proposal.path("status").asText()) || !PROPOSAL.get("sql").equals(result.sql()))
                throw new AssertionError("Adapted engines must still offer a complete unrun query: " + proposal);
        } else {
            var step = new HistoricalStep(1, "offer", "propose_sql", "SUCCEEDED", PROPOSAL, payload, null);
            var history = scene.historicalEvidence(editor, step, new HistoricalFields(List.of(), false, false, false));
            if (!UNSUPPORTED.equals(proposal.path("status").asText()) || proposal.has("sql")
                    || !"EXPLANATION".equals(result.type()) || result.sql() != null || !UNSUPPORTED.equals(history.get("status")))
                throw new AssertionError("Unsupported proposals must stay explanatory in the editor and history: " + proposal);
        }
    }

    private static void checkParameterExamples(List<String> queries, boolean cross) throws Exception {
        var datasource = new DatasourceSchemaDTO();
        datasource.setType("mysql");
        var user = new UserFormVO();
        user.setAccount("current-user");
        String settings = """
                [{"variableName":"account","type":["TEXT"],"defaultValue":"dashboard-user","defaultValueScope":"EDIT"}]
                """;
        var expectedAccounts = Map.of("$[account]", "dashboard-user", "${account}", "dashboard-user",
                "$f2cde[账号]", "current-user");
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:dataset_sql_guide;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        try (var db = source.getConnection(); var setup = db.createStatement();
             var execution = cross ? calcite(source) : source.getConnection()) {
            setup.execute("CREATE TABLE users (account VARCHAR(32))");
            setup.execute("INSERT INTO users VALUES ('current-user'), ('dashboard-user')");
            for (var example : expectedAccounts.entrySet()) {
                String queryExample = queries.stream().filter(sql -> sql.contains(example.getKey()))
                        .findFirst().orElseThrow(() -> new AssertionError("Missing query example for " + example.getKey()));
                // The editor sends the built-in account ID for the visible label.
                String sql = queryExample.replace("$f2cde[账号]", "$f2cde[sysParams.userId]");
                var preview = new SqlparserUtils().handleVariableDefaultValueWithPreparedParams(
                        sql, settings, true, true, null, cross, Map.of(1L, datasource), null, user);
                var bindings = preview.getTableFieldWithValues();
                if (bindings.size() != 1) throw new AssertionError("Expected one bound account for " + sql);
                try (var query = execution.prepareStatement(preview.getSql())) {
                    query.setObject(1, bindings.getFirst().getValue());
                    try (var rows = query.executeQuery()) {
                        if (!rows.next() || !example.getValue().equals(rows.getString(1)) || rows.next())
                            throw new AssertionError("Expected only " + example.getValue() + " for " + sql);
                    }
                }
            }
        }
    }

    private static Connection calcite(JdbcDataSource source) throws Exception {
        var calcite = DriverManager.getConnection("jdbc:calcite:lex=JAVA;caseSensitive=false").unwrap(CalciteConnection.class);
        var schema = calcite.getRootSchema();
        schema.add("fixture", JdbcSchema.create(schema, "fixture", source, null, "public"));
        calcite.setSchema("fixture");
        return calcite;
    }
}
