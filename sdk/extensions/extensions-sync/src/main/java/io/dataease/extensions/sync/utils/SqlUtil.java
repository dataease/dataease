package io.dataease.extensions.sync.utils;

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
     * 定义一个正则表达式模式，匹配SQL语句的分界符（例如分号）
     */
    private static final Pattern STATEMENT_SEPARATOR = Pattern.compile(";(?=([^']*'[^']*')*[^']*$)(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

    /**
     * 定义一个正则表达式模式，匹配查询语句，包括SELECT和WITH语句
     */
    private static final Pattern SELECT_PATTERN = Pattern.compile("(?i)^\\s*(SELECT|WITH)\\b.*$");

    /**
     * 检查SQL语句列表是否只包含SELECT操作
     */
    public static void checkSql(String sql) throws Exception {
        // 分割SQL语句
        List<String> statements = new ArrayList<>();
        for (String statement : STATEMENT_SEPARATOR.split(sql.replaceAll("\r\n", " ")
                .replaceAll("\n", " "))) {
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
            }
        }
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

        // 识别和处理SQL注释
        StringBuilder result = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = (i < sql.length() - 1) ? sql.charAt(i + 1) : '\0';

            // 处理引号
            if (!inSingleLineComment && !inMultiLineComment) {
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                    result.append(c);
                    continue;
                } else if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                    result.append(c);
                    continue;
                }
            }

            // 处理注释
            if (!inSingleQuote && !inDoubleQuote) {
                // 处理单行注释 --
                if (!inMultiLineComment && c == '-' && next == '-') {
                    inSingleLineComment = true;
                    i++; // 跳过下一个字符
                    continue;
                }

                // 处理单行注释 #
                if (!inMultiLineComment && !inSingleLineComment && c == '#') {
                    // 检查#前后为字符，若是则认为是标识符一部分
                    boolean isIdentifier = (i > 0 && Character.isJavaIdentifierPart(sql.charAt(i - 1)))
                            || (i < sql.length() - 1 && Character.isJavaIdentifierPart(sql.charAt(i + 1)));
                    if (!isIdentifier) {
                        inSingleLineComment = true;
                        continue;
                    }
                }

                // 处理多行注释 /*
                if (!inSingleLineComment && c == '/' && next == '*') {
                    inMultiLineComment = true;
                    i++; // 跳过下一个字符
                    continue;
                }

                // 结束多行注释 */
                if (inMultiLineComment && c == '*' && next == '/') {
                    inMultiLineComment = false;
                    i++; // 跳过下一个字符
                    continue;
                }

                // 单行注释遇到换行结束
                if (inSingleLineComment && (c == '\n' || c == '\r')) {
                    inSingleLineComment = false;
                    // 保留换行符
                    result.append(c);
                    continue;
                }
            }

            // 只添加非注释内容
            if (!inSingleLineComment && !inMultiLineComment) {
                result.append(c);
            }
        }

        return result.toString();
    }

}
