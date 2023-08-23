package io.dataease.plugins.common.constants.datasource;

import static io.dataease.plugins.common.constants.DatasourceTypes.hive;

public class HiveConstants extends SQLConstants {
    public static final String KEYWORD_TABLE = hive.getKeywordPrefix() + "%s" + hive.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + hive.getKeywordPrefix() + "%s" + hive.getKeywordSuffix();

    public static final String ALIAS_FIX = hive.getAliasPrefix() + "%s" + hive.getAliasSuffix();

    public static final String UNIX_TIMESTAMP = "unix_timestamp(%s)";

    public static final String DATE_FORMAT = "DATE_FORMAT(%s,'%s')";

    public static final String FROM_UNIXTIME = "FROM_UNIXTIME(%s,'%s')";

    public static final String STR_TO_DATE = "STR_TO_DATE(%s,'%s')";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_INT_FORMAT = "DECIMAL(20,0)";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,8)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";

    public static final String GROUP_CONCAT = "concat_ws(',' ,collect_list(%s))";

    public static final String NAME = "hive";


}
