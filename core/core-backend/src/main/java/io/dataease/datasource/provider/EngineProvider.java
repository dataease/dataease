package io.dataease.datasource.provider;

import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.request.EngineRequest;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.TableField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

import static io.dataease.engine.utils.Utils.SQL_INJECTION_PATTERNS;

/**
 * @Author gin
 * @Date 2021/5/17 4:19 下午
 */
public abstract class EngineProvider {
    private static final Pattern ILLEGAL_IDENTIFIER_CHAR = Pattern.compile("[\\u0000-\\u001f\"'`\\[\\];\\\\]");
    private static final int MAX_IDENTIFIER_LENGTH = 64;

    protected static void validateIdentifier(String raw) {
        String value = StringUtils.defaultString(raw);
        if (StringUtils.isBlank(value) || value.length() > MAX_IDENTIFIER_LENGTH
                || ILLEGAL_IDENTIFIER_CHAR.matcher(value).find()) {
            DEException.throwException("Illegal identifier: " + raw);
        }
    }

    protected static String quoteIdentifier(String raw, char quoteChar) {
        validateIdentifier(raw);
        String quote = String.valueOf(quoteChar);
        return quote + StringUtils.defaultString(raw) + quote;
    }

    public abstract String createView(String name, String viewSQL);

    public abstract String dropTable(String name, CoreDeEngine engine);

    public abstract boolean needCheckExistTable();

    public abstract String dropView(String name);

    public abstract String replaceTable(String name, CoreDeEngine engine);

    public abstract String createTableSql(String name, List<TableField> tableFields, CoreDeEngine engine);

    public abstract String insertSql(String dsType, String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, int page, int pageNumber, List<TableField> tableFields, CoreDeEngine engine);

    public static void validateSqlInjectionRisk(String value) {
        String normalized = StringUtils.defaultString(value);
        if (StringUtils.isEmpty(normalized)) {
            return;
        }
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                DEException.throwException("Illegal table name");
            }
        }
    }
}
