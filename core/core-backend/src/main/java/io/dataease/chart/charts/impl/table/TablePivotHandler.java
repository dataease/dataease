package io.dataease.chart.charts.impl.table;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.chart.charts.impl.GroupChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.constant.DeTypeConstants;
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
import io.dataease.utils.LogUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
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
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) formatResult.getContext().get("desensitizationList");
        if (MapUtils.isNotEmpty(desensitizationList)) {
            formatResult.getAxisMap().forEach((axis, fields) -> {
                // 透视表指标先参与后端计算，返回时再脱敏，其他轴仍过滤脱敏字段。
                if (axis != ChartAxis.yAxis) {
                    fields.removeIf(field -> desensitizationList.containsKey(field.getDataeaseName()));
                }
            });
        }

        // 保留同环比过滤流程，在扩展时间范围之前保存原始过滤条件。
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        String originFilterJson = (String) JsonUtil.toJSONString(filterList);
        if (checkYoyFilter(filterList, yAxis)) {
            List<ChartExtFilterDTO> originFilter = JsonUtil.parseList(originFilterJson, new TypeReference<>() {
            });
            formatResult.getContext().put("originFilter", originFilter);
            formatResult.getContext().put("yoyFiltered", true);
        }
        return (T) new CustomFilterResult(filterList, formatResult.getContext());
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        T result = super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        Map<String, Object> customCalc = calcCustomExpr(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        boolean crossDs = ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross();
        result.getData().put("customCalc", customCalc);
        try {
            var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
            var originSql = result.getQuerySql();
            var dynamicAssistFields = getDynamicThresholdFields(view);
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
            var dimAxis = new ArrayList<>(xAxis);
            var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
            if (xAxisExt != null) {
                dimAxis.addAll(xAxisExt);
            }
            var allowedFieldIds = new HashSet<Long>();
            yAxis.forEach(field -> allowedFieldIds.add(field.getId()));
            dimAxis.forEach(field -> allowedFieldIds.add(field.getId()));
            dynamicAssistFields = dynamicAssistFields.stream().filter(field -> allowedFieldIds.contains(field.getFieldId())).toList();
            // 按阈值配置顺序生成查询列，确保返回值和脱敏规则对应同一个字段。
            var assistFields = getAssistFields(dynamicAssistFields, yAxis, dimAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                fillDatasourceRequest(req, crossDs, dsMap, sqlMap);

                List<ChartSeniorAssistDTO> assists = dynamicAssistFields.stream().filter(ele -> !StringUtils.equalsIgnoreCase(ele.getSummary(), "last_item")).toList();
                if (ObjectUtils.isNotEmpty(assists)) {
                    var assistSql = assistSQL(originSql, assistFields, dsMap, crossDs);
                    req.setQuery(assistSql);
                    logger.debug("calcite assistSql sql: " + assistSql);
                    var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                    var fields = assistFields.stream().filter(field -> !StringUtils.equalsIgnoreCase(field.getSummary(), "last_item")).toList();
                    desensitizeData(assistData, fields, 0, desensitizationList);
                    result.setAssistData(assistData);
                    result.setDynamicAssistFields(assists);
                }

                List<ChartSeniorAssistDTO> assistsOriginList = dynamicAssistFields.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getSummary(), "last_item")).toList();
                if (ObjectUtils.isNotEmpty(assistsOriginList)) {
                    var assistSqlOriginList = assistSQLOriginList(originSql, assistFields, dsMap, crossDs);
                    req.setQuery(assistSqlOriginList);
                    logger.debug("calcite assistSql sql origin list: " + assistSqlOriginList);
                    var assistDataOriginList = (List<String[]>) provider.fetchResultField(req).get("data");
                    var fields = assistFields.stream().filter(field -> StringUtils.equalsIgnoreCase(field.getSummary(), "last_item")).toList();
                    desensitizeData(assistDataOriginList, fields, 0, desensitizationList);
                    result.setAssistDataOriginList(assistDataOriginList);
                    result.setDynamicAssistFieldsOriginList(assistsOriginList);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return result;
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        // 使用当前用户的实际权限覆盖历史标记，供前端汇总、格式化和导出使用。
        for (List<ChartViewFieldDTO> fields : formatResult.getAxisMap().values()) {
            for (ChartViewFieldDTO field : fields) {
                field.setDesensitized(desensitizationList.containsKey(field.getDataeaseName()));
            }
        }
        return super.buildChart(view, calcResult, formatResult, filterResult);
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        // 同环比过滤结束后由 tableRow 统一返回脱敏数据，不额外返回未脱敏的图表序列。
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> buildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        var result = new HashMap<String, Object>();
        var yoyFiltered = filterResult.getContext().get("yoyFiltered") != null;
        // 带过滤同环比直接返回原始数据,再由视图重新组装
        if (yoyFiltered) {
            result.put("data", data);
        }
        return result;
    }

    private Map<String, Object> calcCustomExpr(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        Object totalStr = JsonUtil.toJSONString(view.getCustomAttr().get("tableTotal"));
        TableTotal tableTotal = JsonUtil.parseObject((String) totalStr, TableTotal.class);
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross();
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        // 自定义汇总沿用本次查询通过权限过滤的字段，避免重新引入禁用列。
        var dimensionNames = formatResult.getAxisMap().get(ChartAxis.xAxis).stream().map(ChartViewFieldDTO::getDataeaseName).collect(Collectors.toSet());
        var rowAxis = view.getXAxis().stream().filter(field -> dimensionNames.contains(field.getDataeaseName())).toList();
        var colAxis = view.getXAxisExt().stream().filter(field -> dimensionNames.contains(field.getDataeaseName())).toList();
        var quotaAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var dataMap = new HashMap<String, Object>();
        if (CollectionUtils.isEmpty(rowAxis)) {
            return dataMap;
        }
        // 行总计，列维度聚合加上自定义字段
        var row = tableTotal.getRow();
        if (row.isShowGrandTotals()) {
            var yAxis = getCustomFields(quotaAxis, row.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpList = new ArrayList<>(allFields);
                tmpList.addAll(yAxis);
                var result = getData(sqlMeta, colAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, row.getCalcSubTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("rowSubTotal", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if (i == rowAxis.size() - 1) {
                        break;
                    }
                    var xAxis = new ArrayList<>(colAxis);
                    var subRowAxis = rowAxis.subList(0, i + 1);
                    xAxis.addAll(subRowAxis);
                    if (!yAxis.isEmpty()) {
                        var tmpList = new ArrayList<>(allFields);
                        tmpList.addAll(yAxis);
                        var result = getData(sqlMeta, xAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpList = new ArrayList<>(allFields);
                tmpList.addAll(yAxis);
                var result = getData(sqlMeta, rowAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, col.getCalcSubTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("colSubTotal", tmpData);
                for (int i = 0; i < colAxis.size(); i++) {
                    if (i == colAxis.size() - 1) {
                        break;
                    }
                    var xAxis = new ArrayList<>(rowAxis);
                    var subColAxis = colAxis.subList(0, i + 1);
                    xAxis.addAll(subColAxis);
                    if (!yAxis.isEmpty()) {
                        var tmpList = new ArrayList<>(allFields);
                        tmpList.addAll(yAxis);
                        var result = getData(sqlMeta, xAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                // 清掉聚合轴
                var tmpList = new ArrayList<>(allFields);
                tmpList.addAll(yAxis);
                var result = getData(sqlMeta, Collections.emptyList(), yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("colSubInRowTotal", tmpData);
                for (int i = 0; i < colAxis.size(); i++) {
                    if (i == colAxis.size() - 1) {
                        break;
                    }
                    var tmpList = new ArrayList<>(allFields);
                    tmpList.addAll(yAxis);
                    var xAxis = colAxis.subList(0, i + 1);
                    var result = getData(sqlMeta, xAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
        if (col.isShowGrandTotals() && row.isShowSubTotals() && rowAxis.size() >= 2) {
            var yAxis = getCustomFields(quotaAxis, row.getCalcSubTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<Map<String, Object>>();
                dataMap.put("rowSubInColTotal", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if (i == rowAxis.size() - 1) {
                        break;
                    }
                    var tmpList = new ArrayList<>(allFields);
                    tmpList.addAll(yAxis);
                    var xAxis = rowAxis.subList(0, i + 1);
                    var result = getData(sqlMeta, xAxis, yAxis, tmpList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
            var yAxis = getCustomFields(quotaAxis, col.getCalcTotals().getCfg());
            if (!yAxis.isEmpty()) {
                var tmpData = new ArrayList<List<Map<String, Object>>>();
                dataMap.put("rowSubInColSub", tmpData);
                for (int i = 0; i < rowAxis.size(); i++) {
                    if (i == rowAxis.size() - 1) {
                        break;
                    }
                    var tmpList = new ArrayList<Map<String, Object>>();
                    tmpData.add(tmpList);
                    var subRow = rowAxis.subList(0, i + 1);
                    for (int j = 0; j < colAxis.size(); j++) {
                        if (j == colAxis.size() - 1) {
                            break;
                        }
                        var xAxis = new ArrayList<>(subRow);
                        var subCol = colAxis.subList(0, j + 1);
                        xAxis.addAll(subCol);
                        var tmpAllList = new ArrayList<>(allFields);
                        tmpAllList.addAll(yAxis);
                        var result = getData(sqlMeta, xAxis, yAxis, tmpAllList, crossDs, dsMap, view, provider, needOrder, sqlMap, desensitizationList);
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
                                                   ChartViewDTO view, Provider provider, boolean needOrder, Map<String, Object> sqlMap,
                                                   Map<String, ColumnPermissionItem> desensitizationList) {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        fillDatasourceRequest(datasourceRequest, crossDs, dsMap, sqlMap);
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        nullToBlank(data);
        // 自定义总计、小计在后端计算完成后脱敏，包含行列交叉汇总。
        desensitizeData(data, yAxis, xAxis.size(), desensitizationList);
        return Tuples.of(querySql, data);
    }

    private void desensitizeData(List<String[]> data, List<ChartViewFieldDTO> fields, int offset,
                                Map<String, ColumnPermissionItem> desensitizationList) {
        for (int i = 0; i < fields.size(); i++) {
            var permission = desensitizationList.get(fields.get(i).getDataeaseName());
            if (permission == null) {
                continue;
            }
            int columnIndex = offset + i;
            for (String[] row : data) {
                if (columnIndex < row.length) {
                    row[columnIndex] = ChartDataBuild.desensitizationValue(permission, row[columnIndex]);
                }
            }
        }
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

    private List<ChartViewFieldDTO> getCustomFields(List<ChartViewFieldDTO> quotaAxis, List<TableCalcTotalCfg> cfgList) {
        var quotaIds = quotaAxis.stream().map(ChartViewFieldDTO::getDataeaseName).collect(Collectors.toSet());
        var customFields = new ArrayList<ChartViewFieldDTO>();
        for (TableCalcTotalCfg totalCfg : cfgList) {
            if (!quotaIds.contains(totalCfg.getDataeaseName())) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(totalCfg.getAggregation(), "CUSTOM")) {
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
