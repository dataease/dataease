package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.ds.DatasourceRelationApi.*;
import io.dataease.datasource.manage.*;
import java.util.*;

public final class DatasetSqlRelationRegression {
    static Relation buyer() {
        return new Relation(1L, "下单客户", "customers", "orders",
                List.of(new Condition("tenant_id", "tenant_id"), new Condition("cid", "buyer_id")),
                Cardinality.ONE_TO_MANY, JoinType.RIGHT, "下单时的客户", true, true, List.of());
    }

    static DatasourceRelationManage registry(List<Relation> relations) {
        return new DatasourceRelationManage(null, null) {
            @Override public List<Relation> list(Long id) {
                if (id != 11L) throw new AssertionError("wrong source");
                return relations;
            }
        };
    }

    static List<String> fields(String table) {
        return switch (table) {
            case "orders" -> List.of("tenant_id", "oid", "buyer_id", "ship_customer_id", "actual_amt");
            case "customers" -> List.of("tenant_id", "cid", "name");
            default -> throw new AssertionError("unselected table");
        };
    }

    static Relation shipping() {
        return new Relation(2L, "收货客户", "orders", "customers",
                List.of(new Condition("tenant_id","tenant_id"), new Condition("ship_customer_id","cid")),
                Cardinality.MANY_TO_ONE, JoinType.LEFT, "", true, true, List.of());
    }

    public static void main(String[] args) throws Exception {
        var source = new AiDatasourceTools(null, null, null, null) {
            @Override public Datasource resolve(String id) { return new Datasource(id, "fixture", "1", "mysql"); }
            @Override public Datasource authorize(Scope scope) { return resolve(scope.resourceId()); }
            @Override public boolean hasPhysicalTable(Scope scope, String table) { return Set.of("orders", "customers").contains(table); }
            @Override public TableFields fields(Scope scope, String table, boolean nativeType) {
                return new TableFields(table, DatasetSqlRelationRegression.fields(table).stream().map(n -> new Field(n,"BIGINT","")).toList());
            }
        };
        var registered = new ArrayList<>(List.of(buyer()));
        var scene = new AiDatasetSqlScene(source, new DatasetSqlGuide(new EngineManage(), null), new DatasetSqlSemanticTools(source, DatasetSqlSemanticRegression.registry()), registry(registered));
        var context = scene.resolve("11", "");
        var argsMap = Map.of("fromTable", "orders", "toTable", "customers");
        var json = new ObjectMapper();
        var result = json.readTree(scene.execute(context, "read_relations", argsMap));
        var path = result.path("relations").get(0);
        if (!"MANY_TO_ONE".equals(path.path("cardinality").asText()) || !"LEFT".equals(path.path("joinType").asText())
                || !"orders".equals(path.path("leftTable").asText()) || !"customers".equals(path.path("rightTable").asText())
                || !path.path("conditions").toString().equals("[{\"leftField\":\"tenant_id\",\"rightField\":\"tenant_id\"},{\"leftField\":\"buyer_id\",\"rightField\":\"cid\"}]"))
            throw new AssertionError("complete reverse relation missing: " + result);
        registered.add(shipping());
        registered.add(new Relation(3L, "过期", "orders", "customers", List.of(new Condition("gone", "cid")),
                Cardinality.ONE_TO_ONE, JoinType.INNER, "", true, true, List.of()));
        registered.add(new Relation(4L, "未确认", "orders", "customers", buyer().conditions(),
                Cardinality.ONE_TO_ONE, JoinType.INNER, "", false, true, List.of()));
        String candidates = scene.execute(context, "read_relations", argsMap);
        if (json.readTree(candidates).path("relations").size() != 2 || !candidates.contains("CLARIFY_ROLE"))
            throw new AssertionError("ambiguous roles or unavailable paths: " + candidates);
        String sql = "SELECT o.oid,c.name FROM orders o LEFT JOIN customers c ON (c.cid=o.buyer_id AND o.tenant_id=c.tenant_id)";
        String proposal = scene.execute(context, "propose_sql", Map.of("sql", sql));
        var steps = List.of(new io.dataease.api.ai.conversation.AiConversationDtos.Tool("r", "read_relations", "SUCCEEDED", argsMap, candidates),
                new io.dataease.api.ai.conversation.AiConversationDtos.Tool("p", "propose_sql", "SUCCEEDED", Map.of("sql",sql), proposal));
        String evidence = json.valueToTree(scene.result(context, "", steps, "order", "1")).path("evidence").toString();
        if (!evidence.contains("下单客户") || !evidence.contains("MANY_TO_ONE") || evidence.contains("收货客户"))
            throw new AssertionError("only the composite path used in the proposal is evidence: " + evidence);
        for (String type : List.of("INNER", "RIGHT")) {
            String reversed = "SELECT c.name FROM customers c " + type + " JOIN orders o "
                    + "ON o.buyer_id=c.cid AND c.tenant_id=o.tenant_id";
            var proposed = new io.dataease.api.ai.conversation.AiConversationDtos.Tool("p2", "propose_sql", "SUCCEEDED",
                    Map.of("sql", reversed), scene.execute(context, "propose_sql", Map.of("sql", reversed)));
            var relation = scene.result(context, "", List.of(steps.getFirst(), proposed), "order", "2").evidence()
                    .stream().flatMap(t -> t.relations().stream()).findFirst().orElseThrow();
            var actual = json.valueToTree(relation).path("actual");
            if (!"LEFT".equals(relation.joinType()) || !type.equals(actual.path("joinType").asText())
                    || !"customers".equals(actual.path("leftTable").asText()) || !"orders".equals(actual.path("rightTable").asText()))
                throw new AssertionError("display registered direction and actual SQL independently: " + actual);
        }
        String wrong = "SELECT * FROM orders o JOIN customers c ON true JOIN items i "
                + "ON o.buyer_id=c.cid AND o.tenant_id=c.tenant_id";
        var unrelated = new io.dataease.api.ai.conversation.AiConversationDtos.Tool("p3", "propose_sql", "SUCCEEDED",
                Map.of("sql", wrong), scene.execute(context, "propose_sql", Map.of("sql", wrong)));
        if (scene.result(context, "", List.of(steps.getFirst(), unrelated), "order", "3").evidence()
                .stream().anyMatch(t -> !t.relations().isEmpty()))
            throw new AssertionError("equalities on the wrong join cannot confirm a relationship");
        registered.clear();
        if (!scene.execute(context, "read_relations", argsMap).contains("NO_CONFIRMED_PATH"))
            throw new AssertionError("missing registration must be distinguished from confirmed join");
    }
}
