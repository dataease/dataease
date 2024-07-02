package io.dataease.extensions.datasource.constant;

/**
 * @Author Junjun
 */
public class SqlPlaceholderConstants {
    public static final String TABLE_PLACEHOLDER = "SELECT * FROM DE_PLACEHOLDER_TABLE_0";

    public static final String KEYWORD_PREFIX_REGEX = "[`'\"\\[]?";

    public static final String KEYWORD_SUFFIX_REGEX = "[`'\"\\]]?";

    public static final String TABLE_PLACEHOLDER_REGEX = "SELECT \\* FROM " + KEYWORD_PREFIX_REGEX + "DE_PLACEHOLDER_TABLE_0" + KEYWORD_SUFFIX_REGEX;

    public static final String CALC_FIELD_PLACEHOLDER = "DE_CALC_FIELD_PLACEHOLDER_%s";
}
