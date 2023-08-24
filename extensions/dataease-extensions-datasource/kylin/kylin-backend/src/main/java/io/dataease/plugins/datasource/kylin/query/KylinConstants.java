package io.dataease.plugins.datasource.kylin.query;

import io.dataease.plugins.common.constants.datasource.SQLConstants;


public class KylinConstants extends SQLConstants {

    public static final String KEYWORD_TABLE =  "%s" ;

    public static final String KEYWORD_FIX = "%s." + "%s";


    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DATE_TYPE = "TIMESTAMP";

    public static final String DEFAULT_INT_FORMAT = "BIGINT";

    public static final String DEFAULT_FLOAT_FORMAT = "DOUBLE";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";

    public static final String NAME = "pg";


}
