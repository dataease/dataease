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
    /**
     * 标识符中一旦出现这些字符，就无法通过简单引号包裹安全表达：
     * 控制字符、单/双引号、反引号、方括号、分号、反斜杠。
     * 所有进入引擎库 DDL 的表名、列名、视图名都必须通过该校验并引用。
     */
    private static final Pattern ILLEGAL_IDENTIFIER_CHAR = Pattern.compile("[\\u0000-\\u001f\"'`\\[\\];\\\\]");

    public abstract String createView(String name, String viewSQL);

    public abstract String dropTable(String name);

    public abstract String dropView(String name);

    public abstract String replaceTable(String name);

    public abstract String createTableSql(String name, List<TableField> tableFields, CoreDeEngine engine);

    public abstract String insertSql(String dsType, String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, int page, int pageNumber, List<TableField> tableFields);

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

    /**
     * 校验标识符是否安全，不符合直接拒绝。
     */
    public static void validateIdentifier(String value) {
        String normalized = StringUtils.defaultString(value);
        if (StringUtils.isBlank(normalized) || normalized.length() > 64 || ILLEGAL_IDENTIFIER_CHAR.matcher(normalized).find()) {
            DEException.throwException("Illegal identifier: " + value);
        }
    }

    /**
     * 将标识符按数据库方言引用起来。引用前先做危险字符校验，
     * 因此这里的双写仅作为纵深防御，正常不会触发。
     */
    protected static String quoteIdent(String raw, char quoteChar) {
        validateIdentifier(raw);
        String quote = String.valueOf(quoteChar);
        return quote + raw.replace(quote, quote + quote) + quote;
    }

    /**
     * 引擎库 DDL/DML 只能执行单条语句。允许末尾带一个分号，
     * 字符串字面量内的分号不会被误判。
     */
    public static void validateSingleStatement(String sql) {
        if (StringUtils.isBlank(sql)) {
            return;
        }
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (StringUtils.isEmpty(trimmed)) {
            return;
        }
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (inSingleQuote) {
                if (c == '\\') {
                    i++;
                    continue;
                }
                if (c == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                if (c == '\\') {
                    i++;
                    continue;
                }
                if (c == '"') {
                    inDoubleQuote = false;
                }
                continue;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }
            if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == '`') {
                inBacktick = true;
            } else if (c == ';') {
                DEException.throwException("Multiple statements are not allowed");
            }
        }
    }
}
