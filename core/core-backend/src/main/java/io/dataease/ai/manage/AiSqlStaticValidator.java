package io.dataease.ai.manage;

import io.dataease.ai.support.AiErrors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import java.util.*;
import java.util.concurrent.*;

/** Static analysis only: this module has no connection, Provider or SQL execution capability. */
public final class AiSqlStaticValidator implements AutoCloseable {
    public static final String SCOPE = "MySQL only; one SELECT from one exact-case table; explicit columns with optional aliases, WHERE comparisons (=, <>, >, >=, <, <=), AND/OR/NOT, ORDER BY real columns. No comments, functions, star, joins, subqueries, aggregates, LIMIT or other dialects. PASS checks only this subset and authorized references, never database executability or data types.";
    public record Result(String status, String code, String message, String scope) { }
    /** Null means absent table; empty fields mean metadata cannot be unambiguously validated. */
    @FunctionalInterface public interface Metadata { List<String> fields(String exactTable); }
    private final ThreadPoolExecutor parsers = new ThreadPoolExecutor(0, 2, 10, TimeUnit.SECONDS,
            new SynchronousQueue<>(), task -> { var thread = new Thread(task, "ai-sql-static-parser"); thread.setDaemon(true); return thread; },
            new ThreadPoolExecutor.AbortPolicy());
    private static final class Rejected extends RuntimeException {
        final String status;
        final String code;
        Rejected(String status, String code) { this.status = status; this.code = code; }
    }
    private static void unsupported() { throw new Rejected("UNSUPPORTED", "subset"); }
    private static void invalid() { throw new Rejected("INVALID", "reference"); }
    private static Result result(String status, String code) {
        return new Result(status, code, switch (code) {
            case "verified" -> "Supported syntax and authorized table/column references checked. SQL was not executed.";
            case "reference" -> "Unknown exact table, column or table qualifier in authorized metadata. Correct the reference and validate again.";
            case "parse" -> "Cannot parse the whole input within this tool's supported syntax; database syntax was not verified.";
            default -> "Outside the declared static validation subset. SQL was not executed.";
        }, SCOPE);
    }
    public Result validate(String sql, String dialect, Metadata metadata, Runnable active) {
        return validate(sql, dialect, metadata, active, System.currentTimeMillis() + 750);
    }
    public Result validate(String sql, String dialect, Metadata metadata, Runnable active, long deadline) {
        Runnable check = () -> {
            active.run();
            if (Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
            if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
        };
        check.run();
        if (!"mysql".equalsIgnoreCase(dialect)) return result("UNSUPPORTED", "dialect");
        CCJSqlParser parser = null;
        try {
            lex(sql, check);
            parser = CCJSqlParserUtil.newParser(sql);
            parser.setErrorRecovery(false);
            parser.withUnsupportedStatements(false).withAllowComplexParsing(false).withTimeOut(Math.max(1, Math.min(750, deadline - System.currentTimeMillis())));
            var statements = CCJSqlParserUtil.parseStatements(parser, parsers);
            check.run();
            if (statements == null || statements.size() != 1 || statements.get(0) == null
                    || statements.get(0).getClass() != PlainSelect.class) unsupported();
            var select = (PlainSelect) statements.get(0);
            shape(select);
            var table = (Table) select.getFromItem();
            String tableName = tableName(table);
            String qualifier = table.getAlias() == null ? tableName : alias(table.getAlias());
            var columns = metadata.fields(tableName);
            check.run();
            if (columns == null) invalid();
            if (columns.isEmpty() || columns.size() > 256 || columns.stream().map(value -> value.toLowerCase(Locale.ROOT)).distinct().count() != columns.size()) unsupported();
            var references = new References(columns, qualifier, check);
            var aliases = new HashSet<String>();
            for (var item : select.getSelectItems()) {
                if (item.getClass() != net.sf.jsqlparser.statement.select.SelectItem.class) unsupported();
                if (item.getAlias() != null && !aliases.add(alias(item.getAlias()))) unsupported();
                references.column(item.getExpression());
            }
            if (select.getWhere() != null) references.condition(select.getWhere(), 0);
            if (select.getOrderByElements() != null) for (var order : select.getOrderByElements()) {
                if (order.getClass() != net.sf.jsqlparser.statement.select.OrderByElement.class
                        || order.getNullOrdering() != null || order.isMysqlWithRollup()) unsupported();
                if (order.getExpression().getClass() == Column.class) {
                    var orderColumn = (Column) order.getExpression();
                    String name = identifier(orderColumn.getColumnName());
                    if (!columns.contains(name) && aliases.contains(name)) unsupported();
                }
                references.column(order.getExpression());
            }
            check.run();
            return result("PASS", "verified");
        } catch (Rejected rejected) { return result(rejected.status, rejected.code); }
        catch (JSQLParserException failure) {
            check.run();
            if (Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
            // Parser timeout is an execution failure, never a statement/reference verdict.
            for (Throwable cause = failure; cause != null; cause = cause.getCause())
                if (cause instanceof TimeoutException) throw AiErrors.error("timeout");
            return result("UNSUPPORTED", "parse");
        } catch (RejectedExecutionException failure) { throw AiErrors.error("concurrency"); }
        finally { if (parser != null) parser.interrupted = true; }
    }
    private static void shape(PlainSelect s) {
        if (s.getWithItemsList() != null || s.getForClause() != null || s.getIsolation() != null || s.isOracleSiblings()
                || s.getLimitBy() != null || s.getLimit() != null || s.getOffset() != null || s.getFetch() != null
                || s.getIntoTables() != null || s.getIntoTempTable() != null || s.isUseWithNoLog()
                || s.getForMode() != null || s.getForUpdateTable() != null || s.isSkipLocked() || s.getWait() != null || s.isNoWait()
                || s.getJoins() != null || s.getLateralViews() != null || s.getGroupBy() != null || s.getHaving() != null
                || s.getQualify() != null || s.getWindowDefinitions() != null || s.getKsqlWindow() != null || s.getOracleHierarchical() != null
                || s.getTop() != null || s.getFirst() != null || s.getSkip() != null || s.getOptimizeFor() != null || s.getDistinct() != null
                || s.getOracleHint() != null || s.getMySqlHintStraightJoin() || s.getMySqlSqlCalcFoundRows() || s.getMySqlSqlCacheFlag() != null
                || s.getForXmlPath() != null || s.isEmitChanges() || s.isUsingFinal() || s.isUsingOnly()
                || s.getFromItem() == null || s.getFromItem().getClass() != Table.class
                || s.getSelectItems() == null || s.getSelectItems().isEmpty() || s.getSelectItems().size() > 128) unsupported();
    }
    private static String identifier(String raw) {
        if (raw == null) { unsupported(); return null; }
        if (raw.matches("`[A-Za-z_][A-Za-z0-9_]*`")) return raw.substring(1, raw.length() - 1);
        if (!raw.matches("[A-Za-z_][A-Za-z0-9_]*")) unsupported();
        return raw;
    }
    private static String alias(Alias alias) {
        if (alias.getClass() != Alias.class || alias.getAliasColumns() != null) unsupported();
        return identifier(alias.getName());
    }
    private static String tableName(Table table) {
        if (table.getNameParts() == null || table.getNameParts().size() != 1
                || table.getPivot() != null || table.getUnPivot() != null || table.getSampleClause() != null
                || table.getIndexHint() != null || table.getSqlServerHints() != null) unsupported();
        return identifier(table.getNameParts().get(0));
    }
    private static final class References {
        final List<String> columns;
        final String qualifier;
        final Runnable active;
        int nodes;
        References(List<String> columns, String qualifier, Runnable active) { this.columns = columns; this.qualifier = qualifier; this.active = active; }
        void count(int depth) { active.run(); if (++nodes > 512 || depth > 24) unsupported(); }
        void column(Expression expression) {
            count(0);
            if (expression == null || expression.getClass() != Column.class) unsupported();
            var column = (Column) expression;
            if (column.getArrayConstructor() != null || column.getCommentText() != null) unsupported();
            if (column.getTable() != null) {
                var table = column.getTable();
                if (table.getNameParts() != null && !table.getNameParts().isEmpty()) {
                    String actual = tableName(table);
                    if (table.getAlias() != null) unsupported();
                    if (!qualifier.equals(actual)) {
                        if (qualifier.equalsIgnoreCase(actual)) unsupported();
                        invalid();
                    }
                }
            }
            String name = identifier(column.getColumnName());
            if (!columns.contains(name)) {
                if (columns.stream().anyMatch(value -> value.equalsIgnoreCase(name))) unsupported();
                invalid();
            }
        }
        void value(Expression expression, int depth) {
            count(depth);
            if (expression == null) unsupported();
            if (expression.getClass() == Column.class) column(expression);
            else if (expression.getClass() == StringValue.class) { if (((StringValue) expression).getPrefix() != null) unsupported(); }
            else if (expression.getClass() == SignedExpression.class) {
                var signed = (SignedExpression) expression;
                if ((signed.getSign() != '+' && signed.getSign() != '-') || (signed.getExpression().getClass() != LongValue.class
                        && signed.getExpression().getClass() != DoubleValue.class)) unsupported();
            } else if (expression.getClass() != LongValue.class && expression.getClass() != DoubleValue.class && expression.getClass() != NullValue.class) unsupported();
        }
        void condition(Expression expression, int depth) {
            count(depth);
            Class<?> type = expression.getClass();
            if (type == Parenthesis.class) condition(((Parenthesis) expression).getExpression(), depth + 1);
            else if (type == NotExpression.class) {
                var not = (NotExpression) expression;
                if (not.isExclamationMark()) unsupported();
                condition(not.getExpression(), depth + 1);
            } else if (type == AndExpression.class || type == OrExpression.class) {
                var binary = (BinaryExpression) expression;
                if (type == AndExpression.class && ((AndExpression) expression).isUseOperator()) unsupported();
                condition(binary.getLeftExpression(), depth + 1); condition(binary.getRightExpression(), depth + 1);
            } else if (Set.of(EqualsTo.class, NotEqualsTo.class, GreaterThan.class, GreaterThanEquals.class, MinorThan.class, MinorThanEquals.class).contains(type)) {
                var comparison = (ComparisonOperator) expression;
                if (comparison.getOldOracleJoinSyntax() != 0 || comparison.getOraclePriorPosition() != 0) unsupported();
                value(comparison.getLeftExpression(), depth + 1); value(comparison.getRightExpression(), depth + 1);
            } else unsupported();
        }
    }
    /** Reject comments/ambiguous quoting before AST loses them. Quoted contents never count as SQL tokens. */
    private static void lex(String sql, Runnable active) {
        if (sql == null || sql.isBlank() || sql.length() > 8192) unsupported();
        int depth = 0, tokens = 0;
        for (int i = 0; i < sql.length(); i++) {
            active.run();
            char c = sql.charAt(i);
            if (c == '\'' || c == '`') {
                char quote = c; boolean closed = false;
                for (i++; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if (c == '\\' || c < 32) unsupported();
                    if (c == quote) {
                        if (quote == '\'' && i + 1 < sql.length() && sql.charAt(i + 1) == quote) { i++; continue; }
                        closed = true; break;
                    }
                }
                if (!closed) unsupported();
                tokens++;
            } else {
                if (c == '#' || (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*')
                        || (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-')) unsupported();
                if (!(c <= 127 && (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || "_.,;()=<>!+-*/".indexOf(c) >= 0))) unsupported();
                if (c == '(' && ++depth > 24) unsupported();
                if (c == ')' && --depth < 0) unsupported();
                if (!Character.isWhitespace(c)) tokens++;
            }
            if (tokens > 4096) unsupported();
        }
        if (depth != 0) unsupported();
    }
    @Override public void close() { parsers.shutdownNow(); }
}
