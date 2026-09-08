package io.dataease.chart.charts.impl.others;

import io.dataease.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.SortAxis;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于图表通用查询链路生成的权限过滤明细 SQL，构造箱线图的统计摘要和异常点两条查询语句。
 * 两条查询复用同一个基础关系和 Type 7 统计过程，避免因过滤条件或统计口径不一致产生偏差。
 */
final class BoxPlotSqlBuilder {

    private BoxPlotSqlBuilder() {
    }

    /**
     * 构造固定两条数据查询：第一条返回每个箱体的完整统计量，第二条返回已展示箱体的全部异常点明细
     */
    static Queries build(String baseSql, List<ChartViewFieldDTO> dimensions, ChartViewDTO view) {
        List<String> aliases = aliases(dimensions.size());
        String dimensionColumns = String.join(", ", aliases);
        String qualifiedGroupColumns = aliases.stream().map(alias -> "box_groups." + alias).reduce((a, b) -> a + ", " + b).orElseThrow();
        String partitionColumns = dimensionColumns;
        String valueAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, dimensions.size());
        String groupOrder = buildGroupOrder(dimensions, view.getSortPriority());
        String joinCondition = aliases.stream()
                .map(alias -> "(box_fenced." + alias + " = box_groups." + alias
                        + " OR (box_fenced." + alias + " IS NULL AND box_groups." + alias + " IS NULL))")
                .reduce((a, b) -> a + " AND " + b)
                .orElseThrow();
        String groupLimit = groupLimit(view);

        // 使用 ROW_NUMBER 和 COUNT 精确定位 Type 7 插值需要的相邻样本，不依赖数据库专有分位数函数。
        String commonCte = """
                WITH box_base AS (
                %s
                ),
                box_valid AS (
                    SELECT %s, %s AS metric_value
                    FROM box_base
                    WHERE %s IS NOT NULL
                ),
                box_ranked AS (
                    SELECT %s, metric_value,
                           ROW_NUMBER() OVER (PARTITION BY %s ORDER BY metric_value) AS sample_rank,
                           COUNT(*) OVER (PARTITION BY %s) AS sample_count
                    FROM box_valid
                ),
                box_percentiles AS (
                    SELECT %s, metric_value, sample_count,
                           MAX(CASE WHEN sample_rank = FLOOR((sample_count - 1) * 0.25) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS q1_lower,
                           MAX(CASE WHEN sample_rank = CEIL((sample_count - 1) * 0.25) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS q1_upper,
                           MAX(CASE WHEN sample_rank = FLOOR((sample_count - 1) * 0.5) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS median_lower,
                           MAX(CASE WHEN sample_rank = CEIL((sample_count - 1) * 0.5) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS median_upper,
                           MAX(CASE WHEN sample_rank = FLOOR((sample_count - 1) * 0.75) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS q3_lower,
                           MAX(CASE WHEN sample_rank = CEIL((sample_count - 1) * 0.75) + 1 THEN metric_value END)
                               OVER (PARTITION BY %s) AS q3_upper
                    FROM box_ranked
                ),
                box_stats AS (
                    SELECT %s, metric_value, sample_count,
                           q1_lower + (q1_upper - q1_lower)
                               * (((sample_count - 1) * 0.25) - FLOOR((sample_count - 1) * 0.25)) AS q1,
                           median_lower + (median_upper - median_lower)
                               * (((sample_count - 1) * 0.5) - FLOOR((sample_count - 1) * 0.5)) AS median,
                           q3_lower + (q3_upper - q3_lower)
                               * (((sample_count - 1) * 0.75) - FLOOR((sample_count - 1) * 0.75)) AS q3
                    FROM box_percentiles
                ),
                box_fenced AS (
                    SELECT %s, metric_value, sample_count, q1, median, q3,
                           q1 - (q3 - q1) * 1.5 AS lower_fence,
                           q3 + (q3 - q1) * 1.5 AS upper_fence
                    FROM box_stats
                ),
                box_distinct_groups AS (
                    SELECT DISTINCT %s
                    FROM box_valid
                ),
                box_groups AS (
                    SELECT %s, ROW_NUMBER() OVER (ORDER BY %s) AS box_group_rank
                    FROM box_distinct_groups
                )
                """.formatted(
                baseSql,
                dimensionColumns, valueAlias, valueAlias,
                dimensionColumns, partitionColumns, partitionColumns,
                dimensionColumns,
                partitionColumns, partitionColumns, partitionColumns,
                partitionColumns, partitionColumns, partitionColumns,
                dimensionColumns,
                dimensionColumns,
                dimensionColumns,
                dimensionColumns, groupOrder
        );

        String summarySql = commonCte + """
                SELECT %s,
                       MIN(CASE WHEN box_fenced.metric_value >= box_fenced.lower_fence
                                     AND box_fenced.metric_value <= box_fenced.upper_fence
                                THEN box_fenced.metric_value END) AS box_low,
                       MAX(box_fenced.q1) AS box_q1,
                       MAX(box_fenced.median) AS box_median,
                       MAX(box_fenced.q3) AS box_q3,
                       MAX(box_fenced.lower_fence) AS box_lower_fence,
                       MAX(box_fenced.upper_fence) AS box_upper_fence,
                       MAX(CASE WHEN box_fenced.metric_value >= box_fenced.lower_fence
                                     AND box_fenced.metric_value <= box_fenced.upper_fence
                                THEN box_fenced.metric_value END) AS box_high,
                       MAX(box_fenced.sample_count) AS box_sample_count,
                       SUM(CASE WHEN box_fenced.metric_value < box_fenced.lower_fence
                                     OR box_fenced.metric_value > box_fenced.upper_fence
                                THEN 1 ELSE 0 END) AS box_outlier_count
                FROM box_fenced
                JOIN box_groups ON %s
                %s
                GROUP BY %s, box_groups.box_group_rank
                ORDER BY box_groups.box_group_rank
                """.formatted(qualifiedGroupColumns, joinCondition, groupLimit, qualifiedGroupColumns);

        String outlierSql = commonCte + """
                SELECT %s, box_fenced.metric_value
                FROM box_fenced
                JOIN box_groups ON %s
                WHERE (box_fenced.metric_value < box_fenced.lower_fence
                       OR box_fenced.metric_value > box_fenced.upper_fence)
                %s
                ORDER BY box_groups.box_group_rank, box_fenced.metric_value
                """.formatted(
                qualifiedGroupColumns,
                joinCondition,
                groupLimit.isBlank() ? "" : "AND " + groupLimit.substring("WHERE ".length())
        );
        return new Queries(summarySql, outlierSql);
    }

    private static List<String> aliases(int dimensionCount) {
        List<String> result = new ArrayList<>(dimensionCount);
        for (int i = 0; i < dimensionCount; i++) {
            result.add(String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
        }
        return result;
    }

    private static String buildGroupOrder(List<ChartViewFieldDTO> dimensions, List<SortAxis> sortPriority) {
        List<ChartViewFieldDTO> orderedFields = new ArrayList<>();
        Set<Long> added = new HashSet<>();
        if (sortPriority != null) {
            for (SortAxis sort : sortPriority) {
                for (ChartViewFieldDTO field : dimensions) {
                    if (sort.getId() != null && sort.getId().equals(field.getId()) && added.add(field.getId())) {
                        orderedFields.add(field);
                    }
                }
            }
        }
        for (ChartViewFieldDTO field : dimensions) {
            if (field.getId() == null || added.add(field.getId())) {
                orderedFields.add(field);
            }
        }

        List<String> orders = new ArrayList<>();
        for (ChartViewFieldDTO field : orderedFields) {
            int index = dimensions.indexOf(field);
            String alias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, index);
            if (StringUtils.equalsIgnoreCase(field.getSort(), "custom_sort") && field.getCustomSort() != null) {
                StringBuilder customOrder = new StringBuilder("CASE");
                int position = 0;
                for (String value : field.getCustomSort()) {
                    if (value == null) {
                        continue;
                    }
                    customOrder.append(" WHEN UPPER(CAST(")
                            .append(alias)
                            // CHAR 是当前窗口函数白名单数据库和内置 Calcite 均支持的 CAST 目标类型
                            .append(" AS CHAR(2000))) = UPPER('")
                            .append(Utils.transValue(value))
                            .append("') THEN ")
                            .append(position++);
                }
                customOrder.append(" ELSE ").append(position).append(" END ASC");
                orders.add(customOrder.toString());
            }
            String direction = StringUtils.equalsIgnoreCase(field.getSort(), "desc") ? "DESC" : "ASC";
            orders.add(alias + " " + direction);
        }
        return String.join(", ", orders);
    }

    private static String groupLimit(ChartViewDTO view) {
        Integer displayLimit = resultDisplayLimit(view);
        if (displayLimit != null) {
            // 结果展示数量只限制参与展示的箱体，第二条查询复用相同箱体范围
            return "WHERE box_groups.box_group_rank <= " + displayLimit;
        }
        return "";
    }

    private static Integer resultDisplayLimit(ChartViewDTO view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom") && view.getResultCount() != null) {
            return Math.max(0, view.getResultCount());
        }
        return null;
    }

    record Queries(String summarySql, String outlierSql) {
    }
}
