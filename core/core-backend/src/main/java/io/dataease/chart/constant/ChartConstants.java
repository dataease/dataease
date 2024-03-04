package io.dataease.chart.constant;

/**
 * @Author gin
 */
public class ChartConstants {
    public static final String YEAR_MOM = "year_mom";
    public static final String MONTH_MOM = "month_mom";
    public static final String YEAR_YOY = "year_yoy";
    public static final String DAY_MOM = "day_mom";
    public static final String MONTH_YOY = "month_yoy";
    public static final String[] M_Y = {YEAR_MOM, MONTH_MOM, YEAR_YOY, DAY_MOM, MONTH_YOY};

    //图表数据查询模式
    public static final class VIEW_RESULT_MODE {

        // 所有
        public static final String ALL = "all";

        // 自定义
        public static final String CUSTOM = "custom";
    }
}
