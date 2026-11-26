package io.dataease.ai.scene.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalFields;
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
import java.util.regex.Pattern;

/** Attribute a proposal to its own tool reads. Failure to extract lineage never blocks applying a draft. */
final class DatasetSqlEvidence {
    private static final ObjectMapper JSON = new ObjectMapper();
    private DatasetSqlEvidence() {}

    private static final Map<String, Set<String>> HISTORICAL_OUTCOMES = Map.of(
            "propose_sql", Set.of("INVALID", "NOT_RUN"),
            "read_relations", Set.of("CONFIRMED", "NO_CONFIRMED_PATH", "CLARIFY_ROLE"),
            "search_physical_tables", Set.of("MATCHED", "NO_MATCH", "EMPTY_CATALOG"));

    static Map<String, Object> historicalOutcome(String tool, String result) {
        var allowed = HISTORICAL_OUTCOMES.get(tool);
        if (allowed == null || result == null) return Map.of();
        try {
            String status = JSON.readTree(result).path("status").asText("");
            return allowed.contains(status) ? Map.of("status", status) : Map.of();
        } catch (java.io.IOException invalid) {
            return Map.of();
        }
    }

    private static final Pattern TEMPORAL_TYPE = Pattern.compile(
            "\\b(?:date(?:32|64)?|(?:small)?datetime(?:2|64|offset)?|timestamp(?:tz|_(?:ltz|ntz|tz))?|timetz|time|year)\\b",
            Pattern.CASE_INSENSITIVE);
    /** Completed reads expose only the requested facts; interrupted reads may have no recorded fields. */
    static Map<String, Object> historicalFields(String expectedTable, String result, HistoricalFields selection) {
        boolean details = selection.temporal() || !selection.names().isEmpty();
        if (expectedTable == null || !details && !selection.count() && !selection.listNames()) return Map.of();
        try {
            var data = result == null ? JSON.createObjectNode().put("table", expectedTable) : JSON.readTree(result);
            if (!expectedTable.equals(data.path("table").asText())) return Map.of();
            var rows = data.path("fields");
            boolean complete = rows.isArray() && data.path("inspected").asBoolean(false);
            var evidence = new LinkedHashMap<String, Object>();
            evidence.put("coverage", complete ? "COMPLETE" : "PARTIAL");
            if (selection.count() && rows.isArray()) evidence.put("fieldCount", rows.size());
            if (selection.listNames() && rows.isArray()) {
                var names = new ArrayList<String>();
                for (var row : rows) names.add(row.path("name").asText());
                evidence.put("fieldNames", names);
            }
            if (!details) return evidence;

            var fields = rows.isArray() ? Arrays.asList(JSON.treeToValue(rows, FieldEvidence[].class)) : List.<FieldEvidence>of();
            var requested = new HashSet<>(selection.names());
            var selected = fields.stream().filter(field -> requested.contains(field.name())
                    || selection.temporal() && field.type() != null && TEMPORAL_TYPE.matcher(field.type()).find()).toList();
            evidence.put("source", "HISTORICAL_STEP");
            evidence.put("table", expectedTable);
            evidence.put("fields", selected);

            var found = new HashSet<String>();
            selected.forEach(field -> found.add(field.name()));
            var verifiedAbsent = new HashSet<String>();
            data.path("absentFields").forEach(field -> verifiedAbsent.add(field.asText()));
            var absent = new ArrayList<String>();
            var unrecorded = new ArrayList<String>();
            for (String name : selection.names()) {
                if (found.contains(name)) continue;
                if (complete || verifiedAbsent.contains(name)) absent.add(name);
                else unrecorded.add(name);
            }
            if (!absent.isEmpty()) evidence.put("absentFields", absent);
            if (!unrecorded.isEmpty()) evidence.put("unrecordedFields", unrecorded);
            return evidence;
        } catch (java.io.IOException invalid) {
            return Map.of();
        }
    }

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

    private record JoinedKeys(String rightTable, String joinType, Set<List<String>> keys) {
        RelationEvidence match(RelationEvidence path) {
            boolean reversed = rightTable.equals(path.leftTable());
            if (!reversed && !rightTable.equals(path.rightTable()) || path.conditions().isEmpty()) return null;
            for (var c : path.conditions()) {
                if (!keys.contains(List.of(path.leftTable(), c.leftField(), path.rightTable(), c.rightField()))
                        && !keys.contains(List.of(path.rightTable(), c.rightField(), path.leftTable(), c.leftField()))) return null;
            }
            var actual = new SqlJoin(reversed ? path.rightTable() : path.leftTable(), joinType, rightTable);
            return new RelationEvidence(path.name(), path.description(), path.leftTable(), path.rightTable(),
                    path.conditions(), path.cardinality(), path.joinType(), actual);
        }
    }

    private static final class Columns extends TablesNamesFinder {
        private final Map<String, Set<String>> references = new HashMap<>();
        private final List<JoinedKeys> joins = new ArrayList<>();
        private Map<String, String> bindings = Map.of();

        @Override public void visit(PlainSelect select) {
            var outer = bindings;
            bindings = new HashMap<>();
            bind(select.getFromItem());
            if (select.getJoins() != null) for (var join : select.getJoins()) {
                bind(join.getRightItem());
                if (!(join.getRightItem() instanceof Table table) || join.isSimple() || join.isNatural()
                        || join.isCross() || join.isSemi() || join.isApply() || join.isStraight() || join.isWindowJoin()) continue;
                var rightTable = identifier(table.getFullyQualifiedName());
                var keys = new HashSet<List<String>>();
                for (var on : join.getOnExpressions()) equalities(on, keys);
                if (!keys.isEmpty()) {
                    String type = join.isFull() ? "FULL" : join.isLeft() ? "LEFT" : join.isRight() ? "RIGHT" : "INNER";
                    joins.add(new JoinedKeys(rightTable, type, keys));
                }
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
                if (left != null && !left.isEmpty() && right != null && !right.isEmpty() && !left.equals(right)
                        && Collections.frequency(bindings.values(), left) == 1 && Collections.frequency(bindings.values(), right) == 1)
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
                } else if ("get_named_table_fields".equals(tool.name())) {
                    var read = JSON.readTree(tool.result());
                    String name = tool.parameters().get("table");
                    if (name == null || !name.equals(read.path("table").asText()) || !read.path("fields").isArray()) continue;
                    var previous = inspected.getOrDefault(name, new TableEvidence(name, false, null, null, List.of()));
                    var fields = new LinkedHashMap<String, FieldEvidence>();
                    previous.fields().forEach(field -> fields.put(field.name(), field));
                    read.path("absentFields").forEach(absent -> fields.remove(absent.asText()));
                    for (var field : JSON.treeToValue(read.get("fields"), FieldEvidence[].class)) fields.put(field.name(), field);
                    inspected.put(name, new TableEvidence(name, previous.inspected(), previous.description(), previous.grain(),
                            List.copyOf(fields.values())));
                } else if ("read_relations".equals(tool.name())) {
                    for (var path : JSON.readTree(tool.result()).path("relations"))
                        relations.add(JSON.treeToValue(path, RelationEvidence.class));
                } else if ("propose_sql".equals(tool.name()) && sql.equals(tool.parameters().get("sql"))) {
                    var used = new ArrayList<TableEvidence>();
                    var lineage = JSON.readTree(tool.result());
                    var matched = new ArrayList<RelationEvidence>();
                    for (var join : lineage.path("joins")) {
                        if (!join.isObject()) continue;
                        var joined = JSON.treeToValue(join, JoinedKeys.class);
                        for (var path : relations) {
                            var match = joined.match(path);
                            if (match != null) matched.add(match);
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
                        used.add(new TableEvidence(table.table(), table.inspected(), table.description(), table.grain(),
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
