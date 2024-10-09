package io.dataease.chart.charts.impl.table;

import io.dataease.chart.charts.impl.GroupChartHandler;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TablePivotHandler extends GroupChartHandler {
    @Getter
    private String type = "table-pivot";

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        T result =  super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        Map<String, Object> customCalc = calcCustomExpr(view, filterResult, sqlMap, sqlMeta, provider);
        result.getData().put("customCalc", customCalc);
        try {
            var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
            var originSql = result.getQuerySql();
            var dynamicAssistFields = getDynamicThresholdFields(view);
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var assistFields = getAssistFields(dynamicAssistFields, yAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                req.setDsList(dsMap);
                var assistSql = assistSQL(originSql, assistFields);
                req.setQuery(assistSql);
                logger.debug("calcite assistSql sql: " + assistSql);
                var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                result.setAssistData(assistData);
                result.setDynamicAssistFields(dynamicAssistFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Map<String, Object> calcCustomExpr(ChartViewDTO view, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        Object totalStr = JsonUtil.toJSONString(view.getCustomAttr().get("tableTotal"));
        TableTotal tableTotal = JsonUtil.parseObject((String) totalStr, TableTotal.class);
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var rowAxis = view.getXAxis();
        var colAxis = view.getXAxisExt();
        var dataMap = new HashMap<String, Object>();
        if (CollectionUtils.isEmpty(rowAxis)) {
            return dataMap;
        }
        // 行总计，列维度聚合加上自定义字段
        var row = tableTotal.getRow();
        if (row.isShowGrandTotals()) {
            var yAxis = getCustomFields(view, row.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var result = getData(sqlMeta, colAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                var querySql = result.getT1();
                var data = result.getT2();
                var tmp = new HashMap<String, Object>();
                dataMap.put("rowTotal", tmp);
                tmp.put("data", buildCustomCalcResult(data, colAxis, yAxis));
                tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
            }
        }
        // 行小计，列维度聚合，自定义指标数 * (行维度的数量 - 1)
        if (row.isShowSubTotals()) {
            var yAxis = getCustomFields(view, row.getCalcSubTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("rowSubTotal", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if ( i == rowAxis.size() - 1) {
                        break;
                    }
                    var xAxis = new ArrayList<>(colAxis);
                    var subRowAxis = rowAxis.subList(0, i + 1);
                    xAxis.addAll(subRowAxis);
                    if (!yAxis.isEmpty()) {
                        var result = getData(sqlMeta, xAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                        var querySql = result.getT1();
                        var data = result.getT2();
                        var tmp = new HashMap<String, Object>();
                        tmp.put("data", buildCustomCalcResult(data, xAxis, yAxis));
                        tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
                        tmpData.add(tmp);
                    }
                }
            }
        }
        // 列总计，行维度聚合加上自定义字段
        var col = tableTotal.getCol();
        if (col.isShowGrandTotals() && CollectionUtils.isNotEmpty(colAxis)) {
            var yAxis = getCustomFields(view, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var result = getData(sqlMeta, rowAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                var querySql = result.getT1();
                var data = result.getT2();
                var tmp = new HashMap<String, Object>();
                dataMap.put("colTotal", tmp);
                tmp.put("data", buildCustomCalcResult(data, rowAxis, yAxis));
                tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
            }
        }
        // 列小计，行维度聚合，自定义指标数 * (列维度的数量 - 1)
        if (col.isShowSubTotals() && colAxis.size() >= 2) {
            var yAxis = getCustomFields(view, col.getCalcSubTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("colSubTotal", tmpData);
                for (int i = 0; i < colAxis.size(); i++) {
                    if ( i == colAxis.size() - 1) {
                        break;
                    }
                    var xAxis = new ArrayList<>(rowAxis);
                    var subColAxis = colAxis.subList(0, i + 1);
                    xAxis.addAll(subColAxis);
                    if (!yAxis.isEmpty()) {
                        var result = getData(sqlMeta, xAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                        var querySql = result.getT1();
                        var data = result.getT2();
                        var tmp = new HashMap<String, Object>();
                        tmp.put("data", buildCustomCalcResult(data, xAxis, yAxis));
                        tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
                        tmpData.add(tmp);
                    }
                }
            }
        }
        // 行列交叉部分总计，无聚合，直接算，用列总计公式
        if (row.isShowGrandTotals() && col.isShowGrandTotals()) {
            var yAxis = getCustomFields(view, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                // 清掉聚合轴
                var result = getData(sqlMeta, Collections.emptyList(), yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                var querySql = result.getT1();
                var data = result.getT2();
                var tmp = new HashMap<String, Object>();
                dataMap.put("rowColTotal", tmp);
                var tmpData = new HashMap<String, String>();
                for (int i = 0; i < yAxis.size(); i++) {
                    var a = yAxis.get(i);
                    tmpData.put(a.getDataeaseName(), data.getFirst()[i]);
                }
                tmp.put("data", tmpData);
                tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
            }
        }
        // 行总计里面的列小计
        if (row.isShowGrandTotals() && col.isShowSubTotals() && colAxis.size() >= 2) {
            var yAxis = getCustomFields(view, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("colSubInRowTotal", tmpData);
                for (int i = 0; i < colAxis.size(); i++) {
                    if ( i == colAxis.size() - 1) {
                        break;
                    }
                    var xAxis = colAxis.subList(0, i + 1);
                    var result = getData(sqlMeta, xAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                    var querySql = result.getT1();
                    var data = result.getT2();
                    var tmp = new HashMap<String, Object>();
                    tmp.put("data", buildCustomCalcResult(data, xAxis, yAxis));
                    tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
                    tmpData.add(tmp);
                }
            }
        }
        // 列总计里面的行小计
        if (col.isShowGrandTotals() && row.isShowGrandTotals() && rowAxis.size() >= 2) {
            var yAxis = getCustomFields(view, row.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("rowSubInColTotal", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if ( i == rowAxis.size() - 1) {
                        break;
                    }
                    var xAxis = rowAxis.subList(0, i + 1);
                    var result = getData(sqlMeta, xAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                    var querySql = result.getT1();
                    var data = result.getT2();
                    var tmp = new HashMap<String, Object>();
                    tmp.put("data", buildCustomCalcResult(data, xAxis, yAxis));
                    tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
                    tmpData.add(tmp);
                }
            }
        }
        // 行小计和列小计相交部分
        if (row.isShowSubTotals() && col.isShowSubTotals() && colAxis.size() >= 2 && rowAxis.size() >= 2) {
            var yAxis = getCustomFields(view, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<List<Map<String, Object>>>();
                dataMap.put("rowSubInColSub", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if ( i == rowAxis.size() - 1) {
                        break;
                    }
                    var tmpList = new ArrayList<Map<String, Object>>();
                    tmpData.add(tmpList);
                    var subRow = rowAxis.subList(0, i + 1);
                    var xAxis = new ArrayList<>(subRow);
                    for (int j = 0; j < colAxis.size(); j++) {
                        if ( j == colAxis.size() - 1) {
                            break;
                        }
                        var subCol = colAxis.subList(0, j + 1);
                        xAxis.addAll(subCol);
                        var result = getData(sqlMeta, xAxis, yAxis, allFields, crossDs, dsMap, view, provider, needOrder);
                        var querySql = result.getT1();
                        var data = result.getT2();
                        var tmp = new HashMap<String, Object>();
                        tmp.put("data", buildCustomCalcResult(data, xAxis, yAxis));
                        tmp.put("sql", Base64.getEncoder().encodeToString(querySql.getBytes()));
                        tmpList.add(tmp);
                    }
                }
            }
        }
        return dataMap;
    }

    private Map<String, Object> buildCustomCalcResult(List<String[]> data, List<ChartViewFieldDTO> dimAxis, List<ChartViewFieldDTO> quotaAxis) {
        var rootResult = new HashMap<String, Object>();
        if (CollectionUtils.isEmpty(dimAxis)) {
            var rowData = data.getFirst();
            for (int i = 0; i < rowData.length; i++) {
                var qAxis = quotaAxis.get(i);
                rootResult.put(qAxis.getDataeaseName(), rowData[i]);
            }
            return rootResult;
        }
        for (int i = 0; i < data.size(); i++) {
            var rowData = data.get(i);
            Map<String, Object> curSubMap = rootResult;
            for (int j = 0; j < dimAxis.size(); j++) {
                var tmpMap = curSubMap.get(rowData[j]);
                if (tmpMap == null) {
                    tmpMap = new HashMap<String, Object>();
                    curSubMap.put(rowData[j], tmpMap);
                    curSubMap = (Map<String, Object>) tmpMap;
                } else {
                    curSubMap = (Map<String, Object>) tmpMap;
                }
                if (j == dimAxis.size() - 1) {
                    for (int k = 0; k < quotaAxis.size(); k++) {
                        var qAxis = quotaAxis.get(k);
                        curSubMap.put(qAxis.getDataeaseName(), rowData[j + k + 1]);
                    }
                }
            }
        }
        return rootResult;
    }

    private Tuple2<String, List<String[]>> getData(SQLMeta sqlMeta, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis,
                           List<ChartViewFieldDTO> allFields, boolean crossDs, Map<Long, DatasourceSchemaDTO> dsMap,
                           ChartViewDTO view, Provider provider, boolean needOrder) {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        nullToBlank(data);
        return Tuples.of(querySql, data);
    }
    private void nullToBlank(List<String[]> data) {
        data.forEach(r -> {
            for (int i = 0; i < r.length; i++) {
                if (r[i] == null) {
                    r[i] = "";
                }
            }
        });
    }

    private List<ChartViewFieldDTO> getCustomFields(ChartViewDTO view, List<TableCalcTotalCfg> cfgList) {
        var quotaIds = view.getYAxis().stream().map(ChartViewFieldDTO::getDataeaseName).collect(Collectors.toSet());
        var customFields = new ArrayList<ChartViewFieldDTO>();
        for (TableCalcTotalCfg totalCfg : cfgList) {
            if (!quotaIds.contains(totalCfg.getDataeaseName())) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(totalCfg.getAggregation(), "CUSTOM")){
                var field = new ChartViewFieldDTO();
                field.setDeType(DeTypeConstants.DE_FLOAT);
                BeanUtils.copyBean(field, totalCfg);
                field.setId(IDUtils.snowID());
                field.setExtField(ExtFieldConstant.EXT_CALC);
                customFields.add(field);
            }
        }
        return customFields;
    }
}
