package io.dataease.plugins.common.constants.datasource;


import static io.dataease.plugins.common.constants.DatasourceTypes.impala;

public class ImpalaConstants extends SQLConstants {

    public static final String KEYWORD_TABLE = impala.getKeywordPrefix() + "%s" + impala.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + impala.getKeywordPrefix() + "%s" + impala.getKeywordSuffix();

    public static final String ALIAS_FIX = impala.getAliasPrefix() + "%s" + impala.getAliasSuffix();

    public static final String UNIX_TIMESTAMP = "unix_timestamp(%s)";

    public static final String DATE_FORMAT = "from_unixtime(UNIX_TIMESTAMP(%s), '%s')";

    public static final String STR_TO_DATE = "from_unixtime(UNIX_TIMESTAMP(%s, '%s'), '%s')";

    public static final String FROM_UNIXTIME = "FROM_UNIXTIME(%s,'%s')";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_INT_FORMAT = "BIGINT";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,8)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String WHERE_NUMBER_VALUE_VALUE = "%s";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";

    public static final String GROUP_CONCAT = "group_concat(%s, ',')";

    public static final String NAME = "impala";

}

