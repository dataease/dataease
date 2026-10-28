package io.dataease.commons.utils;

import io.dataease.extensions.view.dto.SqlVariableDetails;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.function.Function;

/**
 * Keeps each filter on a SQL variable as a separate AND condition. Filter values are
 * still resolved by the caller's prepared-binding builder, never inserted into SQL.
 */
class SqlVariableFilterExpander {
    private final Map<String, List<SqlVariableDetails>> repeatedParameters = new LinkedHashMap<>();
    private final Map<String, Binding> bindings = new LinkedHashMap<>();
    private List<SqlVariableDetails> parameters = Collections.emptyList();

    void reset(List<SqlVariableDetails> parameters) {
        this.parameters = parameters == null ? Collections.emptyList() : parameters;
        repeatedParameters.clear();
        bindings.clear();
        for (SqlVariableDetails parameter : this.parameters) {
            if (parameter.getVariableName() != null) {
                repeatedParameters.computeIfAbsent(normalize(parameter.getVariableName()), key -> new ArrayList<>()).add(parameter);
            }
        }
        repeatedParameters.values().removeIf(values -> values.size() < 2);
    }

    String register(List<String> variableNames, Function<Map<String, SqlVariableDetails>, String> builder) {
        List<String> names = variableNames.stream().map(SqlVariableFilterExpander::normalize)
                .filter(repeatedParameters::containsKey).distinct().toList();
        if (names.isEmpty()) {
            return null;
        }
        String token = "DE_FILTER_BIND_" + bindings.size();
        bindings.put(token, new Binding(names, builder));
        return "'" + token + "'";
    }

    List<SqlVariableDetails> parametersFor(Map<String, SqlVariableDetails> selection) {
        List<SqlVariableDetails> result = new ArrayList<>();
        for (SqlVariableDetails parameter : parameters) {
            if (!selection.containsKey(normalize(parameter.getVariableName()))) {
                result.add(parameter);
            }
        }
        result.addAll(selection.values());
        return result;
    }

    String expand(String sql) {
        if (bindings.isEmpty()) {
            return sql;
        }
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            expandSelect(select);
            String result = select.toString();
            // A variable in a projection is a scalar, rather than a filter condition.
            for (Map.Entry<String, Binding> entry : bindings.entrySet()) {
                String token = "'" + entry.getKey() + "'";
                if (result.contains(token)) {
                    Binding binding = entry.getValue();
                    result = result.replace(token, binding.builder().apply(selections(binding.names()).getFirst()));
                }
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to expand SQL variable filter conditions", e);
        }
    }

    private void expandSelect(Select select) throws Exception {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                expandSelect(withItem);
            }
        }
        if (select instanceof ParenthesedSelect parenthesedSelect) {
            expandSelect(parenthesedSelect.getSelect());
        } else if (select instanceof SetOperationList setOperationList) {
            for (Select item : setOperationList.getSelects()) {
                expandSelect(item);
            }
        } else if (select instanceof PlainSelect plainSelect) {
            expandFrom(plainSelect.getFromItem());
            expandJoins(plainSelect.getJoins());
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                expandSubqueries(item.getExpression());
            }
            expandSubqueries(plainSelect.getWhere());
            expandSubqueries(plainSelect.getHaving());
            plainSelect.setWhere(expandCondition(plainSelect.getWhere()));
            plainSelect.setHaving(expandCondition(plainSelect.getHaving()));
        }
    }

    private void expandFrom(FromItem fromItem) throws Exception {
        if (fromItem instanceof ParenthesedSelect select) {
            expandSelect(select);
        } else if (fromItem instanceof ParenthesedFromItem item) {
            expandFrom(item.getFromItem());
            expandJoins(item.getJoins());
        }
    }

    private void expandJoins(List<Join> joins) throws Exception {
        if (joins == null) {
            return;
        }
        for (Join join : joins) {
            expandFrom(join.getRightItem());
            List<Expression> conditions = new ArrayList<>();
            for (Expression expression : join.getOnExpressions()) {
                expandSubqueries(expression);
                conditions.add(expandCondition(expression));
            }
            join.setOnExpressions(conditions);
        }
    }

    private void expandSubqueries(Expression expression) {
        if (expression == null) {
            return;
        }
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Select select) {
                try {
                    expandSelect(select);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    private Expression expandCondition(Expression expression) throws Exception {
        if (expression == null) {
            return null;
        }
        if (expression instanceof AndExpression) {
            BinaryExpression binary = (BinaryExpression) expression;
            binary.setLeftExpression(expandCondition(binary.getLeftExpression()));
            binary.setRightExpression(expandCondition(binary.getRightExpression()));
            return binary;
        }
        if (expression instanceof Parenthesis parenthesis) {
            parenthesis.setExpression(expandCondition(parenthesis.getExpression()));
            return parenthesis;
        }
        Set<String> tokens = new LinkedHashSet<>();
        // Keep OR/NOT groups intact so one component's value is used consistently
        // throughout the condition before combining different components with AND.
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(StringValue value) {
                if (bindings.containsKey(value.getValue())) {
                    tokens.add(value.getValue());
                }
            }

            @Override
            public void visit(Select select) {
                // Its conditions have already been expanded in their own query scope.
            }
        });
        if (tokens.isEmpty()) {
            return expression;
        }
        List<String> names = tokens.stream().flatMap(token -> bindings.get(token).names().stream()).distinct().toList();
        Expression result = null;
        for (Map<String, SqlVariableDetails> selection : selections(names)) {
            String condition = expression.toString();
            for (String token : tokens) {
                condition = condition.replace("'" + token + "'", bindings.get(token).builder().apply(selection));
            }
            Expression item = new Parenthesis(CCJSqlParserUtil.parseCondExpression(condition));
            result = result == null ? item : new AndExpression(result, item);
        }
        return new Parenthesis(result);
    }

    private List<Map<String, SqlVariableDetails>> selections(List<String> names) {
        List<Map<String, SqlVariableDetails>> result = new ArrayList<>();
        result.add(new LinkedHashMap<>());
        for (String name : names) {
            List<Map<String, SqlVariableDetails>> expanded = new ArrayList<>();
            for (Map<String, SqlVariableDetails> selection : result) {
                for (SqlVariableDetails parameter : repeatedParameters.get(name)) {
                    Map<String, SqlVariableDetails> item = new LinkedHashMap<>(selection);
                    item.put(name, parameter);
                    expanded.add(item);
                }
            }
            result = expanded;
        }
        return result;
    }

    static String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private record Binding(List<String> names, Function<Map<String, SqlVariableDetails>, String> builder) {
    }
}
