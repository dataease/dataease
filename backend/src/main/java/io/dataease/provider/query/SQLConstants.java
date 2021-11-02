package io.dataease.provider.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/7/8 3:12 下午
 */
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
    public static final String SQL_TEMPLATE = "sql/sqlTemplate.stg";

    public static final String TABLE_ALIAS_PREFIX = "T_A_%s";
    public static final String FIELD_ALIAS_X_PREFIX = "F_AX_%s";
    public static final String FIELD_ALIAS_Y_PREFIX = "F_AY_%s";
    public static final String GROUP_ALIAS_PREFIX = "G_A_%s";
    public static final String ORDER_ALIAS_X_PREFIX = "O_AX_%s";
    public static final String ORDER_ALIAS_Y_PREFIX = "O_AY_%s";
    public static final String WHERE_ALIAS_PREFIX = "W_A_%s";
}
