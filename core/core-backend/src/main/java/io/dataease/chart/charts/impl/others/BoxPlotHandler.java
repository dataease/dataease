package io.dataease.chart.charts.impl.others;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.constant.DeTypeConstants;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartCalcDataResult;
import io.dataease.extensions.view.dto.ChartDimensionDTO;
import io.dataease.extensions.view.dto.ChartQuotaDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.CustomFilterResult;
import io.dataease.extensions.view.dto.FieldSource;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.i18n.Translator;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 箱线图处理器：数据库具备窗口函数能力时固定执行两条受控查询，否则执行带样本上限的 Java 回退。
 */
@Component
public class BoxPlotHandler extends DefaultChartHandler {

    private static final Set<String> FALLBACK_WARNED_DATASOURCES = ConcurrentHashMap.newKeySet();

    @Getter
    private final String type = "box-plot";

    @Value("${dataease.chart.box-plot.max-samples:100000}")
    private int maxSamples;

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = new ArrayList<ChartViewFieldDTO>();
        if (CollectionUtils.isNotEmpty(view.getXAxis())) {
            xAxis.addAll(view.getXAxis());
        }
        if (CollectionUtils.isNotEmpty(view.getXAxisExt())) {
            xAxis.addAll(view.getXAxisExt());
        }
        result.getAxisMap().put(ChartAxis.xAxis, xAxis);
        result.getAxisMap().put(
                ChartAxis.xAxisExt,
                CollectionUtils.isEmpty(view.getXAxisExt()) ? new ArrayList<>() : new ArrayList<>(view.getXAxisExt())
        );
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(
            ChartViewDTO view,
            AxisFormatResult formatResult,
            CustomFilterResult filterResult,
            Map<String, Object> sqlMap,
            SQLMeta sqlMeta,
            Provider provider
    ) {
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        validateAxes(view, xAxis, xAxisExt, yAxis);

        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        boolean crossDs = ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross();
        DatasourceRequest capabilityRequest = new DatasourceRequest();
        fillDatasourceRequest(capabilityRequest, crossDs, dsMap, sqlMap);

        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var detailFields = new ArrayList<ChartViewFieldDTO>(xAxis);
        detailFields.add(yAxis.getFirst());
        sqlMeta.setChartType(type);
        Dimension2SQLObj.dimension2sqlObj(
                sqlMeta,
                detailFields,
                FieldUtil.transFields(allFields),
                crossDs,
                dsMap,
                Utils.getParams(FieldUtil.transFields(allFields)),
                view.getCalParams(),
                pluginManage
        );

        if (provider.supportsWindowFunctions(capabilityRequest)) {
            return (T) calcWithDatabase(view, filterResult, sqlMap, sqlMeta, provider, dsMap, crossDs, xAxis, xAxisExt, yAxis.getFirst());
        }
        warnFallback(dsMap);
        return (T) calcWithJavaFallback(view, filterResult, sqlMap, sqlMeta, provider, dsMap, crossDs, xAxis, xAxisExt, yAxis);
    }

    private ChartCalcDataResult calcWithDatabase(
            ChartViewDTO view,
            CustomFilterResult filterResult,
            Map<String, Object> sqlMap,
            SQLMeta sqlMeta,
            Provider provider,
            Map<Long, DatasourceSchemaDTO> dsMap,
            boolean crossDs,
            List<ChartViewFieldDTO> xAxis,
            List<ChartViewFieldDTO> xAxisExt,
            ChartViewFieldDTO yAxis
    ) {
        List<SQLObj> orders = sqlMeta.getXOrders();
        String baseSql;
        try {
            // 部分数据库不允许明细 CTE 自带 ORDER BY，且分位数会在窗口函数内独立排序，因此这里移除明细排序。
            sqlMeta.setXOrders(new ArrayList<>());
            baseSql = SQLProvider.createQuerySQL(sqlMeta, false, false, false);
        } finally {
            sqlMeta.setXOrders(orders);
        }

        BoxPlotSqlBuilder.Queries queries = BoxPlotSqlBuilder.build(baseSql, xAxis, view);
        String summarySql = provider.rebuildSQL(queries.summarySql(), sqlMeta, crossDs, dsMap);
        String outlierSql = provider.rebuildSQL(queries.outlierSql(), sqlMeta, crossDs, dsMap);
        logger.debug("箱线图统计摘要 SQL：{}", summarySql);
        logger.debug("箱线图异常点 SQL：{}", outlierSql);

        List<String[]> summaryRows = fetch(provider, summarySql, crossDs, dsMap, sqlMap);
        LinkedHashMap<List<String>, BoxPlotStatistics.BoxPlotSummary> summaryMap = parseSummaryRows(summaryRows, xAxis.size());
        List<String[]> outlierRows = fetch(provider, outlierSql, crossDs, dsMap, sqlMap);
        mergeOutliers(summaryMap, outlierRows, xAxis.size());
        List<BoxPlotStatistics.BoxPlotSummary> summaries = new ArrayList<>(summaryMap.values());

        ChartCalcDataResult result = new ChartCalcDataResult();
        result.setContext(filterResult.getContext());
        result.setQuerySql(summarySql);
        result.setData(buildBoxPlotResult(filterResult, xAxis, xAxisExt, yAxis, summaries));
        result.setOriginData(buildSummaryRows(summaries));
        return result;
    }

    private ChartCalcDataResult calcWithJavaFallback(
            ChartViewDTO view,
            CustomFilterResult filterResult,
            Map<String, Object> sqlMap,
            SQLMeta sqlMeta,
            Provider provider,
            Map<Long, DatasourceSchemaDTO> dsMap,
            boolean crossDs,
            List<ChartViewFieldDTO> xAxis,
            List<ChartViewFieldDTO> xAxisExt,
            List<ChartViewFieldDTO> yAxis
    ) {
        int sampleLimit = Math.max(1, maxSamples);
        List<String> dsTypes = dsMap.values().stream().map(DatasourceSchemaDTO::getType).toList();
        String querySql = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, Utils.isNeedOrder(dsTypes), false, 0, sampleLimit + 1);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        logger.debug("箱线图有界回退 SQL：{}", querySql);
        List<String[]> rows = fetch(provider, querySql, crossDs, dsMap, sqlMap);
        if (rows.size() > sampleLimit) {
            DEException.throwException(String.format(Translator.get("i18n_box_plot_sample_limit"), sampleLimit));
        }
        rows = ChartDataUtil.resultCustomSort(xAxis, yAxis, view.getSortPriority(), rows);

        List<BoxPlotStatistics.BoxPlotSummary> summaries = BoxPlotStatistics.summarize(rows, xAxis.size());
        Integer displayLimit = resultDisplayLimit(view);
        if (displayLimit != null) {
            // 用户配置的结果展示数量只限制箱体，箱体内的真实异常点完整保留
            summaries = summaries.stream().limit(displayLimit).toList();
        }

        ChartCalcDataResult result = new ChartCalcDataResult();
        result.setContext(filterResult.getContext());
        result.setQuerySql(querySql);
        result.setData(buildBoxPlotResult(filterResult, xAxis, xAxisExt, yAxis.getFirst(), summaries));
        result.setOriginData(buildSummaryRows(summaries));
        return result;
    }

    private List<String[]> fetch(
            Provider provider,
            String querySql,
            boolean crossDs,
            Map<Long, DatasourceSchemaDTO> dsMap,
            Map<String, Object> sqlMap
    ) {
        DatasourceRequest request = new DatasourceRequest();
        fillDatasourceRequest(request, crossDs, dsMap, sqlMap);
        request.setQuery(querySql);
        return (List<String[]>) provider.fetchResultField(request).get("data");
    }

    private LinkedHashMap<List<String>, BoxPlotStatistics.BoxPlotSummary> parseSummaryRows(List<String[]> rows, int dimensionCount) {
        LinkedHashMap<List<String>, BoxPlotStatistics.BoxPlotSummary> result = new LinkedHashMap<>();
        for (String[] row : rows) {
            if (row == null || row.length < dimensionCount + 9) {
                continue;
            }
            List<String> dimensions = new ArrayList<>(dimensionCount);
            for (int i = 0; i < dimensionCount; i++) {
                dimensions.add(row[i]);
            }
            BoxPlotStatistics.BoxPlotSummary summary = new BoxPlotStatistics.BoxPlotSummary(
                    Collections.unmodifiableList(dimensions),
                    toBigDecimal(row[dimensionCount]),
                    toBigDecimal(row[dimensionCount + 1]),
                    toBigDecimal(row[dimensionCount + 2]),
                    toBigDecimal(row[dimensionCount + 3]),
                    toBigDecimal(row[dimensionCount + 6]),
                    new ArrayList<>(),
                    toLong(row[dimensionCount + 8]),
                    toLong(row[dimensionCount + 7])
            );
            result.put(summary.dimensions(), summary);
        }
        return result;
    }

    private void mergeOutliers(
            Map<List<String>, BoxPlotStatistics.BoxPlotSummary> summaries,
            List<String[]> rows,
            int dimensionCount
    ) {
        for (String[] row : rows) {
            if (row == null || row.length <= dimensionCount) {
                continue;
            }
            List<String> dimensions = new ArrayList<>(dimensionCount);
            for (int i = 0; i < dimensionCount; i++) {
                dimensions.add(row[i]);
            }
            BoxPlotStatistics.BoxPlotSummary summary = summaries.get(dimensions);
            BigDecimal value = toBigDecimal(row[dimensionCount]);
            if (summary != null && value != null) {
                summary.outliers().add(value);
            }
        }
    }

    private Integer resultDisplayLimit(ChartViewDTO view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom") && view.getResultCount() != null) {
            return Math.max(0, view.getResultCount());
        }
        return null;
    }

    private void warnFallback(Map<Long, DatasourceSchemaDTO> dsMap) {
        String datasourceKey = dsMap.values().stream()
                .map(ds -> ds.getId() + ":" + ds.getType() + ":" + ds.getDsVersion())
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("unknown");
        if (FALLBACK_WARNED_DATASOURCES.add(datasourceKey)) {
            logger.warn(
                    "箱线图已对数据源 [{}] 启用有界 Java 回退；maxSamples={}",
                    datasourceKey,
                    Math.max(1, maxSamples)
            );
        }
    }

    private void validateAxes(
            ChartViewDTO view,
            List<ChartViewFieldDTO> xAxis,
            List<ChartViewFieldDTO> xAxisExt,
            List<ChartViewFieldDTO> yAxis
    ) {
        int baseDimensionCount = CollectionUtils.size(view.getXAxis());
        int groupDimensionCount = CollectionUtils.size(xAxisExt);
        // 公共下钻链路会把当前层级之后的维度追加到 xAxis
        long drillDimensionCount = xAxis.stream()
                .filter(field -> FieldSource.DRILL == field.getSource())
                .count();
        boolean invalid = baseDimensionCount != 1
                || groupDimensionCount > 1
                || CollectionUtils.size(xAxis) != baseDimensionCount + groupDimensionCount + drillDimensionCount
                || CollectionUtils.size(yAxis) != 1;
        if (!invalid) {
            ChartViewFieldDTO valueField = yAxis.getFirst();
            invalid = !List.of(DeTypeConstants.DE_INT, DeTypeConstants.DE_FLOAT).contains(valueField.getDeType())
                    || StringUtils.equals(valueField.getOriginName(), "*");
        }
        if (invalid) {
            DEException.throwException(Translator.get("i18n_box_plot_field_error"));
        }
    }

    private Map<String, Object> buildBoxPlotResult(
            CustomFilterResult filterResult,
            List<ChartViewFieldDTO> xAxis,
            List<ChartViewFieldDTO> xAxisExt,
            ChartViewFieldDTO yAxis,
            List<BoxPlotStatistics.BoxPlotSummary> summaries
    ) {
        boolean isDrill = filterResult.getFilterList().stream().anyMatch(filter -> filter.getFilterType() == 1);
        int categoryIndex = isDrill ? xAxis.size() - 1 : 0;
        int groupIndex = xAxisExt.isEmpty() ? -1 : findFieldIndex(xAxis, xAxisExt.getFirst().getId());
        List<Map<String, Object>> data = new ArrayList<>();

        for (BoxPlotStatistics.BoxPlotSummary summary : summaries) {
            Map<String, Object> item = new LinkedHashMap<>();
            String category = summary.dimensions().get(categoryIndex);
            item.put("field", category);
            item.put("name", category);
            if (groupIndex >= 0) {
                item.put("category", summary.dimensions().get(groupIndex));
            }
            item.put("low", summary.low());
            item.put("q1", summary.q1());
            item.put("median", summary.median());
            item.put("q3", summary.q3());
            item.put("high", summary.high());
            item.put("outliers", summary.outliers());
            item.put("outlierCount", summary.outlierCount());
            item.put("count", summary.count());
            item.put("value", summary.median());
            item.put("dimensionList", buildDimensions(xAxis, summary.dimensions()));
            item.put("quotaList", List.of(buildQuota(yAxis)));
            data.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    private int findFieldIndex(List<ChartViewFieldDTO> fields, Long id) {
        for (int i = 0; i < fields.size(); i++) {
            if (Objects.equals(fields.get(i).getId(), id)) {
                return i;
            }
        }
        return -1;
    }

    private List<ChartDimensionDTO> buildDimensions(List<ChartViewFieldDTO> fields, List<String> values) {
        List<ChartDimensionDTO> result = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            ChartDimensionDTO dimension = new ChartDimensionDTO();
            dimension.setId(fields.get(i).getId());
            dimension.setValue(values.get(i));
            result.add(dimension);
        }
        return result;
    }

    private ChartQuotaDTO buildQuota(ChartViewFieldDTO field) {
        ChartQuotaDTO quota = new ChartQuotaDTO();
        quota.setId(field.getId());
        return quota;
    }

    private List<String[]> buildSummaryRows(List<BoxPlotStatistics.BoxPlotSummary> summaries) {
        List<String[]> result = new ArrayList<>();
        for (BoxPlotStatistics.BoxPlotSummary summary : summaries) {
            String[] row = new String[summary.dimensions().size() + 1];
            for (int i = 0; i < summary.dimensions().size(); i++) {
                row[i] = summary.dimensions().get(i);
            }
            row[row.length - 1] = toPlainString(summary.median());
            result.add(row);
        }
        return result;
    }

    private BigDecimal toBigDecimal(String value) {
        return StringUtils.isBlank(value) ? null : new BigDecimal(value);
    }

    private long toLong(String value) {
        return StringUtils.isBlank(value) ? 0L : new BigDecimal(value).longValue();
    }

    private String toPlainString(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }
}
