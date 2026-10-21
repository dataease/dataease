package io.dataease.extensions.datasource.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * sql操作工具类
 *
 * @author fit2cloud
 */
public class SqlUtil {

    /**
     * 定义一个正则表达式模式，匹配查询语句，包括SELECT和WITH语句
     */
    private static final Pattern SELECT_PATTERN = Pattern.compile("(?i)^\\s*(SELECT|WITH)\\b.*$");

    /**
     * 匹配写操作关键字。用于拦截 Data-modifying CTE（如 WITH ... DELETE/UPDATE/INSERT ...）
     */
    private static final Pattern WRITE_OPERATION_PATTERN = Pattern.compile(
            "(?i)\\b(INSERT|UPDATE|DELETE|MERGE|TRUNCATE|DROP|ALTER|CREATE|GRANT|REVOKE|CALL|EXEC|EXECUTE|INTO)\\b|\\bREPLACE\\s+INTO\\b");

    /**
     * 检查SQL语句列表是否只包含SELECT操作
     */
    public static void checkSql(String sql) throws Exception {
        // 反斜杠+引号在不同数据库（MySQL 转义 vs PostgreSQL 字面量）中语义不同，
        // 为避免解析歧义被绕过，直接拒绝这类语句（fail-closed）。
        if (containsBackslashQuote(sql)) {
            throw new Exception(sql);
        }
        // 先剔除字符串、注释等非代码片段，避免其中的分号或关键字干扰判断。
        // 必须在换行符归一化前处理行注释，否则 --/# 会吞掉整条后续 SQL。
        String masked = maskSql(sql);
        // 统一换行符，避免多行 SELECT/WITH 无法匹配前缀
        String normalized = masked.replaceAll("[\\r\\n]+", " ");
        // 分割SQL语句
        List<String> statements = new ArrayList<>();
        for (String statement : normalized.split(";")) {
            statements.add(statement.trim());
        }
        // 检查每个SQL语句
        for (String statement : statements) {
            if (!statement.isEmpty()) {
                // 检查是否为查询语句
                if (!SELECT_PATTERN.matcher(statement).matches()) {
                    // 如果不是查询语句，抛出异常
                    throw new Exception(statement);
                }
                // FOR UPDATE / FOR NO KEY UPDATE 属于行级锁读，不是写操作
                String readonlyStatement = statement.replaceAll("(?i)\\bFOR\\s+(?:NO\\s+KEY\\s+)?UPDATE\\b", " ");
                // 拦截 Data-modifying CTE 等隐藏在 WITH/SELECT 后的写操作
                if (WRITE_OPERATION_PATTERN.matcher(readonlyStatement).find()) {
                    throw new Exception(statement);
                }
            }
        }
    }

    /**
     * 将SQL中的字符串、引号标识符、dollar-quoted string 和注释替换为空格，
     * 仅保留可执行代码关键字，供安全检查使用。
     */
    private static String maskSql(String sql) {
        StringBuilder result = new StringBuilder(sql.length());
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);

            int dollarEnd = dollarQuoteEnd(sql, i);
            if (dollarEnd > i) {
                result.append(' ');
                i = dollarEnd;
                continue;
            }

            if (c == '\'' || c == '"' || c == '`') {
                int j = i + 1;
                while (j < sql.length()) {
                    char sc = sql.charAt(j);
                    if (sc == c) {
                        if (j + 1 < sql.length() && sql.charAt(j + 1) == c) {
                            j += 2;
                            continue;
                        }
                        j++;
                        break;
                    }
                    j++;
                }
                result.append(' ');
                i = j;
                continue;
            }

            if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                int j = i + 2;
                while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                    j++;
                }
                i = j;
                continue;
            }

            // 单行注释 #（兼容 MySQL），但保留 PostgreSQL 的 #>/#>>/#- JSON 运算符
            if (c == '#') {
                boolean isIdentifier = (i > 0 && Character.isJavaIdentifierPart(sql.charAt(i - 1)))
                        || (i + 1 < sql.length() && Character.isJavaIdentifierPart(sql.charAt(i + 1)));
                boolean postgresOperator = i + 1 < sql.length()
                        && (sql.charAt(i + 1) == '>' || sql.charAt(i + 1) == '-');
                if (!isIdentifier && !postgresOperator) {
                    int j = i + 1;
                    while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                        j++;
                    }
                    i = j;
                    continue;
                }
            }

            if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end >= 0 ? end + 2 : sql.length();
                continue;
            }

            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * 返回 dollar-quoted string 的结束位置；若不是 dollar quote 则返回 -1。
     */
    private static int dollarQuoteEnd(String sql, int start) {
        if (sql.charAt(start) != '$' || start + 1 >= sql.length()) {
            return -1;
        }
        // 仅在 token 边界识别 dollar quote，避免把 a$$ 这类合法标识符当成字符串起始
        if (start > 0) {
            char prev = sql.charAt(start - 1);
            if (Character.isLetterOrDigit(prev) || prev == '_' || prev == '$') {
                return -1;
            }
        }
        int next = start + 1;
        if (sql.charAt(next) == '$') {
            int end = sql.indexOf("$$", next + 1);
            return end >= 0 ? end + 2 : sql.length();
        }
        if (Character.isLetter(sql.charAt(next)) || sql.charAt(next) == '_') {
            int tagEnd = next;
            while (tagEnd < sql.length()
                    && (Character.isLetterOrDigit(sql.charAt(tagEnd)) || sql.charAt(tagEnd) == '_')) {
                tagEnd++;
            }
            if (tagEnd < sql.length() && sql.charAt(tagEnd) == '$') {
                String tag = sql.substring(start, tagEnd + 1);
                int end = sql.indexOf(tag, tagEnd + 1);
                return end >= 0 ? end + tag.length() : sql.length();
            }
        }
        return -1;
    }

    /**
     * 检测 SQL 中是否包含反斜杠+引号（\' 或 \"）。这类写法在不同数据库方言中
     * 语义不同（MySQL 作为转义，PostgreSQL 默认作为字面量），统一拒绝以避免解析歧义。
     */
    private static boolean containsBackslashQuote(String sql) {
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);

            int dollarEnd = dollarQuoteEnd(sql, i);
            if (dollarEnd > i) {
                i = dollarEnd;
                continue;
            }

            if (c == '\'' || c == '"') {
                int j = i + 1;
                while (j < sql.length()) {
                    char sc = sql.charAt(j);
                    if (sc == '\\' && j + 1 < sql.length() && sql.charAt(j + 1) == c) {
                        int backslashCount = 0;
                        int k = j;
                        while (k >= i + 1 && sql.charAt(k) == '\\') {
                            backslashCount++;
                            k--;
                        }
                        // 仅奇数个反斜杠表示转义引号，偶数个是普通字面量反斜杠
                        if (backslashCount % 2 == 1) {
                            return true;
                        }
                    }
                    if (sc == c) {
                        if (j + 1 < sql.length() && sql.charAt(j + 1) == c) {
                            j += 2;
                            continue;
                        }
                        j++;
                        break;
                    }
                    j++;
                }
                i = j;
                continue;
            }

            if (c == '`') {
                int j = i + 1;
                while (j < sql.length() && sql.charAt(j) != '`') {
                    j++;
                }
                i = j < sql.length() ? j + 1 : j;
                continue;
            }

            if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                int j = i + 2;
                while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                    j++;
                }
                i = j;
                continue;
            }

            if (c == '#' ) {
                boolean isIdentifier = (i > 0 && Character.isJavaIdentifierPart(sql.charAt(i - 1)))
                        || (i + 1 < sql.length() && Character.isJavaIdentifierPart(sql.charAt(i + 1)));
                boolean postgresOperator = i + 1 < sql.length()
                        && (sql.charAt(i + 1) == '>' || sql.charAt(i + 1) == '-');
                if (!isIdentifier && !postgresOperator) {
                    int j = i + 1;
                    while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                        j++;
                    }
                    i = j;
                    continue;
                }
            }

            if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end >= 0 ? end + 2 : sql.length();
                continue;
            }

            i++;
        }
        return false;
    }

    /**
     * 转换值,将值中的双引号转义
     *
     * @param value
     * @return
     */
    public static String transValue(String value) {
        return value.replaceAll("\"", "\\\\\"");
    }

    /**
     * 移除SQL注释
     * 单行的 -- 注释 或者 # 注释
     * 多行的 /* 注释
     * 忽略单引号或双引号中的内容
     *
     * @param sql
     * @return
     */
    public static String removeSqlComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        StringBuilder result = new StringBuilder(sql.length());
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);

            // PostgreSQL dollar-quoted string：整体保留，内部不识别注释
            int dollarEnd = dollarQuoteEnd(sql, i);
            if (dollarEnd > i) {
                result.append(sql, i, dollarEnd);
                i = dollarEnd;
                continue;
            }

            // 单引号字符串：支持 '' 转义
            if (c == '\'') {
                int j = i + 1;
                while (j < sql.length()) {
                    char sc = sql.charAt(j);
                    if (sc == '\'') {
                        if (j + 1 < sql.length() && sql.charAt(j + 1) == '\'') {
                            j += 2;
                            continue;
                        }
                        j++;
                        break;
                    }
                    j++;
                }
                result.append(sql, i, j);
                i = j;
                continue;
            }

            // 双引号标识符/字符串：支持 "" 转义
            if (c == '"') {
                int j = i + 1;
                while (j < sql.length()) {
                    char sc = sql.charAt(j);
                    if (sc == '"') {
                        if (j + 1 < sql.length() && sql.charAt(j + 1) == '"') {
                            j += 2;
                            continue;
                        }
                        j++;
                        break;
                    }
                    j++;
                }
                result.append(sql, i, j);
                i = j;
                continue;
            }

            // 反引号标识符
            if (c == '`') {
                int j = i + 1;
                while (j < sql.length() && sql.charAt(j) != '`') {
                    j++;
                }
                if (j < sql.length()) {
                    j++;
                }
                result.append(sql, i, j);
                i = j;
                continue;
            }

            // 单行注释 --
            if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                int j = i + 2;
                while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                    j++;
                }
                if (j < sql.length()) {
                    result.append(sql.charAt(j));
                    j++;
                }
                i = j;
                continue;
            }

            // 单行注释 #（兼容 MySQL），但保留 PostgreSQL 的 #>/#>>/#- JSON 运算符
            if (c == '#') {
                boolean isIdentifier = (i > 0 && Character.isJavaIdentifierPart(sql.charAt(i - 1)))
                        || (i + 1 < sql.length() && Character.isJavaIdentifierPart(sql.charAt(i + 1)));
                boolean postgresOperator = i + 1 < sql.length()
                        && (sql.charAt(i + 1) == '>' || sql.charAt(i + 1) == '-');
                if (!isIdentifier && !postgresOperator) {
                    int j = i + 1;
                    while (j < sql.length() && sql.charAt(j) != '\n' && sql.charAt(j) != '\r') {
                        j++;
                    }
                    if (j < sql.length()) {
                        result.append(sql.charAt(j));
                        j++;
                    }
                    i = j;
                    continue;
                }
            }

            // 多行注释
            if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end >= 0 ? end + 2 : sql.length();
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

}
