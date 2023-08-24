package io.dataease.plugins.common.constants.datasource;


import static io.dataease.plugins.common.constants.DatasourceTypes.db2;

public class Db2Constants extends SQLConstants {

    public static final String KEYWORD_TABLE = db2.getKeywordPrefix() + "%s" + db2.getKeywordSuffix();

    public static final String KEYWORD_FIX = "%s." + db2.getKeywordPrefix() + "%s" + db2.getKeywordSuffix();

    public static final String ALIAS_FIX = db2.getAliasPrefix() + "%s" + db2.getAliasSuffix();

    public static final String UNIX_TIMESTAMP = "BIGINT(TIMESTAMPDIFF(2,CHAR(%s -TIMESTAMP('1970-01-01 08:00:00'))))";

    public static final String DATE_FORMAT = "TO_CHAR(TIMESTAMP(%s),'%s')";

    public static final String TO_DATE = "to_date(TIMESTAMP(\"%s\"),'%s')";

    public static final String FROM_UNIXTIME = "TO_CHAR(TIMESTAMP('1970-01-01 08:00:00') +(%s)SECONDS, '%s')";

    public static final String FORMAT_TIME = "TO_CHAR(TIMESTAMP(REPLACE(CONCAT('1970-01-01 ',%s),'.',':') ),'%s')";

    public static final String FORMAT_DATE = "TO_CHAR(TIMESTAMP(CONCAT(%s, ' 00:00:00')),'%s') ";

    public static final String STR_TO_DATE = "timestamp(trim(char(%s)))";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-DD HH24:MI:SS";

    public static final String DEFAULT_INT_FORMAT = "BIGINT";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(20,8)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String BRACKETS = "(%s)";

    public static final String NAME = "db2";

    public static final String GROUP_CONCAT = "LISTAGG(%s, ',')";


}
