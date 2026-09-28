package io.dataease.engine.utils;

import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @Author Junjun
 */
public class SQLUtils {
    public static String transKeyword(String value) {
        return Optional.ofNullable(value).orElse("").replaceAll("'", "''").replaceAll("\\\\","\\\\\\\\").replace("\n", "\\n");
    }

    /**
     * 在将调用方 SQL 包装进外层 SELECT 前，fail-closed 地校验其恰好为一条只读 SELECT。
     * 拒绝批处理/多语句、DML/DDL 等带副作用的语句；合法查询（含 UNION、CTE、ORDER BY、? 预编译占位）不受影响。
     *
     * @param sql 待校验的调用方 SQL（可能包含已绑定的 ? 占位符）
     */
    public static void validateSingleReadOnlySelect(String sql) {
        if (StringUtils.isBlank(sql)) {
            return;
        }
        String normalized = sql.replace("?", "1");
        try {
            SqlNode node = SqlParser.create(normalized, SqlParser.Config.DEFAULT.withLex(Lex.JAVA)).parseStmt();
            if (!isReadOnlyQuery(node)) {
                DEException.throwException(Translator.get("i18n_sql_only_select"));
            }
        } catch (SqlParseException e) {
            DEException.throwException(Translator.get("i18n_sql_only_select"));
        }
    }

    private static boolean isReadOnlyQuery(SqlNode node) {
        if (node == null) {
            return false;
        }
        if (node instanceof SqlSelect) {
            return true;
        }
        if (node instanceof SqlOrderBy) {
            SqlNode operand = ((SqlOrderBy) node).getOperandList().get(0);
            return isReadOnlyQuery(operand);
        }
        if (node instanceof SqlWith) {
            SqlNode query = ((SqlWith) node).getOperandList().get(1);
            return isReadOnlyQuery(query);
        }
        if (node instanceof SqlBasicCall) {
            SqlKind kind = node.getKind();
            if (kind == SqlKind.UNION || kind == SqlKind.INTERSECT || kind == SqlKind.EXCEPT) {
                for (SqlNode operand : ((SqlBasicCall) node).getOperandList()) {
                    if (!isReadOnlyQuery(operand)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static String buildOriginPreviewSql(String sql, int limit, int offset) {
        return "SELECT * FROM (" + sql + ") tmp LIMIT " + limit + " OFFSET " + offset;
    }

    public static String buildOriginPreviewSqlWithOrderBy(String sql, int limit, int offset, String orderBy) {
        return "SELECT * FROM (" + sql + ") tmp ORDER BY " + orderBy + " LIMIT " + limit + " OFFSET " + offset;
    }
}
