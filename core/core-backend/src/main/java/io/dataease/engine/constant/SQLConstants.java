package io.dataease.engine.constant;

import java.util.ArrayList;
import java.util.List;

public class SQLConstants {
    /**
     * 维度类型list
     */
    public static final List<Integer> DIMENSION_TYPE = new ArrayList<Integer>() {{
        add(0);// 文本
        add(1);// 时间
        add(5);// 地理位置
    }};

    /**
     * 指标类型list
     */
    public static final List<Integer> QUOTA_TYPE = new ArrayList<Integer>() {{
        add(2);// 整型
        add(3);// 浮点
        add(4);// 布尔
    }};

    /**
     * sql ST模板
     */
    public static final String SQL_TEMPLATE = "sqlTemplate.stg";

    public static final String SCHEMA = "s_a_%s";

    public static final String TABLE_ALIAS_PREFIX = "t_a_%s";

    public static final String FIELD_ALIAS_X_PREFIX = "f_ax_%s";

    public static final String FIELD_ALIAS_Y_PREFIX = "f_ay_%s";

    public static final String GROUP_ALIAS_PREFIX = "g_a_%s";

    public static final String ORDER_ALIAS_X_PREFIX = "o_ax_%s";

    public static final String ORDER_ALIAS_Y_PREFIX = "o_ay_%s";

    public static final String WHERE_ALIAS_PREFIX = "w_a_%s";

    public static final String TABLE_NAME = "%s.`%s`";

    public static final String FIELD_NAME = "%s.`%s`";

    public static final String FIELD_DOT = "`%s`";

    public static final String UNIX_TIMESTAMP = "UNIX_TIMESTAMP(%s)";

    public static final String DATE_FORMAT = "DATE_FORMAT(%s,'%s')";

    public static final String DE_DATE_FORMAT = "DE_DATE_FORMAT(%s,'%s')";

    public static final String CAST_DATE_FORMAT = "CAST_DATE_FORMAT(%s,'%s','%s')";

    public static final String DE_CAST_DATE_FORMAT = "DE_CAST_DATE_FORMAT(%s,'%s','%s')";

    public static final String FROM_UNIXTIME = "FROM_UNIXTIME(%s,'%s')";

    public static final String STR_TO_DATE = "STR_TO_DATE(%s,'%s')";

    public static final String DE_STR_TO_DATE = "DE_STR_TO_DATE(%s,'%s')";

    public static final String GET_DATE_FORMAT = "GET_DATE_FORMAT(%s)";

    public static final String CAST = "CAST(%s AS %s)";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_INT_FORMAT = "DECIMAL(18,0)";

    public static final String DEFAULT_FLOAT_FORMAT = "DECIMAL(27,8)";

    public static final String WHERE_VALUE_NULL = "(NULL,'')";

    public static final String WHERE_VALUE_VALUE = "'%s'";

    public static final String WHERE_NUMBER_VALUE = "%s";

    public static final String AGG_COUNT = "COUNT(*)";

    public static final String AGG_FIELD = "%s(%s)";

    public static final String WHERE_BETWEEN = "'%s' AND '%s'";

    public static final String WHERE_VALUE_BETWEEN = "%s AND %s";

    public static final String BRACKETS = "(%s)";

    public static final String ROUND = "ROUND(%s,%s)";

    public static final String VARCHAR = "VARCHAR";

    public static final String NAME = "engine_mysql";

    public static final String GROUP_CONCAT = "GROUP_CONCAT(%s)";

    public static final String QUARTER = "QUARTER(%s)";
}
