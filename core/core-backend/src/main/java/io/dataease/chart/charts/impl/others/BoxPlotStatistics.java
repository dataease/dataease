package io.dataease.chart.charts.impl.others;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱线图有界 Java 回退使用的统计工具。
 * 空值、空字符串和非数值不参与统计，重复值按实际明细条数保留。
 * 四分位数使用 Type 7 线性插值，异常值使用 Tukey 1.5 倍四分位距规则识别。
 */
final class BoxPlotStatistics {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal LOWER_QUARTILE = new BigDecimal("0.25");
    private static final BigDecimal MEDIAN = new BigDecimal("0.5");
    private static final BigDecimal UPPER_QUARTILE = new BigDecimal("0.75");
    private static final BigDecimal TUKEY_OUTLIER_FACTOR = new BigDecimal("1.5");

    private BoxPlotStatistics() {
    }

    /**
     * 将带维度前缀的明细行按完整维度组合分桶，并计算每个箱体的统计摘要。
     */
    static List<BoxPlotSummary> summarize(List<String[]> rows, int dimensionCount) {
        Map<List<String>, List<BigDecimal>> buckets = new LinkedHashMap<>();
        for (String[] row : rows) {
            if (row == null || row.length <= dimensionCount) {
                continue;
            }
            BigDecimal value = parseNumber(row[dimensionCount]);
            if (value == null) {
                continue;
            }
            List<String> dimensions = new ArrayList<>(Arrays.asList(Arrays.copyOf(row, dimensionCount)));
            buckets.computeIfAbsent(dimensions, ignored -> new ArrayList<>()).add(value);
        }

        List<BoxPlotSummary> result = new ArrayList<>();
        buckets.forEach((dimensions, values) -> result.add(summarizeBucket(dimensions, values)));
        return result;
    }

    /**
     * 围栏只用于判断异常值，上下须取围栏内真实存在的最小值和最大值。
     */
    private static BoxPlotSummary summarizeBucket(List<String> dimensions, List<BigDecimal> values) {
        values.sort(BigDecimal::compareTo);
        BigDecimal q1 = percentile(values, LOWER_QUARTILE);
        BigDecimal median = percentile(values, MEDIAN);
        BigDecimal q3 = percentile(values, UPPER_QUARTILE);
        BigDecimal iqr = q3.subtract(q1, MATH_CONTEXT);
        BigDecimal lowerFence = q1.subtract(iqr.multiply(TUKEY_OUTLIER_FACTOR, MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal upperFence = q3.add(iqr.multiply(TUKEY_OUTLIER_FACTOR, MATH_CONTEXT), MATH_CONTEXT);

        List<BigDecimal> outliers = new ArrayList<>();
        BigDecimal low = null;
        BigDecimal high = null;
        for (BigDecimal value : values) {
            if (value.compareTo(lowerFence) < 0 || value.compareTo(upperFence) > 0) {
                outliers.add(value);
                continue;
            }
            if (low == null) {
                low = value;
            }
            high = value;
        }
        return new BoxPlotSummary(dimensions, low, q1, median, q3, high, outliers, outliers.size(), values.size());
    }

    /**
     * 使用 position = (n - 1) * p 的 Type 7 规则，在相邻两个样本之间做精确十进制插值。
     */
    private static BigDecimal percentile(List<BigDecimal> values, BigDecimal percentile) {
        if (values.size() == 1) {
            return values.getFirst();
        }
        BigDecimal position = BigDecimal.valueOf(values.size() - 1L).multiply(percentile, MATH_CONTEXT);
        int lowerIndex = position.intValue();
        BigDecimal fraction = position.subtract(BigDecimal.valueOf(lowerIndex), MATH_CONTEXT);
        BigDecimal lower = values.get(lowerIndex);
        if (fraction.signum() == 0) {
            return lower;
        }
        BigDecimal upper = values.get(lowerIndex + 1);
        return lower.add(upper.subtract(lower, MATH_CONTEXT).multiply(fraction, MATH_CONTEXT), MATH_CONTEXT);
    }

    private static BigDecimal parseNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * outlierCount 表示真实异常值总数，outliers 只保存受展示上限约束后返回前端的异常点。
     */
    record BoxPlotSummary(
            List<String> dimensions,
            BigDecimal low,
            BigDecimal q1,
            BigDecimal median,
            BigDecimal q3,
            BigDecimal high,
            List<BigDecimal> outliers,
            long outlierCount,
            long count
    ) {
        BoxPlotSummary withDisplayedOutliers(List<BigDecimal> displayedOutliers) {
            return new BoxPlotSummary(dimensions, low, q1, median, q3, high, displayedOutliers, outlierCount, count);
        }
    }
}
