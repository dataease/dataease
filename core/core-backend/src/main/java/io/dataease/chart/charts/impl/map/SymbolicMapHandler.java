package io.dataease.chart.charts.impl.map;

import io.dataease.chart.charts.impl.GroupChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.trans.Field2SQLObj;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.trans.Table2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SymbolicMapHandler extends GroupChartHandler {
    @Resource
    private DatasetDataManage datasetDataManage;

    @Getter
    private String type = "symbolic-map";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        result.getAxisMap().put(ChartAxis.extBubble, view.getExtBubble());
        return result;
    }


    private Map<String, Object> customBuildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data, List<ChartViewFieldDTO> detailFields, List<String[]> detailData) {
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var extBubble = formatResult.getAxisMap().get(ChartAxis.extBubble);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        return ChartDataBuild.transSymbolicMapNormalWithDetail(view, xAxis, yAxis, extBubble, data, detailFields, detailData);
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
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var extBubble = formatResult.getAxisMap().get(ChartAxis.extBubble);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        List<ChartViewFieldDTO> countField =chartViewManege.transFieldDTO(Collections.singletonList(chartViewManege.createCountField(view.getTableId())));
        List<DatasetTableFieldDTO> datasetTableFieldDTOList = FieldUtil.transFields(allFields);
        SQLMeta sqlMeta1 = new SQLMeta();
        BeanUtils.copyBean(sqlMeta1, sqlMeta);
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, datasetTableFieldDTOList, crossDs, dsMap, Utils.getParams(datasetTableFieldDTOList), view.getCalParams(), pluginManage);
        List<ChartViewFieldDTO> yAxis = new ArrayList<>();
        if(!extBubble.isEmpty() && !"*".equals(extBubble.get(0).getDataeaseName())){
            yAxis.addAll(extBubble);
        }
        yAxis.addAll(countField);
        datasetTableFieldDTOList.addAll(FieldUtil.transFields(countField));
        formatResult.getAxisMap().put(ChartAxis.yAxis,countField);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, datasetTableFieldDTOList, crossDs, dsMap, Utils.getParams(datasetTableFieldDTOList), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        // 获取所有字段数据作为数据详情返回
        List<String[]> detailData = new ArrayList<>();
        List<Long> xAxisIds = xAxis.stream().map(ChartViewFieldDTO::getId).toList();
        List<ChartViewFieldDTO> detailFields = new ArrayList<>();
        detailFields.addAll(xAxis);
        detailFields.addAll(allFields.stream().filter(field -> !xAxisIds.contains(field.getId())).toList());
        if (ObjectUtils.isNotEmpty(detailFields)) {
            List<DatasetTableFieldDTO> allFieldsTmp = FieldUtil.transFields(detailFields);
            datasetDataManage.buildFieldName(sqlMap, allFieldsTmp);
            String sql = (String) sqlMap.get("sql");
            sql = Utils.replaceSchemaAlias(sql, dsMap);
            SQLMeta sqlMeta2 = new SQLMeta();
            Table2SQLObj.table2sqlobj(sqlMeta2, null, "(" + sql + ")", crossDs);
            Field2SQLObj.field2sqlObj(sqlMeta2, allFieldsTmp, allFieldsTmp, crossDs, dsMap, Utils.getParams(allFieldsTmp), null, pluginManage);// todo chartParam从视图里取
            String querySQL;
            querySQL = SQLProvider.createQuerySQL(sqlMeta2, false, needOrder, false);
            querySQL = provider.rebuildSQL(querySQL, sqlMeta2, crossDs, dsMap);
            logger.debug("calcite data preview sql: " + querySQL);
            // 调用数据源的calcite获得data
            DatasourceRequest datasourceRequest1 = new DatasourceRequest();
            datasourceRequest1.setQuery(querySQL);
            datasourceRequest1.setDsList(dsMap);
            detailData = (List<String[]>) provider.fetchResultField(datasourceRequest1).get("data");
        }
        //自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, data);
        //数据重组逻辑可重载
        var result = customBuildResult(view, formatResult, filterResult, data, detailFields, detailData);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        formatResult.getAxisMap().put(ChartAxis.yAxis,new ArrayList<>());
        return calcResult;
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var extBubble = formatResult.getAxisMap().get(ChartAxis.extBubble);
        // 如果是表格导出查询 则在此处直接就可以返回
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        if (view.getIsExcelExport()) {
            Map<String, Object> sourceInfo = ChartDataBuild.transTableNormal(xAxis, extBubble, view, calcResult.getOriginData(), extStack, desensitizationList);
            sourceInfo.put("sourceData", calcResult.getOriginData());
            view.setData(sourceInfo);
            return view;
        }
        // 图表组件可再扩展
        Map<String, Object> mapTableNormal = calcResult.getData();
        var drillFilters = filterResult.getFilterList().stream().filter(f -> f.getFilterType() == 1).collect(Collectors.toList());
        var isDrill = CollectionUtils.isNotEmpty(drillFilters);
        // 构建结果
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.putAll(calcResult.getData());
        dataMap.putAll(mapTableNormal);
        dataMap.put("sourceFields", allFields);
        mergeAssistField(calcResult.getDynamicAssistFields(), calcResult.getAssistData());
        dataMap.put("dynamicAssistLines", calcResult.getDynamicAssistFields());
        view.setData(dataMap);
        view.setSql(Base64.getEncoder().encodeToString(calcResult.getQuerySql().getBytes()));
        view.setDrill(isDrill);
        view.setDrillFilters(drillFilters);
        return view;
    }
}
