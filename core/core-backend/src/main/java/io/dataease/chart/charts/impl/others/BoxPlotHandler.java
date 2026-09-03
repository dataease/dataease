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
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartCalcDataResult;
import io.dataease.extensions.view.dto.ChartDimensionDTO;
import io.dataease.extensions.view.dto.ChartQuotaDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.CustomFilterResult;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class BoxPlotHandler extends DefaultChartHandler {

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
            // 查询时类别和可选分组都作为维度，统计时再按二者组合形成独立箱体
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
        List<String> dsList = dsMap.values().stream().map(DatasourceSchemaDTO::getType).toList();
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross();
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        fillDatasourceRequest(datasourceRequest, crossDs, dsMap, sqlMap);

        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var detailFields = new ArrayList<ChartViewFieldDTO>(xAxis);
        detailFields.add(yAxis.getFirst());
        // 箱线统计依赖明细值，字段必须一次性按维度查询以避免 SQL 聚合
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

        String querySql;
        if (view.getIsExcelExport()) {
            querySql = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, view);
        } else {
            querySql = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, 0, maxSamples + 1);
        }
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite box plot chart sql: " + querySql);
        List<String[]> rows = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        rows = ChartDataUtil.resultCustomSort(xAxis, yAxis, view.getSortPriority(), rows);

        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        if (view.getIsExcelExport()) {
            calcResult.setOriginData(rows);
            calcResult.setData(Map.of("data", List.of()));
            return calcResult;
        }
        if (rows.size() > maxSamples) {
            DEException.throwException(String.format(Translator.get("i18n_box_plot_sample_limit"), maxSamples));
        }

        List<BoxPlotStatistics.BoxPlotSummary> summaries = BoxPlotStatistics.summarize(rows, xAxis.size());
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom") && view.getResultCount() != null) {
            summaries = summaries.stream().limit(view.getResultCount()).toList();
        }
        calcResult.setData(buildBoxPlotResult(filterResult, xAxis, xAxisExt, yAxis.getFirst(), summaries));
        calcResult.setOriginData(buildSummaryRows(summaries));
        return calcResult;
    }

    private void validateAxes(
            ChartViewDTO view,
            List<ChartViewFieldDTO> xAxis,
            List<ChartViewFieldDTO> xAxisExt,
            List<ChartViewFieldDTO> yAxis
    ) {
        int baseDimensionCount = view.getXAxis() == null ? 0 : view.getXAxis().size();
        boolean invalid = baseDimensionCount != 1
                || xAxis == null
                || xAxis.size() < baseDimensionCount + CollectionUtils.size(xAxisExt)
                || CollectionUtils.size(xAxisExt) > 1
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
            // 单独返回异常值数量，避免前端从展示数组重复推导统计口径
            item.put("outlierCount", summary.outliers().size());
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

    private String toPlainString(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }
}
