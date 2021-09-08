package io.dataease.provider.es;

import io.dataease.provider.SQLConstants;

import static io.dataease.datasource.constants.DatasourceTypes.es;

/**
 * @Author gin
 * @Date 2021/7/8 7:22 下午
 */
public class EsSqlLConstants extends SQLConstants {
    public static final String KEYWORD_TABLE = es.getKeywordPrefix() + "%s" + es.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + es.getKeywordPrefix() + "%s" + es.getKeywordSuffix();

    public static final String UNIX_TIMESTAMP = "UNIX_TIMESTAMP(%s)";

    public static final String DATETIME_FORMAT = "DATETIME_FORMAT(%s,'%s')";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-dd HH:mm:ss";

    public static final String DEFAULT_INT_FORMAT = "DECIMAL(20,0)";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,2)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";
}
