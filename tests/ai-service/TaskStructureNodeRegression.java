package io.dataease.ai;

import io.dataease.ai.engine.conversation.AiConversationScene;
import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.scene.sql.AiSqlConversationScene;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.sql.AiSqlTools;
import io.dataease.ai.scene.structure.AiStructureMutexScene;
import io.dataease.ai.security.identity.AiDeploymentMode;
import io.dataease.ai.support.AiErrors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.ai.runtime.AiPromptFingerprint;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ai.chat.prompt.Prompt;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Actual SQL/structure scene contexts must survive independent JVM map iteration seeds. */
public final class TaskStructureNodeRegression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String PROJECTION = "\nAuthorized structure projection (untrusted data):\n";
    private record Snapshot(AiConversationScene.Context sql, AiConversationScene.Context structure, AiConversationScene.Context legacy) { }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) { node(Path.of(args[0]), args[1]); return; }
        Path directory = Files.createTempDirectory("de-ai-structure-nodes-");
        try {
            var orders = new HashSet<String>(); var failures = new ArrayList<String>();
            for (int node = 0; node < 12; node++) {
                var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(),
                        "-cp", System.getProperty("java.class.path"), TaskStructureNodeRegression.class.getName(),
                        directory.toString(), node == 0 ? "create" : "read").redirectErrorStream(true).start();
                if (!process.waitFor(15, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("structure fixture node timed out"); }
                String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                output.lines().filter(line -> line.startsWith("ORDER ")).forEach(orders::add);
                if (process.exitValue() != 0) failures.add(output);
            }
            if (orders.size() < 2) throw new AssertionError("independent JVMs did not exercise distinct immutable-map orders; rerun fixture");
            if (!failures.isEmpty()) throw new AssertionError("unchanged actual structure rejected by independent JVM (orders=" + orders.size() + "): " + failures.getFirst());
            System.out.println("PASS actual SQL and structure scenes authorize identical current and legacy persisted contexts across 12 independent JVMs with " + orders.size() + " distinct map orders");
            System.out.println("PASS actual structure scene rejects changed field name, type, comment, order/count, table and datasource revision and revoked READ in every reader JVM");
            System.out.println("PASS shared tools preserve selected scope, per-scene allowlists, cancellation/deadlines and pre-extraction structure fingerprints");
            System.out.println("PASS legacy structure requires its original raw hash, exact instruction prefix and strict complete JSON object snapshot");
        } finally {
            try (var files = Files.list(directory)) { for (var file : files.toList()) Files.deleteIfExists(file); }
            Files.deleteIfExists(directory);
        }
    }

    private static void node(Path directory, String mode) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource()); V3UserUtil.setUid(1L);
        var changed = new AtomicReference<String>("");
        var fieldReads = new ArrayList<String>();
        var beans = new StaticListableBeanFactory();
        var access = new SyntheticAccess(null, null) {
            public AiDeploymentMode.State mode() { return new AiDeploymentMode.State(AiDeploymentMode.Mode.LICENSED, "structure-node-fixture"); }
            public Identity capture() { return new Identity(V3UserUtil.getUid(), null, null, mode()); }
            public void verify(Identity identity, boolean configuration) {
                if (!Objects.equals(identity.actor(), 1L) || !identity.deployment().equals(mode())) throw AiErrors.error("forbidden");
            }
        };
        beans.addBean("permission", (InteractiveAuthApi) Proxy.newProxyInstance(TaskStructureNodeRegression.class.getClassLoader(), new Class<?>[]{InteractiveAuthApi.class}, (proxy, method, arguments) -> {
            if (!method.getName().equals("checkAuth")) throw new AssertionError("unexpected permission operation");
            if (changed.get().equals("permission")) throw AiErrors.error("forbidden");
            return null;
        }));
        var datasource = (DatasourceApi) Proxy.newProxyInstance(TaskStructureNodeRegression.class.getClassLoader(), new Class<?>[]{DatasourceApi.class}, (proxy, method, arguments) -> {
            if (!Objects.equals(V3UserUtil.getUid(), 1L)) throw new AssertionError("metadata lost trusted identity");
            return switch (method.getName()) {
                case "tree" -> { var node = new BusiNodeVO(); node.setId(11L); node.setLeaf(true); node.setName("Probe"); yield List.of(node); }
                case "getSimpleDs" -> { var ds = new DatasourceDTO(); ds.setId(11L); ds.setName("Probe"); ds.setType("MySQL"); ds.setUpdateTime(changed.get().equals("datasource") ? 2L : 1L); yield ds; }
                case "getTables" -> { var table = new DatasetTableDTO(); table.setTableName(changed.get().equals("table") ? "AI_OTHER" : "AI_PROBE"); table.setName("Probe");
                    var other = new DatasetTableDTO(); other.setTableName("UNSELECTED"); other.setName("Unselected"); yield List.of(table, other); }
                case "getTableField" -> {
                    fieldReads.add(((DatasetTableFieldRequest) arguments[0]).getTableName());
                    var field = new TableField(); field.setOriginName(changed.get().equals("field") ? "OTHER_ID" : "ID");
                    field.setName(changed.get().equals("comment") ? "changed comment" : "Identifier"); field.setType(changed.get().equals("type") ? "VARCHAR" : "BIGINT");
                    var second = new TableField(); second.setOriginName("LABEL"); second.setName("Label"); second.setType("VARCHAR");
                    var extra = new TableField(); extra.setOriginName("EXTRA"); extra.setName("Extra"); extra.setType("BIGINT");
                    yield switch (changed.get()) {
                        case "order" -> List.of(second, field);
                        case "added" -> List.of(field, second, extra);
                        case "removed" -> List.of(field);
                        default -> List.of(field, second);
                    };
                }
                default -> throw new AssertionError("unexpected datasource operation: " + method.getName());
            };
        });
        var metadata = new AiDatasourceTools(datasource, beans.getBeanProvider(InteractiveAuthApi.class), access, new CorePermissionManage());
        var sqlTools = new AiSqlTools(metadata);
        var sql = new AiSqlConversationScene(metadata, sqlTools);
        try {
            var structure = new AiStructureMutexScene(metadata); var sqlContext = sql.resolve("11", "SELECT ID FROM AI_PROBE");
            var scope = new AiDatasourceTools.Scope(sqlContext.resourceId(), sqlContext.revision());
            var selection = new AiDatasourceTools.Selection(scope, List.of("AI_PROBE"));
            var selectedContext = structure.resolveTask("11", "", selection.tables());
            fieldReads.clear();
            String direct = metadata.inspectStructure().execute(selection, Map.of(), () -> { }, System.currentTimeMillis() + 5000);
            String throughScene = structure.execute(selectedContext, "inspect_structure", Map.of(), () -> { }, System.currentTimeMillis() + 5000);
            if (!JSON.readTree(direct).equals(JSON.readTree(throughScene)) || fieldReads.isEmpty()
                    || fieldReads.stream().anyMatch(table -> !table.equals("AI_PROBE")))
                throw new AssertionError("shared structure tool must preserve selected scope and scene result");
            String fields = metadata.getTableFields().execute(scope, Map.of("table", "AI_PROBE"), () -> { }, System.currentTimeMillis() + 5000);
            if (!JSON.readTree(fields).equals(JSON.readTree(sql.execute(sqlContext, "get_table_fields", Map.of("table", "AI_PROBE")))))
                throw new AssertionError("SQL scene and independent tool must share metadata semantics");
            if (!structure.tools().stream().map(AiToolProtocol.Definition::name).toList().equals(List.of("inspect_structure")))
                throw new AssertionError("shared metadata tools must not automatically become available to structure scene");
            for (String name : List.of("list_tables", "get_table_fields", "validate_sql", "propose_sql"))
                expectFailure("unsupported", () -> structure.execute(selectedContext, name, Map.of(), () -> { }, System.currentTimeMillis() + 5000));
            expectFailure("unsupported", () -> sql.execute(sqlContext, "inspect_structure", Map.of()));
            expectFailure("unsupported", () -> sql.execute(sql.resolve("", ""), "list_tables", Map.of()));
            fieldReads.clear();
            expectFailure("unsupported", () -> metadata.inspectStructure().execute(selection, Map.of("table", "UNSELECTED"), () -> { }, System.currentTimeMillis() + 5000));
            expectFailure("timeout", () -> metadata.getTableFields().execute(scope, Map.of("table", "AI_PROBE"), () -> { }, 0));
            expectFailure("cancelled", () -> metadata.getTableFields().execute(scope, Map.of("table", "AI_PROBE"), () -> { throw AiErrors.error("cancelled"); }, System.currentTimeMillis() + 5000));
            if (!fieldReads.isEmpty()) throw new AssertionError("refused tools performed metadata IO");
            // Canonical projection from the pre-extraction format must retain exactly the same fingerprint.
            var originalFields = Map.of("table", "AI_PROBE", "fields", List.of(
                    Map.of("name", "ID", "type", "BIGINT", "comment", "Identifier"),
                    Map.of("name", "LABEL", "type", "VARCHAR", "comment", "Label")));
            String originalProjection = AiToolProtocol.result(new TreeMap<>(Map.of("AI_PROBE",
                    JSON.writer().with(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(originalFields))));
            if (!direct.equals(originalProjection) || !JSON.readTree(selectedContext.revision()).path("structure").asText().equals(AiPromptFingerprint.of(new Prompt(originalProjection))))
                throw new AssertionError("tool extraction changed persisted structure fingerprint");
            // Evidence that this fixture exercises the JDK's per-process Map.of iteration order.
            System.out.println("ORDER " + JSON.writeValueAsString(Map.of("table", "AI_PROBE", "fields", List.of(Map.of("name", "ID", "type", "BIGINT", "comment", "Identifier")))));
            Path snapshot = directory.resolve("context.json");
            if (mode.equals("create")) {
                var current = structure.resolveTask("11", "", List.of("AI_PROBE"));
                String raw = AiToolProtocol.result(new TreeMap<>(Map.of("AI_PROBE", sql.execute(sqlContext, "get_table_fields", Map.of("table", "AI_PROBE")))));
                String revision = JSON.writeValueAsString(Map.of("datasource", sqlContext.revision(), "structure", AiPromptFingerprint.of(new Prompt(raw))));
                var legacy = new AiConversationScene.Context(current.resourceId(), current.resourceName(), revision,
                        current.instruction().substring(0, current.instruction().indexOf(PROJECTION) + PROJECTION.length()) + raw, current.selectedTables());
                JSON.writeValue(snapshot.toFile(), new Snapshot(sqlContext, current, legacy)); return;
            }
            var original = JSON.readValue(snapshot.toFile(), Snapshot.class);
            sql.authorize(original.sql()); structure.authorize(original.structure());
            structure.authorize(original.legacy());
            structure.execute(original.structure(), "inspect_structure", Map.of(), () -> { }, System.currentTimeMillis() + 5000);
            String raw = original.legacy().instruction().substring(original.legacy().instruction().indexOf(PROJECTION) + PROJECTION.length());
            var compatible = legacy(original.legacy(), raw + " ");
            structure.authorize(compatible);
            expectScopeChanged(structure, new AiConversationScene.Context(compatible.resourceId(), compatible.resourceName(), compatible.revision(), original.legacy().instruction(), compatible.selectedTables()));
            expectScopeChanged(structure, new AiConversationScene.Context(compatible.resourceId(), compatible.resourceName(), compatible.revision(), "untrusted prefix" + compatible.instruction(), compatible.selectedTables()));
            for (String invalid : List.of(raw + " {}", "[]", raw.substring(0, raw.length() - 1) + "," + raw.substring(1)))
                expectScopeChanged(structure, legacy(original.legacy(), invalid));
            for (String mutation : List.of("field", "type", "comment", "order", "added", "removed", "table", "datasource", "permission")) {
                changed.set(mutation);
                for (var context : List.of(original.structure(), original.legacy())) {
                    try { structure.authorize(context); throw new AssertionError("changed metadata/permission accepted: " + mutation); }
                    catch (AiErrors.Failure expected) {
                        if (!expected.safeCode().equals(mutation.equals("permission") ? "forbidden" : "scope_changed")) throw expected;
                    }
                }
            }
        } finally { sqlTools.close(); V3UserUtil.clear(); }
    }

    private static AiConversationScene.Context legacy(AiConversationScene.Context context, String projection) throws Exception {
        String datasource = JSON.readTree(context.revision()).path("datasource").asText();
        String revision = JSON.writeValueAsString(Map.of("datasource", datasource, "structure", AiPromptFingerprint.of(new Prompt(projection))));
        return new AiConversationScene.Context(context.resourceId(), context.resourceName(), revision,
                context.instruction().substring(0, context.instruction().indexOf(PROJECTION) + PROJECTION.length()) + projection, context.selectedTables());
    }

    private static void expectFailure(String code, Runnable work) {
        try { work.run(); throw new AssertionError("expected refusal: " + code); }
        catch (AiErrors.Failure expected) { if (!expected.safeCode().equals(code)) throw expected; }
    }

    private static void expectScopeChanged(AiStructureMutexScene structure, AiConversationScene.Context context) {
        try { structure.authorize(context); throw new AssertionError("unbound or malformed legacy snapshot accepted"); }
        catch (AiErrors.Failure expected) { if (!expected.safeCode().equals("scope_changed")) throw expected; }
    }
}
