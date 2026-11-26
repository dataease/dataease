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
import java.util.regex.Pattern;

/** Attribute a proposal to its own tool reads. Failure to extract lineage never blocks applying a draft. */
final class DatasetSqlEvidence {
    private static final ObjectMapper JSON = new ObjectMapper();
    private DatasetSqlEvidence() {}

    private static final Pattern TEMPORAL_QUESTION = Pattern.compile(
            "(?:时间|時間|日期|時刻|时刻)(?:类型的?|類型的?)?(?:字段|欄位|列)|\\b(?:time|date|temporal|timestamp|datetime)[ -]+(?:fields?|columns?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TEMPORAL_TYPE = Pattern.compile(
            "\\b(?:date(?:32|64)?|(?:small)?datetime(?:2|64|offset)?|timestamp(?:tz|_(?:ltz|ntz|tz))?|timetz|time|year)\\b",
            Pattern.CASE_INSENSITIVE);
    // Quoted identifiers and physical snake_case names are distinguishable from ordinary prose.
    private static final Pattern IDENTIFIER_HINT = Pattern.compile(
            "[`\"]([^`\"\\r\\n]{1,256})[`\"]|\\[([^\\]\\r\\n]{1,256})\\]|(?<![a-zA-Z0-9_$])([a-zA-Z][a-zA-Z0-9]*(?:_[a-zA-Z0-9]+)+)(?![a-zA-Z0-9_$])");

    // Leave bare physical names in place, including "type" in "the type of type".
    // Plural "date columns" asks about a category; singular "the date column" may name a column.
    private static final Pattern ENGLISH_FIELD_SYNTAX = Pattern.compile(
            "\\b(?:step\\s*#?\\s*\\d+|(?:first|second|third|\\d+(?:st|nd|rd|th)?)\\s+(?:answer|reply|response)"
                    + "|(?:answer|reply|response)\\s*#?\\s*\\d+|(?:date|time|temporal|timestamp|datetime)[ -]+(?:fields|columns)"
                    + "|(?:data\\s+)?(?:types?|names?|units?|formats?|meanings?|descriptions?|status)\\s+(?:of|for)"
                    + "|(?:what|which)\\s+(?:data\\s+)?types?\\b|what|which|the|a|an|is|are|was|were|do|does|did"
                    + "|there|any|and|or|in|on|at|from|of|for|columns?|fields?)\\b",
            Pattern.CASE_INSENSITIVE);

    /** An interrupted table read has no recorded columns. Completed reads use the original structure. */
    static Map<String, Object> historicalFields(String expectedTable, String result, String question) {
        if (expectedTable == null) return Map.of();
        try {
            var snapshot = result == null ? new TableEvidence(expectedTable, false, null, null, List.of())
                    : JSON.readValue(result, TableEvidence.class);
            if (!expectedTable.equals(snapshot.table()) || snapshot.fields() == null) return Map.of();
            var missing = new LinkedHashSet<String>();
            var hints = IDENTIFIER_HINT.matcher(question);
            while (hints.find()) {
                String name = hints.group();
                if (name.startsWith("`") || name.startsWith("\"") || name.startsWith("["))
                    name = name.substring(1, name.length() - 1);
                missing.add(name);
            }
            var identifiers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            identifiers.addAll(missing);
            String prose = ENGLISH_FIELD_SYNTAX.matcher(hints.replaceAll(" ")).replaceAll(" ");
            var named = snapshot.fields().stream()
                    .filter(field -> identifiers.contains(field.name()) || mentioned(prose, field.name())).toList();
            missing.removeIf(expectedTable::equalsIgnoreCase);
            for (var field : named) missing.removeIf(field.name()::equalsIgnoreCase);
            boolean temporal = named.isEmpty() && missing.isEmpty() && TEMPORAL_QUESTION.matcher(question).find();
            if (!temporal && named.isEmpty() && missing.isEmpty()) return Map.of();
            var selected = temporal ? snapshot.fields().stream().filter(field -> field.type() != null
                    && TEMPORAL_TYPE.matcher(field.type()).find()).toList() : named;
            var evidence = new LinkedHashMap<String, Object>();
            evidence.put("source", "HISTORICAL_STEP");
            evidence.put("table", snapshot.table());
            evidence.put("coverage", snapshot.inspected() ? "COMPLETE" : "PARTIAL");
            evidence.put("fields", selected);
            if (!missing.isEmpty()) evidence.put(snapshot.inspected() ? "absentFields" : "unrecordedFields", missing);
            return evidence;
        } catch (java.io.IOException invalid) {
            return Map.of();
        }
    }

    private static boolean mentioned(String question, String name) {
        if (name == null || name.isBlank()) return false;
        return Pattern.compile("(?<![a-zA-Z0-9_$])" + Pattern.quote(name) + "(?![a-zA-Z0-9_$])",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(question).find();
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
