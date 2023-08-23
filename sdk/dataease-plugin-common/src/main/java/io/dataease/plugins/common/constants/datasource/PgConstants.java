package io.dataease.plugins.common.constants.datasource;



import static io.dataease.plugins.common.constants.DatasourceTypes.pg;

public class PgConstants extends SQLConstants {

    public static final String KEYWORD_TABLE = pg.getKeywordPrefix() + "%s" + pg.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + pg.getKeywordPrefix() + "%s" + pg.getKeywordSuffix();

    public static final String ALIAS_FIX = pg.getAliasPrefix() + "%s" + pg.getAliasSuffix();

    public static final String UNIX_TIMESTAMP = "floor(extract(epoch from(( %s - timestamp '1970-01-01 00:00:00')*1000))) ";

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

    public static final String GROUP_CONCAT = "array_to_string(array_agg(%s),',')";

    public static final String NAME = "pg";


}
