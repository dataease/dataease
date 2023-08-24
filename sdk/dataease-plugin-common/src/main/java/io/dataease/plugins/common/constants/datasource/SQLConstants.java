package io.dataease.plugins.common.constants.datasource;

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
    public static final String SQL_TEMPLATE = "pluginSqltemplate.stg";

    public static final String TABLE_ALIAS_PREFIX = "t_a_%s";
    public static final String FIELD_ALIAS_X_PREFIX = "f_ax_%s";
    public static final String FIELD_ALIAS_Y_PREFIX = "f_ay_%s";
    public static final String GROUP_ALIAS_PREFIX = "g_a_%s";
    public static final String ORDER_ALIAS_X_PREFIX = "o_ax_%s";
    public static final String ORDER_ALIAS_Y_PREFIX = "o_ay_%s";
    public static final String WHERE_ALIAS_PREFIX = "w_a_%s";
}