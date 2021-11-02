package io.dataease.provider.query.redshift;

import io.dataease.provider.query.SQLConstants;

import static io.dataease.commons.constants.DatasourceTypes.pg;

/**
 * Redshift 静态变量
 *
 * @className: RedshiftConstants
 * @description: Redshift 静态变量
 * @author: Jiantao Yan
 * @date: 2021/10/11 17:12
 **/
public class RedshiftConstants extends SQLConstants {
    public static final String KEYWORD_TABLE = pg.getKeywordPrefix() + "%s" + pg.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + pg.getKeywordPrefix() + "%s" + pg.getKeywordSuffix();

    public static final String UNIX_TIMESTAMP = "floor(extract(epoch from(( %s - timestamp '1970-01-01 00:00:00')*1000))) ";

    public static final String DATE_FORMAT = "to_char(%s, %s)";

    public static final String FROM_UNIXTIME = "to_timestamp(%s)";

    public static final String TO_DATE = "to_date(%s,'%s')";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "'YYYY-MM-DD HH24:MI:SS'";

    public static final String DEFAULT_INT_FORMAT = "numeric(18,0)";

    public static final String DEFAULT_FLOAT_FORMAT = "numeric(18,2)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";


}
