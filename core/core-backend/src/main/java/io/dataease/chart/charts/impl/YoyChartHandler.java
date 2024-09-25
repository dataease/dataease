package io.dataease.chart.charts.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.ExtWhere2Str;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 带同环比计算的图表处理器
 */
public class YoyChartHandler extends DefaultChartHandler {
    @Override
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var result = super.customFilter(view, filterList, formatResult);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        String originFilterJson = (String) JsonUtil.toJSONString(filterList);
        // 如果设置了同环比的指标字段设置了过滤器，那就需要把该过滤器的时间往前回调一年
        // 计算完同环比之后，再把计算结果和原有的过滤结果对比，去除不该出现的前一年的数据
        boolean yoyFiltered = checkYoyFilter(filterList, yAxis);
        if (yoyFiltered) {
            List<ChartExtFilterDTO> originFilter = JsonUtil.parseList(originFilterJson, new TypeReference<>() {
            });
            formatResult.getContext().put("originFilter", originFilter);
            formatResult.getContext().put("yoyFiltered", true);
        }
        return (T) result;
    }

    @Override
    public Map<String, Object> buildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        var yoyFiltered = filterResult.getContext().get("yoyFiltered") != null;
        // 带过滤同环比直接返回原始数据,再由视图重新组装
        if (yoyFiltered) {
            var result = new HashMap<String, Object>();
            result.put("data", data);
            return result;
        }
        return buildNormalResult(view, formatResult, filterResult, data);
    }

    /**
     * 构建同环比类型的数据
     *
     * @param view         视图对象
     * @param formatResult 处理后的轴
     * @param filterResult 处理后的过滤器
     * @param data         原始数据
     * @return 视图构建结果
     */
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        return super.buildResult(view, formatResult, filterResult, data);
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
        // 这里拿到的可能有一年前的数据
        var expandedResult = (T) super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        // 检查同环比过滤，拿到实际数据
        var yoyFiltered = filterResult.getContext().get("yoyFiltered") != null;
        if (yoyFiltered) {
            var originFilter = (List<ChartExtFilterDTO>) filterResult.getContext().get("originFilter");
            var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
            ExtWhere2Str.extWhere2sqlOjb(sqlMeta, originFilter, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
            var originSql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            originSql = provider.rebuildSQL(originSql, sqlMeta, crossDs, dsMap);
            var request = new DatasourceRequest();
            request.setDsList(dsMap);
            request.setQuery(originSql);
            logger.debug("calcite yoy sql: " + originSql);
            // 实际过滤后的数据
            var originData = (List<String[]>) provider.fetchResultField(request).get("data");
            List<String[]> resultData = new ArrayList<>();
            // 包含一年前的数据, 已计算同环比
            var yoyData = (List<String[]>) expandedResult.getData().get("data");
            var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
            // 对比维度,只保留实际过滤后的数据
            for (String[] yoyDataLine : yoyData) {
                StringBuilder x1 = new StringBuilder();
                for (int i = 0; i < xAxis.size(); i++) {
                    x1.append(yoyDataLine[i]);
                }
                for (String[] originDataLine : originData) {
                    StringBuilder x2 = new StringBuilder();
                    for (int i = 0; i < xAxis.size(); i++) {
                        x2.append(originDataLine[i]);
                    }
                    if (StringUtils.equals(x1, x2)) {
                        resultData.add(yoyDataLine);
                        break;
                    }
                }
            }
            yoyData.clear();
            yoyData.addAll(resultData);
            var result = this.buildNormalResult(view, formatResult, filterResult, yoyData);
            expandedResult.setData(result);
            expandedResult.setOriginData(resultData);
            expandedResult.setQuerySql(originSql);
        }
        // 同环比数据排序
        expandedResult.setOriginData(sortData(view, expandedResult.getOriginData(),formatResult));
        return expandedResult;
    }

    public static List<String[]> sortData(ChartViewDTO view, List<String[]> data, AxisFormatResult formatResult) {
        // 维度排序
        List<ChartViewFieldDTO> xAxisSortList = view.getXAxis().stream().filter(x -> !StringUtils.equalsIgnoreCase("none", x.getSort())).toList();
        // 指标排序
        List<ChartViewFieldDTO> yAxisSortList = view.getYAxis().stream().filter(y -> !StringUtils.equalsIgnoreCase("none", y.getSort())).toList();
        // 不包含维度排序时，指标排序生效
        if (!data.isEmpty() && CollectionUtils.isEmpty(xAxisSortList) && CollectionUtils.isNotEmpty(yAxisSortList)) {
            // 指标排序仅第一个生效
            ChartViewFieldDTO firstYAxis = yAxisSortList.getFirst();
            boolean asc = firstYAxis.getSort().equalsIgnoreCase("asc");
            // 维度指标
            List<ChartViewFieldDTO> allAxisList = new ArrayList<>();
            allAxisList.addAll(formatResult.getAxisMap().get(ChartAxis.xAxis));
            allAxisList.addAll(formatResult.getAxisMap().get(ChartAxis.yAxis));
            int index = findIndex(allAxisList, firstYAxis.getId());
            return sortData(data, asc, index);
        }
        return data;

    }

    public static List<String[]> sortData(List<String[]> data, boolean ascending, int index) {
        Comparator<String[]> comparator;
        if (ascending) {
            comparator = Comparator.comparing(item -> toBigDecimal(item[index]), Comparator.nullsFirst(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(item -> toBigDecimal(item[index]), Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return data.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static BigDecimal toBigDecimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value, e);
        }
    }

    public static int findIndex(List<ChartViewFieldDTO> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.equalsIgnoreCase(list.get(i).getId().toString(), id.toString())) {
                return i;
            }
        }
        return -1;
    }
}
