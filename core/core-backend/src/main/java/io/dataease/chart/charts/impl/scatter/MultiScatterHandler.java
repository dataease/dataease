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
 * 多维散点图处理器。
 * 所有轴字段均作为维度处理，不做聚合，直接查询明细数据。
 */
@Component
public class MultiScatterHandler extends DefaultChartHandler {
    @Getter
    private final String type = "multi-scatter";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var colorAxis = new ArrayList<ChartViewFieldDTO>();
        if (CollectionUtils.isNotEmpty(view.getExtColor())) {
            colorAxis.addAll(view.getExtColor());
        }
        result.getAxisMap().put(ChartAxis.xAxis, colorAxis);
        result.getAxisMap().put(ChartAxis.extColor, colorAxis);

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

        var detailAxis = new ArrayList<ChartViewFieldDTO>();
        detailAxis.addAll(filteredXAxis);
        detailAxis.addAll(filteredYAxis);
        detailAxis.addAll(filteredExtBubble);
        detailAxis.addAll(filteredYAxisExt);
        detailAxis.addAll(filteredExtLabel);
        detailAxis.addAll(filteredExtTooltip);
        result.getAxisMap().put(ChartAxis.yAxis, detailAxis);
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
        var allDimFields = new ArrayList<ChartViewFieldDTO>();
        allDimFields.addAll(xAxis);
        allDimFields.addAll(yAxis);

        Dimension2SQLObj.dimension2sqlObj(sqlMeta, allDimFields, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite multi-scatter chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");

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
        var detailAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var xAxisQuota = retainAxisFields(view.getXAxis(), detailAxis);
        var yAxisQuota = retainAxisFields(view.getYAxis(), detailAxis);
        var extBubble = retainAxisFields(formatResult.getAxisMap().get(ChartAxis.extBubble), detailAxis);
        var yAxisExt = retainAxisFields(formatResult.getAxisMap().get(ChartAxis.yAxisExt), detailAxis);
        view.setExtLabel(retainAxisFields(formatResult.getAxisMap().get(ChartAxis.extLabel), detailAxis));
        view.setExtTooltip(retainAxisFields(formatResult.getAxisMap().get(ChartAxis.extTooltip), detailAxis));
        return ChartDataBuild.transMultiScatterDataAntV(
                formatResult.getAxisMap().get(ChartAxis.extColor),
                xAxisQuota,
                yAxisQuota,
                extBubble,
                yAxisExt,
                view,
                data,
                isDrill
        );
    }

    private List<ChartViewFieldDTO> filterNonDimensionFields(List<ChartViewFieldDTO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return new ArrayList<>();
        }
        return fields.stream()
                .filter(f -> !StringUtils.equalsIgnoreCase(f.getOriginName(), "*"))
                .collect(Collectors.toList());
    }

    private List<ChartViewFieldDTO> retainAxisFields(List<ChartViewFieldDTO> source, List<ChartViewFieldDTO> retainedAxis) {
        if (CollectionUtils.isEmpty(source) || CollectionUtils.isEmpty(retainedAxis)) {
            return new ArrayList<>();
        }
        return source.stream()
                .filter(field -> retainedAxis.stream().anyMatch(retained -> retained.getId().equals(field.getId())))
                .collect(Collectors.toList());
    }
}



