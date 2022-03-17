package io.dataease.provider.query.impala;

import io.dataease.provider.SQLConstants;

import static io.dataease.commons.constants.DatasourceTypes.mysql;

public class ImpalaConstants extends SQLConstants {
    public static final String KEYWORD_TABLE = mysql.getKeywordPrefix() + "%s" + mysql.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + mysql.getKeywordPrefix() + "%s" + mysql.getKeywordSuffix();

    public static final String UNIX_TIMESTAMP = "unix_timestamp(%s)";

    public static final String DATE_FORMAT = "from_unixtime(UNIX_TIMESTAMP(%s), '%s')";

    public static final String FROM_UNIXTIME = "FROM_UNIXTIME(%s,'%s')";

    public static final String STR_TO_DATE = "STR_TO_DATE(%s,'%s')";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_INT_FORMAT = "BIGINT";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,2)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";
}
