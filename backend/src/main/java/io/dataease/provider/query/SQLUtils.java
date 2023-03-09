package io.dataease.provider.query;

/**
 * @Author Junjun
 */
public class SQLUtils {
    public static String transKeyword(String value) {
        return value.replaceAll("'", "\\\\'");
    }
}
