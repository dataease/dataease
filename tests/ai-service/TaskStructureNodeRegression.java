package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiToolProtocol;
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
            System.out.println("PASS legacy structure requires its original raw hash, exact instruction prefix and strict complete JSON object snapshot");
        } finally {
            try (var files = Files.list(directory)) { for (var file : files.toList()) Files.deleteIfExists(file); }
            Files.deleteIfExists(directory);
        }
    }

    private static void node(Path directory, String mode) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource()); V3UserUtil.setUid(1L);
        var changed = new AtomicReference<String>("");
        var beans = new StaticListableBeanFactory();
        var access = new AiAccess(null, null) {
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
                case "getTables" -> { var table = new DatasetTableDTO(); table.setTableName(changed.get().equals("table") ? "AI_OTHER" : "AI_PROBE"); table.setName("Probe"); yield List.of(table); }
                case "getTableField" -> {
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
        var sql = new AiSqlConversationScene(datasource, beans.getBeanProvider(InteractiveAuthApi.class), access, new CorePermissionManage());
        try {
            var structure = new AiStructureMutexScene(sql); var sqlContext = sql.resolve("11", "SELECT ID FROM AI_PROBE");
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
        } finally { sql.destroy(); V3UserUtil.clear(); }
    }

    private static AiConversationScene.Context legacy(AiConversationScene.Context context, String projection) throws Exception {
        String datasource = JSON.readTree(context.revision()).path("datasource").asText();
        String revision = JSON.writeValueAsString(Map.of("datasource", datasource, "structure", AiPromptFingerprint.of(new Prompt(projection))));
        return new AiConversationScene.Context(context.resourceId(), context.resourceName(), revision,
                context.instruction().substring(0, context.instruction().indexOf(PROJECTION) + PROJECTION.length()) + projection, context.selectedTables());
    }

    private static void expectScopeChanged(AiStructureMutexScene structure, AiConversationScene.Context context) {
        try { structure.authorize(context); throw new AssertionError("unbound or malformed legacy snapshot accepted"); }
        catch (AiErrors.Failure expected) { if (!expected.safeCode().equals("scope_changed")) throw expected; }
    }
}
