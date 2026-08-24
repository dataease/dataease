package io.dataease.listener.sql;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DemoDataInitUtils {

    private static final DateTimeFormatter DEMO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DemoDataInitUtils() {
    }

    static List<DemoInsertStatement> loadInsertStatements(String sqlResourcePath) {
        List<DemoInsertStatement> statements = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(sqlResourcePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String sql = line.trim();
                if (sql.isEmpty() || sql.startsWith("--")) {
                    continue;
                }
                if (sql.startsWith("INSERT INTO")) {
                    statements.add(parseInsertStatement(sql));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load demo init data from " + sqlResourcePath, e);
        }
        return statements;
    }

    static String toText(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    static Long toLong(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    static Integer toInteger(Map<String, Object> values, String key) {
        Long value = toLong(values, key);
        return value == null ? null : value.intValue();
    }

    static Boolean toBoolean(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = value.toString();
        if ("1".equals(text) || "true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("0".equals(text) || "false".equalsIgnoreCase(text)) {
            return false;
        }
        return null;
    }

    static Date toDate(Map<String, Object> values, String key) {
        String value = toText(values, key);
        if (value == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(value, DEMO_DATETIME_FORMATTER);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    static boolean isRootNode(Long pid) {
        return pid == null || pid == 0L;
    }

    private static DemoInsertStatement parseInsertStatement(String sql) {
        int intoStart = "INSERT INTO".length();
        int columnsStart = sql.indexOf('(', intoStart);
        if (columnsStart < 0) {
            throw new IllegalArgumentException("Invalid demo insert sql, columns not found: " + sql);
        }
        String tableName = stripSqlIdentifier(sql.substring(intoStart, columnsStart));
        int columnsEnd = findClosingParenthesis(sql, columnsStart);
        int valuesStart = sql.indexOf("VALUES", columnsEnd);
        if (valuesStart < 0) {
            throw new IllegalArgumentException("Invalid demo insert sql, values not found: " + sql);
        }
        int valuesParenStart = sql.indexOf('(', valuesStart);
        int valuesParenEnd = findClosingParenthesis(sql, valuesParenStart);
        List<String> columns = parseColumns(sql.substring(columnsStart + 1, columnsEnd));
        List<Object> values = parseValues(sql.substring(valuesParenStart + 1, valuesParenEnd));
        if (columns.size() != values.size()) {
            throw new IllegalArgumentException("Invalid demo insert sql, column/value size mismatch: " + tableName);
        }
        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            valueMap.put(columns.get(i), values.get(i));
        }
        return new DemoInsertStatement(tableName, valueMap);
    }

    private static List<String> parseColumns(String text) {
        List<String> columns = new ArrayList<>();
        for (String column : text.split(",")) {
            columns.add(stripSqlIdentifier(column));
        }
        return columns;
    }

    private static List<Object> parseValues(String text) {
        List<Object> values = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            index = skipWhitespace(text, index);
            if (index >= text.length()) {
                break;
            }

            char current = text.charAt(index);
            Object value;
            if (startsWithIgnoreCase(text, index, "NULL")) {
                value = null;
                index += "NULL".length();
            } else if (isBitLiteral(text, index)) {
                value = text.charAt(index + 2) == '1';
                index += 4;
            } else if (current == '\'') {
                ParsedString parsedString = parseQuotedSqlString(text, index);
                value = parsedString.getValue();
                index = parsedString.getNextIndex();
            } else {
                int start = index;
                while (index < text.length() && text.charAt(index) != ',') {
                    index++;
                }
                value = parseUnquotedValue(text.substring(start, index).trim());
            }

            values.add(value);
            index = skipWhitespace(text, index);
            if (index < text.length() && text.charAt(index) == ',') {
                index++;
            }
        }
        return values;
    }

    private static ParsedString parseQuotedSqlString(String text, int startIndex) {
        StringBuilder value = new StringBuilder();
        int index = startIndex + 1;
        while (index < text.length()) {
            char current = text.charAt(index);
            if (current == '\\' && index + 1 < text.length()) {
                value.append(unescapeSqlChar(text.charAt(index + 1)));
                index += 2;
                continue;
            }
            if (current == '\'') {
                if (index + 1 < text.length() && text.charAt(index + 1) == '\'') {
                    value.append('\'');
                    index += 2;
                    continue;
                }
                return new ParsedString(value.toString(), index + 1);
            }
            value.append(current);
            index++;
        }
        throw new IllegalArgumentException("Unclosed sql string value");
    }

    private static char unescapeSqlChar(char value) {
        return switch (value) {
            case '0' -> '\0';
            case 'b' -> '\b';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'Z' -> (char) 26;
            default -> value;
        };
    }

    private static Object parseUnquotedValue(String text) {
        if (text.isEmpty()) {
            return "";
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ignored) {
            return text;
        }
    }

    private static boolean isBitLiteral(String text, int index) {
        return index + 3 < text.length()
                && (text.charAt(index) == 'b' || text.charAt(index) == 'B')
                && text.charAt(index + 1) == '\''
                && (text.charAt(index + 2) == '0' || text.charAt(index + 2) == '1')
                && text.charAt(index + 3) == '\'';
    }

    private static int findClosingParenthesis(String text, int openIndex) {
        int depth = 0;
        boolean inQuote = false;
        for (int i = openIndex; i < text.length(); i++) {
            char current = text.charAt(i);
            if (inQuote) {
                if (current == '\\') {
                    i++;
                } else if (current == '\'') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        inQuote = false;
                    }
                }
                continue;
            }
            if (current == '\'') {
                inQuote = true;
            } else if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Closing parenthesis not found");
    }

    private static int skipWhitespace(String text, int index) {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static boolean startsWithIgnoreCase(String text, int index, String prefix) {
        return text.regionMatches(true, index, prefix, 0, prefix.length());
    }

    private static String stripSqlIdentifier(String value) {
        String result = value.trim();
        if ((result.startsWith("`") && result.endsWith("`"))
                || (result.startsWith("\"") && result.endsWith("\""))) {
            return result.substring(1, result.length() - 1);
        }
        return result;
    }

    static class DemoInsertStatement {
        private final String tableName;
        private final Map<String, Object> values;

        DemoInsertStatement(String tableName, Map<String, Object> values) {
            this.tableName = tableName;
            this.values = values;
        }

        String getTableName() {
            return tableName;
        }

        Map<String, Object> getValues() {
            return values;
        }
    }

    private static class ParsedString {
        private final String value;
        private final int nextIndex;

        private ParsedString(String value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }

        private String getValue() {
            return value;
        }

        private int getNextIndex() {
            return nextIndex;
        }
    }
}
