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

    public static String buildOriginPreviewSql(String sql, int limit, int offset) {
        return "SELECT * FROM (" + sql + ") tmp LIMIT " + limit + " OFFSET " + offset;
    }

    public static String buildOriginPreviewSqlWithOrderBy(String sql, int limit, int offset, String orderBy) {
        return "SELECT * FROM (" + sql + ") tmp ORDER BY " + orderBy + " LIMIT " + limit + " OFFSET " + offset;
    }
}
