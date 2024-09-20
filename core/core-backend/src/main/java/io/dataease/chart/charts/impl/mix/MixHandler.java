package io.dataease.chart.charts.impl.mix;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MixHandler extends YoyChartHandler {
    @Getter
    private final String type = "chart-mix";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var axisMap = new HashMap<ChartAxis, List<ChartViewFieldDTO>>();
        var context = new HashMap<String, Object>();
        AxisFormatResult result = new AxisFormatResult(axisMap, context);
        //左轴分组子维度,非分组不需要
        axisMap.put(ChartAxis.xAxisExt, Collections.emptyList());
        //左轴堆叠子维度,非堆叠不需要
        axisMap.put(ChartAxis.extStack, Collections.emptyList());
        //左轴指标
        axisMap.put(ChartAxis.yAxis, view.getYAxis());
        //右轴分组子维度
        axisMap.put(ChartAxis.extBubble, view.getExtBubble());
        //右轴指标
        axisMap.put(ChartAxis.yAxisExt, view.getYAxisExt());
        //去除除了x轴以外的排序
        axisMap.forEach((k, v) -> {
            v.forEach(x -> x.setSort("none"));
        });
        axisMap.put(ChartAxis.extLabel, view.getExtLabel());
        axisMap.put(ChartAxis.extTooltip, view.getExtTooltip());
        //图表整体主维度
        axisMap.put(ChartAxis.xAxis, new ArrayList<>(view.getXAxis()));
        context.put("xAxisBase", new ArrayList<>(view.getXAxis()));
        axisMap.put(ChartAxis.drill, new ArrayList<>(view.getDrillFields()));
        return result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        if (StringUtils.equals((String) formatResult.getContext().get("isRight"), "isRight")) {
            var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
            var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var xAxisBase = xAxis.subList(0, xAxis.size() - xAxisExt.size());
            return ChartDataBuild.transMixChartDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill, true);
        }

        var xAxisBase = (List<ChartViewFieldDTO>) formatResult.getContext().get("xAxisBase");
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
        var result = ChartDataBuild.transMixChartDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill, false);
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        //计算左轴, 包含 xAxis, yAxis
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        var leftResult = (T) super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        var dynamicAssistFields = getDynamicAssistFields(view);
        try {
            //如果有同环比过滤,应该用原始sql
            var originSql = leftResult.getQuerySql();
            var leftAssistFields = dynamicAssistFields.stream().filter(x -> StringUtils.equalsAnyIgnoreCase(x.getYAxisType(), "left")).toList();
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var assistFields = getAssistFields(leftAssistFields, yAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                req.setDsList(dsMap);
                var assistSql = assistSQL(originSql, assistFields);
                req.setQuery(assistSql);
                logger.debug("calcite assistSql sql: " + assistSql);
                var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                leftResult.setAssistData(assistData);
                leftResult.setDynamicAssistFields(leftAssistFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AxisFormatResult formatResult2 = new AxisFormatResult();
        var axisMap = new HashMap<ChartAxis, List<ChartViewFieldDTO>>();
        axisMap.put(ChartAxis.extLabel, new ArrayList<>(formatResult.getAxisMap().get(ChartAxis.extLabel)));
        axisMap.put(ChartAxis.extTooltip, new ArrayList<>(formatResult.getAxisMap().get(ChartAxis.extTooltip)));
        axisMap.put(ChartAxis.drill, new ArrayList<>(formatResult.getAxisMap().get(ChartAxis.drill)));
        formatResult2.setAxisMap(axisMap);
        formatResult2.setContext(formatResult.getContext());

        // 计算右轴，包含 xAxis,xAxisExt,yAxisExt,需要去掉 group 和 stack
        var xAxis = new ArrayList<>(view.getXAxis());
        var extBubble = new ArrayList<>(formatResult.getAxisMap().get(ChartAxis.extBubble));
        xAxis.addAll(extBubble);
        var dillAxis = (ArrayList<ChartViewFieldDTO>) formatResult.getContext().get("dillAxis");
        var fields = xAxis.stream().map(ChartViewFieldDTO::getId).collect(Collectors.toSet());
        for (ChartViewFieldDTO dillAxi : dillAxis) {
            if (!fields.contains(dillAxi.getId())) {
                xAxis.add(dillAxi);
            }
        }
        formatResult2.getAxisMap().put(ChartAxis.xAxis, xAxis);
        formatResult2.getAxisMap().put(ChartAxis.xAxisExt, extBubble);
        var yAxisExt = new ArrayList<>(formatResult.getAxisMap().get(ChartAxis.yAxisExt));
        formatResult2.getAxisMap().put(ChartAxis.yAxis, yAxisExt);
        formatResult2.getContext().remove("yoyFiltered");
        formatResult2.getContext().put("isRight", "isRight");


        formatResult.getContext().put("subAxisMap", axisMap);

        // 右轴重新检测同环比过滤
        customFilter(view, filterResult.getFilterList(), formatResult2);
        var rightResult = (T) super.calcChartResult(view, formatResult2, filterResult, sqlMap, sqlMeta, provider);
        try {
            //如果有同环比过滤,应该用原始sql
            var originSql = rightResult.getQuerySql();
            var rightAssistFields = dynamicAssistFields.stream().filter(x -> StringUtils.equalsAnyIgnoreCase(x.getYAxisType(), "right")).toList();
            var yAxis = formatResult2.getAxisMap().get(ChartAxis.yAxis);
            var assistFields = getAssistFields(rightAssistFields, yAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                req.setDsList(dsMap);
                var assistSql = assistSQL(originSql, assistFields);
                req.setQuery(assistSql);
                var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                rightResult.setAssistData(assistData);
                rightResult.setDynamicAssistFields(rightAssistFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        var mixResult = (T) new ChartCalcDataResult();
        var data = new HashMap<String, Object>();
        data.put("left", leftResult);
        data.put("right", rightResult);
        mixResult.setData(data);
        mixResult.setContext(filterResult.getContext());
        return mixResult;
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        var leftCalcResult = (ChartCalcDataResult) calcResult.getData().get("left");
        var leftFields = new ArrayList<ChartViewFieldDTO>();
        leftFields.addAll(formatResult.getAxisMap().get(ChartAxis.xAxis));
        leftFields.addAll(formatResult.getAxisMap().get(ChartAxis.yAxis));
        mergeAssistField(leftCalcResult.getDynamicAssistFields(), leftCalcResult.getAssistData());
        var leftOriginData = leftCalcResult.getOriginData();
        var leftTable = ChartDataBuild.transTableNormal(leftFields, view, leftOriginData, desensitizationList);
        var leftData = new HashMap<String, Object>(leftTable);
        leftData.putAll(leftCalcResult.getData());
        leftData.put("dynamicAssistLines", leftCalcResult.getDynamicAssistFields());

        var rightCalcResult = (ChartCalcDataResult) calcResult.getData().get("right");
        var rightFields = new ArrayList<ChartViewFieldDTO>();

        var subAxisMap = (HashMap<ChartAxis, List<ChartViewFieldDTO>>) formatResult.getContext().get("subAxisMap");
        rightFields.addAll(subAxisMap.get(ChartAxis.xAxis));
        rightFields.addAll(subAxisMap.get(ChartAxis.yAxis));

        mergeAssistField(rightCalcResult.getDynamicAssistFields(), rightCalcResult.getAssistData());
        var rightOriginData = rightCalcResult.getOriginData();
        var rightTable = ChartDataBuild.transTableNormal(rightFields, view, rightOriginData, desensitizationList);
        var rightData = new HashMap<String, Object>(rightTable);
        rightData.putAll(rightCalcResult.getData());
        rightData.put("dynamicAssistLines", rightCalcResult.getDynamicAssistFields());

        // 构建结果
        Map<String, Object> chartData = new TreeMap<>();
        chartData.put("left", leftData);
        chartData.put("right", rightData);

        var drillFilters = filterResult.getFilterList().stream().filter(f -> f.getFilterType() == 1).collect(Collectors.toList());
        // 日期下钻替换回去
        drillFilters.forEach(f -> {
            if (CollectionUtils.isNotEmpty(f.getOriginValue())) {
                f.setValue(f.getOriginValue());
            }
        });
        var isDrill = CollectionUtils.isNotEmpty(drillFilters);
        view.setDrillFilters(drillFilters);
        view.setDrill(isDrill);
        view.setSql(leftCalcResult.getQuerySql());
        view.setData(chartData);
        return view;
    }
}
