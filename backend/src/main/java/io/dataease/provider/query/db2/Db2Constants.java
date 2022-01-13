package io.dataease.provider.query.db2;

import io.dataease.provider.query.SQLConstants;

import static io.dataease.commons.constants.DatasourceTypes.db2;

public class Db2Constants extends SQLConstants {
    public static final String KEYWORD_TABLE = db2.getKeywordPrefix() + "%s" + db2.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + db2.getKeywordPrefix() + "%s" + db2.getKeywordSuffix();

    public static final String UNIX_TIMESTAMP = "BIGINT(TIMESTAMPDIFF(2,CHAR(%s -TIMESTAMP('1970-01-01 08:00:00'))))";

    public static final String DATE_FORMAT = "TO_CHAR(TIMESTAMP(%s),'%s')";

    public static final String FROM_UNIXTIME = "TO_CHAR(TIMESTAMP('1970-01-01 08:00:00') +(%s)SECONDS, '%s')";

    public static final String STR_TO_DATE = "timestamp(trim(char(%s)))";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-DD HH24:MI:SS";

    public static final String DEFAULT_INT_FORMAT = "BIGINT";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,2)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";
}
