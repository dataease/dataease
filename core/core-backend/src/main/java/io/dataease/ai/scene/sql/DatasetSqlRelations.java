package io.dataease.ai.scene.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.ds.DatasourceRelationApi.*;
import io.dataease.datasource.manage.DatasourceRelationManage;
import java.util.*;
import java.util.stream.Collectors;

/** Project confirmed relationships in the requested orientation, checking every physical key. */
final class DatasetSqlRelations {
    private record Path(String name, String description, String leftTable, String rightTable,
                        List<Condition> conditions, Cardinality cardinality, JoinType joinType) {}

    static AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata, DatasourceRelationManage registry) {
        var definition = new AiToolProtocol.Definition("read_relations",
                "Read confirmed same-source relationships between two exact physical tables, oriented from the first table.",
                Map.of("fromTable", "Starting table, usually the rows to preserve", "toTable", "Other selected table"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            metadata.authorize(scope);
            String from = args.get("fromTable"), to = args.get("toTable");
            var paths = new ArrayList<Path>();
            if (metadata.hasPhysicalTable(scope, from) && metadata.hasPhysicalTable(scope, to)) {
                var registered = registry.list(Long.valueOf(scope.resourceId())).stream()
                        .filter(r -> r.confirmed() && r.valid() && !r.conditions().isEmpty())
                        .filter(r -> (r.leftTable().equals(from) && r.rightTable().equals(to))
                                || (r.leftTable().equals(to) && r.rightTable().equals(from))).toList();
                if (!registered.isEmpty()) {
                    var left = fields(metadata, scope, from);
                    active.run();
                    var right = fields(metadata, scope, to);
                    for (var r : registered) {
                        var path = orient(r, from, to);
                        if (path.conditions().stream().allMatch(c -> left.contains(c.leftField()) && right.contains(c.rightField())))
                            paths.add(path);
                    }
                }
            }
            active.run();
            metadata.authorize(scope);
            return AiToolProtocol.result(Map.of("relations", paths, "status", paths.isEmpty() ? "NO_CONFIRMED_PATH"
                    : paths.size() == 1 ? "CONFIRMED" : "CLARIFY_ROLE"));
        });
    }

    private static Set<String> fields(AiDatasourceTools metadata, AiDatasourceTools.Scope scope, String table) {
        return metadata.fields(scope, table, true).fields().stream()
                .map(AiDatasourceTools.Field::name).collect(Collectors.toSet());
    }

    private static Path orient(Relation r, String from, String to) {
        if (from.equals(r.leftTable()))
            return new Path(r.name(), r.description(), from, to, r.conditions(), r.cardinality(), r.joinType());
        var conditions = r.conditions().stream().map(c -> new Condition(c.rightField(), c.leftField())).toList();
        var cardinality = switch (r.cardinality()) {
            case MANY_TO_ONE -> Cardinality.ONE_TO_MANY;
            case ONE_TO_MANY -> Cardinality.MANY_TO_ONE;
            default -> r.cardinality();
        };
        var join = switch (r.joinType()) {
            case RIGHT -> JoinType.LEFT;
            case LEFT -> JoinType.RIGHT;
            default -> r.joinType();
        };
        return new Path(r.name(), r.description(), from, to, conditions, cardinality, join);
    }
}
