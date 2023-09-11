package io.dataease.plugins.datasource.kingbase.query;


import io.dataease.plugins.common.constants.datasource.SQLConstants;


public class KingbaseConstants extends SQLConstants {

    public static final String KEYWORD_TABLE = "%s";

    public static final String KEYWORD_FIX = "%s." + "%s";

    public static final String ALIAS_FIX = "%s";

    public static final String UNIX_TIMESTAMP = "floor(extract(epoch from(( %s - timestamp '1970-01-01 00:00:00')" +
            "*1000))) ";
    public static final String DATE_FORMAT = "to_char(%s, '%s')";
    public static final String STR_TO_DATE = "to_timestamp(%s, '%s')";
    public static final String FROM_UNIXTIME = "to_timestamp(%s)";
    public static final String CAST = "CAST(%s AS %s)";
    public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-DD HH24:MI:SS";
    public static final String DEFAULT_INT_FORMAT = "numeric(38,0)";
    public static final String DEFAULT_FLOAT_FORMAT = "numeric(38,8)";
    public static final String WHERE_VALUE_NULL = "(NULL,'')";
    public static final String WHERE_VALUE_VALUE = "'%s'";
    public static final String AGG_COUNT = "COUNT(*)";
    public static final String AGG_FIELD = "%s(%s)";
    public static final String WHERE_BETWEEN = "'%s' AND '%s'";
    public static final String BRACKETS = "(%s)";
    public static final String GROUP_CONCAT = "to_char(listagg(%s,',' ) within GROUP (order by (%s)))";
    public static final String NAME = "pg";
}
