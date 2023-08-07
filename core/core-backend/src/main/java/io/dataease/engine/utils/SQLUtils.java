package io.dataease.engine.utils;

/**
 * @Author Junjun
 */
public class SQLUtils {
    public static String transKeyword(String value) {
        return value.replaceAll("'", "\\\\'");
    }

    public static String buildOriginPreviewSql(String sql) {
        return "SELECT * FROM (" + sql + ") tmp LIMIT 100 OFFSET 0";
    }
}
