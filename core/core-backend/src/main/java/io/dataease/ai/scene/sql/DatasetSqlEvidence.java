package io.dataease.ai.scene.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.ai.conversation.AiConversationDtos.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;

/** Attribute a proposal to its own tool reads. Failure to extract lineage never blocks applying a draft. */
final class DatasetSqlEvidence {
    private static final ObjectMapper JSON = new ObjectMapper();
    private DatasetSqlEvidence() {}

    static Map<String, Object> lineage(String sql) {
        try {
            var statement = CCJSqlParserUtil.parse(sql, parser -> parser.withTimeOut(500).withAllowComplexParsing(false));
            var columns = new Columns();
            var tables = columns.getTables(statement);
            var result = new TreeMap<String, Set<String>>();
            for (var table : tables) {
                var name = identifier(table);
                result.put(name, columns.references.getOrDefault(name, Set.of()));
            }
            return Map.of("status", "NOT_RUN", "references", result, "joins", columns.joins);
        } catch (JSQLParserException | RuntimeException unavailable) {
            return Map.of("status", "NOT_RUN", "references", Map.of(), "joins", List.of());
        }
    }

    private static String identifier(String raw) {
        if (raw.length() > 1 && ((raw.startsWith("\"") && raw.endsWith("\""))
                || (raw.startsWith("`") && raw.endsWith("`")) || (raw.startsWith("[") && raw.endsWith("]"))))
            return raw.substring(1, raw.length() - 1);
        return raw;
    }

    private static final class Columns extends TablesNamesFinder {
        private final Map<String, Set<String>> references = new HashMap<>();
        private final List<Set<List<String>>> joins = new ArrayList<>();
        private Map<String, String> bindings = Map.of();

        @Override public void visit(PlainSelect select) {
            var outer = bindings;
            bindings = new HashMap<>();
            bind(select.getFromItem());
            if (select.getJoins() != null) select.getJoins().forEach(join -> bind(join.getRightItem()));
            if (select.getJoins() != null) for (var join : select.getJoins()) {
                var keys = new HashSet<List<String>>();
                for (var on : join.getOnExpressions()) equalities(on, keys);
                if (!keys.isEmpty()) joins.add(keys);
            }
            super.visit(select);
            if (select.getGroupBy() != null && select.getGroupBy().getGroupByExpressionList() != null)
                select.getGroupBy().getGroupByExpressionList().accept(this);
            if (select.getOrderByElements() != null)
                select.getOrderByElements().forEach(order -> order.getExpression().accept(this));
            bindings = outer;
        }

        private void equalities(Expression on, Set<List<String>> keys) {
            if (on instanceof Parenthesis group) {
                equalities(group.getExpression(), keys);
            } else if (on instanceof AndExpression and) {
                equalities(and.getLeftExpression(), keys);
                equalities(and.getRightExpression(), keys);
            } else if (on instanceof EqualsTo eq && eq.getLeftExpression() instanceof Column a
                    && eq.getRightExpression() instanceof Column b && a.getTable() != null && b.getTable() != null) {
                var left = bindings.get(identifier(a.getTable().getFullyQualifiedName()));
                var right = bindings.get(identifier(b.getTable().getFullyQualifiedName()));
                if (left != null && right != null)
                    keys.add(List.of(left, identifier(a.getColumnName()), right, identifier(b.getColumnName())));
            }
        }

        private void bind(FromItem item) {
            if (item == null) return;
            var name = item instanceof Table table ? identifier(table.getFullyQualifiedName()) : "";
            bindings.put(item.getAlias() == null ? name : identifier(item.getAlias().getName()), name);
        }

        private void record(Table qualifier, String column) {
            String name = qualifier == null && bindings.size() == 1 ? bindings.values().iterator().next()
                    : qualifier == null ? null : bindings.get(identifier(qualifier.getFullyQualifiedName()));
            if (name != null && !name.isEmpty()) references.computeIfAbsent(name, k -> new TreeSet<>()).add(column);
        }

        @Override public void visit(Column column) { record(column.getTable(), identifier(column.getColumnName())); }
        @Override public void visit(SelectItem item) {
            if (item.getExpression() instanceof AllTableColumns all) record(all.getTable(), "*");
            else if (item.getExpression() instanceof AllColumns) {
                for (String name : bindings.values()) references.computeIfAbsent(name, k -> new TreeSet<>()).add("*");
            } else super.visit(item);
        }
    }

    static List<TableEvidence> forProposal(List<Tool> tools, String sql) {
        var inspected = new HashMap<String, TableEvidence>();
        var relations = new LinkedHashSet<RelationEvidence>();
        List<TableEvidence> evidence = List.of();
        for (var tool : tools) {
            if (!"SUCCEEDED".equals(tool.status())) continue;
            try {
                if ("get_table_fields".equals(tool.name())) {
                    var table = JSON.readValue(tool.result(), TableEvidence.class);
                    if (table.inspected()) inspected.put(table.table(), table);
                } else if ("read_relations".equals(tool.name())) {
                    for (var path : JSON.readTree(tool.result()).path("relations"))
                        relations.add(JSON.treeToValue(path, RelationEvidence.class));
                } else if ("propose_sql".equals(tool.name()) && sql.equals(tool.parameters().get("sql"))) {
                    var used = new ArrayList<TableEvidence>();
                    var lineage = JSON.readTree(tool.result());
                    var matched = new ArrayList<RelationEvidence>();
                    for (var join : lineage.path("joins")) {
                        Set<List<String>> keys = JSON.convertValue(join, new com.fasterxml.jackson.core.type.TypeReference<>() {});
                        for (var path : relations) {
                            if (path.conditions().stream().allMatch(c -> keys.contains(List.of(path.leftTable(), c.leftField(), path.rightTable(), c.rightField()))
                                    || keys.contains(List.of(path.rightTable(), c.rightField(), path.leftTable(), c.leftField()))))
                                matched.add(path);
                        }
                    }
                    lineage.path("references").fields().forEachRemaining(entry -> {
                        var table = inspected.get(entry.getKey());
                        var paths = matched.stream().filter(r -> r.leftTable().equals(entry.getKey())).distinct().toList();
                        if (table == null) {
                            used.add(new TableEvidence(entry.getKey(), false, null, null, List.of(), paths));
                            return;
                        }
                        var names = new HashSet<String>();
                        entry.getValue().forEach(name -> names.add(name.asText()));
                        used.add(new TableEvidence(table.table(), true, table.description(), table.grain(),
                                table.fields().stream().filter(f -> names.contains("*") || names.contains(f.name())).toList(), paths));
                    });
                    evidence = List.copyOf(used);
                }
            } catch (java.io.IOException unreadable) {
                // A legacy tool result may lack the structured evidence projection.
            }
        }
        return evidence;
    }
}
