package io.dataease.engine.func;

import io.dataease.exception.DEException;

import java.util.Locale;
import java.util.Map;

/**
 * @Author Junjun
 */
public class FunctionConstant {
    /**
     * 所有聚合函数数组
     */
    public static final String[] AGG_FUNC = {"SUM", "AVG", "MAX", "MIN", "COUNT", "STDDEV", "STDDEV_POP", "STDDEV_SAMP", "VAR_POP", "VAR_SAMP"};

    private static final Map<String, String> CHART_AGG_FUNC = Map.ofEntries(
            Map.entry("sum", "SUM"),
            Map.entry("avg", "AVG"),
            Map.entry("max", "MAX"),
            Map.entry("min", "MIN"),
            Map.entry("stddev_pop", "STDDEV_POP"),
            Map.entry("var_pop", "VAR_POP"),
            Map.entry("count", "COUNT"),
            Map.entry("count_distinct", "COUNT")
    );

    private static final Map<String, String> ASSIST_AGG_FUNC = Map.of(
            "avg", "AVG",
            "max", "MAX",
            "min", "MIN"
    );

    public static String resolveChartAggregation(String summary) {
        if (summary == null || summary.isEmpty()) {
            return summary;
        }
        return resolveAggregation(summary, CHART_AGG_FUNC);
    }

    public static String resolveAssistAggregation(String summary) {
        return resolveAggregation(summary, ASSIST_AGG_FUNC);
    }

    private static String resolveAggregation(String summary, Map<String, String> supportedFunctions) {
        String function = summary == null ? null : supportedFunctions.get(summary.toLowerCase(Locale.ROOT));
        if (function == null) {
            DEException.throwException("Illegal aggregation function");
        }
        return function;
    }
}
