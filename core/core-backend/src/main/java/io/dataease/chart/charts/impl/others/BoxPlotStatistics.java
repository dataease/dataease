package io.dataease.chart.charts.impl.others;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱线图统计工具
 * 统计过程固定为以下步骤，前端不重复计算这些统计量
 * 剔除空值、空字符串和无法转换为数字的值
 * 将每个样本桶内的有效指标明细值升序排列
 * 使用 Type 7 线性插值计算 Q1、中位数和 Q3
 * 使用 Tukey 1.5 倍四分位距规则识别异常值
 * 在非异常值中取得真实最小值和最大值作为上下须
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
     * 将查询得到的明细行转换为每个箱体对应的统计摘要
     * 同一组内每一条有效明细都计为一个样本，异常值仍包含在有效样本数中
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
            // 样本数只统计可转换为数字的有效明细，空值和非数字不进入任何统计量
            List<String> dimensions = new ArrayList<>(Arrays.asList(Arrays.copyOf(row, dimensionCount)));
            buckets.computeIfAbsent(dimensions, ignored -> new ArrayList<>()).add(value);
        }

        List<BoxPlotSummary> result = new ArrayList<>();
        buckets.forEach((dimensions, values) -> result.add(summarizeBucket(dimensions, values)));
        return result;
    }

    /**
     * 计算一个维度组合对应的箱体
     * 异常值判断采用严格小于或严格大于，等于的值属于正常样本
     * 同值的多条异常明细分别保留，异常值数量按明细条数计算而不是按不同值计数
     */
    private static BoxPlotSummary summarizeBucket(List<String> dimensions, List<BigDecimal> values) {
        values.sort(BigDecimal::compareTo);
        BigDecimal q1 = percentile(values, LOWER_QUARTILE);
        BigDecimal median = percentile(values, MEDIAN);
        BigDecimal q3 = percentile(values, UPPER_QUARTILE);
        BigDecimal iqr = q3.subtract(q1, MATH_CONTEXT);
        // Tukey 口径：围栏用于判定异常值，须线取围栏内真实存在的最小值和最大值
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

        // 非空样本至少有一个值落在由自身四分位数形成的围栏内
        return new BoxPlotSummary(dimensions, low, q1, median, q3, high, outliers, values.size());
    }

    /**
     * 计算分位数
     * 例如四个样本 1、2、3、4 的 Q1 位置为 0.75，结果为 1 + (2 - 1) * 0.75 = 1.75
     */
    private static BigDecimal percentile(List<BigDecimal> values, BigDecimal percentile) {
        if (values.size() == 1) {
            return values.getFirst();
        }
        // 使用 Type 7 线性插值，与 Excel PERCENTILE.INC 及常用统计软件默认口径一致
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
     * 单个箱体的完整统计结果
     */
    record BoxPlotSummary(
            List<String> dimensions,
            BigDecimal low,
            BigDecimal q1,
            BigDecimal median,
            BigDecimal q3,
            BigDecimal high,
            List<BigDecimal> outliers,
            int count
    ) {
    }
}
