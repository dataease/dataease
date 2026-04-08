package io.dataease.chart.charts.impl.scatter;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多维散点图处理器
 * 所有轴字段均作为维度处理（不聚合），直接查询明细数据
 * extColor: 颜色维度字段
 * xAxis: 横轴指标字段
 * yAxis: 纵轴指标字段
 * extBubble: 气泡大小指标字段
 * yAxisExt: 明暗指标字段
 */
@Component
public class MultiScatterHandler extends DefaultChartHandler {
    @Getter
    private String type = "multi-scatter";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = new ArrayList<ChartViewFieldDTO>();
        if (CollectionUtils.isNotEmpty(view.getExtColor())) {
            xAxis.addAll(view.getExtColor());
        }
        result.getAxisMap().put(ChartAxis.xAxis, xAxis);
        result.getAxisMap().put(ChartAxis.extColor, view.getExtColor());
        var filteredXAxis = filterNonDimensionFields(view.getXAxis());
        var filteredYAxis = filterNonDimensionFields(view.getYAxis());
        var filteredExtBubble = filterNonDimensionFields(view.getExtBubble());
        var filteredYAxisExt = filterNonDimensionFields(view.getYAxisExt());
        var filteredExtLabel = filterNonDimensionFields(view.getExtLabel());
        var filteredExtTooltip = filterNonDimensionFields(view.getExtTooltip());
        view.setXAxis(filteredXAxis);
        view.setYAxis(filteredYAxis);
        view.setExtBubble(filteredExtBubble);
        view.setYAxisExt(filteredYAxisExt);
        view.setExtLabel(filteredExtLabel);
        view.setExtTooltip(filteredExtTooltip);
        var yAxis = new ArrayList<ChartViewFieldDTO>();
        yAxis.addAll(filteredXAxis);
        yAxis.addAll(filteredYAxis);
        yAxis.addAll(filteredExtBubble);
        yAxis.addAll(filteredYAxisExt);
        yAxis.addAll(filteredExtLabel);
        yAxis.addAll(filteredExtTooltip);
        result.getAxisMap().put(ChartAxis.yAxis, yAxis);
        result.getAxisMap().put(ChartAxis.extBubble, filteredExtBubble);
        result.getAxisMap().put(ChartAxis.yAxisExt, filteredYAxisExt);
        result.getAxisMap().put(ChartAxis.extLabel, filteredExtLabel);
        result.getAxisMap().put(ChartAxis.extTooltip, filteredExtTooltip);
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross();
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setIsCross(crossDs);
        datasourceRequest.setDsList(dsMap);

        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");

        // 所有字段都作为维度处理（Dimension），不做聚合
        // xAxis 是 extColor 维度字段，yAxis 是所有指标字段
        // Dimension2SQLObj.dimension2sqlObj 会覆盖 meta.xFields，所以需要合并为一个列表一次调用
        var allDimFields = new ArrayList<ChartViewFieldDTO>();
        allDimFields.addAll(xAxis);
        allDimFields.addAll(yAxis);
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, allDimFields, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);

        String querySql = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite multi-scatter chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");

        // 自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, yAxis, view.getSortPriority(), data);

        // 数据重组
        var result = this.buildResult(view, formatResult, filterResult, data);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        return calcResult;
    }

    @Override
    public Map<String, Object> buildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult.getFilterList().stream().anyMatch(ele -> ele.getFilterType() == 1);
        var extColor = formatResult.getAxisMap().get(ChartAxis.extColor);
        var xAxisQuota = view.getXAxis() != null ? view.getXAxis() : new ArrayList<ChartViewFieldDTO>();
        var yAxisQuota = view.getYAxis() != null ? view.getYAxis() : new ArrayList<ChartViewFieldDTO>();
        var extBubble = formatResult.getAxisMap().get(ChartAxis.extBubble);
        var yAxisExt = formatResult.getAxisMap().get(ChartAxis.yAxisExt);

        return ChartDataBuild.transMultiScatterDataAntV(
                extColor, xAxisQuota, yAxisQuota, extBubble, yAxisExt, view, data, isDrill
        );
    }

    /**
     * 过滤掉无法作为维度（非聚合）查询的字段，例如 记录数（originName="*"）。
     * 这些字段需要聚合函数（如 COUNT(*)），在多维散点图的明细查询中不可用。
     */
    private List<ChartViewFieldDTO> filterNonDimensionFields(List<ChartViewFieldDTO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return new ArrayList<>();
        }
        return fields.stream()
                .filter(f -> !StringUtils.equalsIgnoreCase(f.getOriginName(), "*"))
                .collect(Collectors.toList());
    }
}


