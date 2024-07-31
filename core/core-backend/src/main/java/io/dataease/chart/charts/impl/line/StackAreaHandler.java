package io.dataease.chart.charts.impl.line;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StackAreaHandler extends YoyChartHandler {
    @Getter
    private String type = "area-stack";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = result.getAxisMap().get(ChartAxis.xAxis);
        xAxis.addAll(view.getExtStack());
        var yAxis = result.getAxisMap().get(ChartAxis.yAxis);
        yAxis.addAll(view.getExtLabel());
        yAxis.addAll(view.getExtTooltip());
        result.getAxisMap().put(ChartAxis.extStack, view.getExtStack());
        result.getAxisMap().put(ChartAxis.extLabel, view.getExtLabel());
        result.getAxisMap().put(ChartAxis.extTooltip, view.getExtTooltip());
        return result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var drillAxis = xAxis.stream().filter(axis -> FieldSource.DRILL == axis.getSource()).toList();
        var xAxisBase = xAxis.subList(0, xAxis.size() - extStack.size() - drillAxis.size());
        return ChartDataBuild.transStackChartDataAntV(xAxisBase, xAxis, yAxis, view, data, extStack, isDrill);
    }

    @Override
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var result = super.customFilter(view, filterList, formatResult);
        List<ChartDrillRequest> drillRequestList = view.getChartExtRequest().getDrill();
        var drillFields = formatResult.getAxisMap().get(ChartAxis.drill);
        // 堆叠维度下钻
        if (ObjectUtils.isNotEmpty(drillRequestList) && (drillFields.size() > drillRequestList.size())) {
            List<ChartExtFilterDTO> noDrillFilterList = filterList
                    .stream()
                    .filter(ele -> ele.getFilterType() != 1)
                    .collect(Collectors.toList());
            var noDrillFieldAxis = formatResult.getAxisMap().get(ChartAxis.xAxis)
                    .stream()
                    .filter(ele -> ele.getSource() != FieldSource.DRILL)
                    .collect(Collectors.toList());
            List<ChartExtFilterDTO> drillFilters = new ArrayList<>();
            ArrayList<ChartViewFieldDTO> fieldsToFilter = new ArrayList<>();
            var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
            if (ObjectUtils.isNotEmpty(extStack) &&
                    Objects.equals(drillFields.get(0).getId(), extStack.get(0).getId())) {
                fieldsToFilter.addAll(view.getXAxis());
            }
            groupStackDrill(noDrillFieldAxis, noDrillFilterList, fieldsToFilter, drillFields, drillRequestList);
            formatResult.getAxisMap().put(ChartAxis.xAxis, noDrillFieldAxis);
            result.setFilterList(noDrillFilterList);
        }
        return (T) result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        var result = (T) super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        try {
            //如果有同环比过滤,应该用原始sql
            var originSql = result.getQuerySql();
            var dynamicAssistFields = getDynamicAssistFields(view);
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var assistFields = getAssistFields(dynamicAssistFields, yAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                req.setDsList(dsMap);
                var assistSql = assistSQL(originSql, assistFields);
                req.setQuery(assistSql);
                logger.info("calcite assist sql: " + assistSql);
                var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                result.setAssistData(assistData);
                result.setDynamicAssistFields(dynamicAssistFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
