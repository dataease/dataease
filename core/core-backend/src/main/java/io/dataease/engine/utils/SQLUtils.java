package io.dataease.engine.utils;

/**
 * @Author Junjun
 */
public class SQLUtils {
    public static String transKeyword(String value) {
        return value.replaceAll("'", "\\\\'");
    }
}
