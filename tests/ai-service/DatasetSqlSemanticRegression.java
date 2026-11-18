package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.manage.DatasourceSemanticManage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** Real editor scene and tool protocol with a synthetic registry and a controlled live metadata boundary. */
public final class DatasetSqlSemanticRegression {
    public static void main(String[] args) throws Exception {
        var json = new ObjectMapper();
        var allowed = new AtomicBoolean(true);
        var revokeAfterRead = new AtomicBoolean();
        var source = new AiDatasourceTools(null, null, null, null) {
            @Override public Datasource resolve(String id) { return new Datasource(id, "fixture", "1", "mysql"); }
            @Override public Datasource authorize(Scope scope) {
                if (!allowed.get()) throw new SecurityException("READ revoked");
                return resolve(scope.resourceId());
            }
            @Override public List<Table> tables(Scope scope) { throw new AssertionError("search accessed remote catalog"); }
            @Override public TableFields fields(Scope scope, String table, boolean nativeType) {
                if (revokeAfterRead.get()) allowed.set(false);
                return new TableFields(table, List.of(new Field("actual_amt", "DECIMAL", ""),
                        new Field("pay_code", "CHAR", ""), new Field("due_amt", "DECIMAL", ""),
                        new Field("changed", "INT", ""), new Field("pending", "CHAR", "")));
            }
        };
        var scene = new AiDatasetSqlScene(source, new io.dataease.datasource.manage.EngineManage(), new DatasetSqlSemanticTools(source, registry()), DatasetSqlRelationRegression.registry(List.of()));
        var context = scene.resolve("11", "");
        var found = json.readTree(scene.execute(context, "search_semantics", Map.of("terms", "订单,已支付,实付金额")));
        if (found.path("tables").size() != 1 || !found.path("tables").toString().contains("orders")) throw new AssertionError(found);
        var details = scene.execute(context, "get_table_fields", Map.of("table", "orders"));
        if (!details.contains("实付金额") || !details.contains("已支付") || !details.contains("每订单一行")
                || details.contains("pending-meaning") || details.contains("obsolete-") || details.contains("example-only")) {
            throw new AssertionError("Only live, independently confirmed meanings and actual value mappings may be delivered: " + details);
        }
        String sql = "SELECT o.actual_amt, 'due_amt' AS quoted FROM orders o WHERE o.pay_code = 'P'";
        var proposal = scene.execute(context, "propose_sql", Map.of("sql", sql));
        var tools = List.of(new io.dataease.api.ai.conversation.AiConversationDtos.Tool("read", "get_table_fields", "SUCCEEDED", Map.of("table", "orders"), details),
                new io.dataease.api.ai.conversation.AiConversationDtos.Tool("proposal", "propose_sql", "SUCCEEDED", Map.of("sql", sql), proposal));
        var result = json.valueToTree(scene.result(context, "examined due_amt too", tools, "node", "1"));
        String evidence = result.path("evidence").toString();
        if (!evidence.contains("orders") || !evidence.contains("actual_amt") || !evidence.contains("已支付")
                || evidence.contains("due_amt") || evidence.contains("changed")) throw new AssertionError("wrong proposal evidence: " + result);
        revokeAfterRead.set(true);
        try {
            scene.execute(context, "get_table_fields", Map.of("table", "orders"));
            throw new AssertionError("returned metadata after revocation during read");
        } catch (SecurityException denied) { /* expected */ }
    }

    static DatasourceSemanticManage registry() {
        return new DatasourceSemanticManage(null, null, null, null) {
            @Override public Semantics list(Long id) {
                return new Semantics(List.of(new TableSemantic(1L, "orders", "销售订单", "每订单一行",
                        true, "销售", true, true, 6, 5),
                        new TableSemantic(2L, "old_orders", "旧订单", "", true, "", false, true, 0, 0)));
            }
            @Override public List<FieldSemantic> registeredFields(Long id) {
                return List.of(field("actual_amt", "实付金额", "DECIMAL", true, List.of()),
                        field("pay_code", "支付状态", "CHAR", true, List.of(new ValueReference("P", "已支付"), new ValueReference("example-only", ""))),
                        field("due_amt", "应收金额", "DECIMAL", true, List.of()),
                        field("changed", "obsolete-type-meaning", "CHAR", true, List.of()),
                        field("dropped", "obsolete-column-meaning", "CHAR", true, List.of()),
                        field("pending", "pending-meaning", "CHAR", false, List.of()));
            }
            @Override public TableDetails detail(Long id, Long tableId) { return new TableDetails(registeredFields(id), 0); }
        };
    }

    private static FieldSemantic field(String name, String business, String type, boolean confirmed, List<ValueReference> values) {
        return new FieldSemantic("orders", name, business, "", "检索别名", confirmed, type, "", values);
    }
}
