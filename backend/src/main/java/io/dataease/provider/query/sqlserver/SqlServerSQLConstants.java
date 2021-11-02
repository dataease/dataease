package io.dataease.provider.query.sqlserver;

import io.dataease.provider.query.SQLConstants;

import static io.dataease.commons.constants.DatasourceTypes.sqlServer;

/**
 * @Author gin
 * @Date 2021/7/8 7:22 下午
 */
public class SqlServerSQLConstants extends SQLConstants {
    public static final String KEYWORD_TABLE = sqlServer.getKeywordPrefix() + "%s" + sqlServer.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + sqlServer.getKeywordPrefix() + "%s" + sqlServer.getKeywordSuffix();

    public static final String UNIX_TIMESTAMP = "CAST(DATEDIFF(ss,'1970-01-01 08:00:00', %s) as bigint ) * 1000 ";

    public static final String DATE_FORMAT = "CONVERT(varchar(100), %s, %s)";

    public static final String FROM_UNIXTIME = "convert(varchar, %s ,120)";

    public static final String CONVERT = "CONVERT(%s, %s)";

    public static final String LONG_TO_DATE = "DATEADD(second,%s,'1970-01-01 08:00:00')";

    public static final String STRING_TO_DATE = "CONVERT(datetime, %s ,120)";

    public static final String DEFAULT_INT_FORMAT = "DECIMAL(20,0)";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,2)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";
}
