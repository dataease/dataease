package io.dataease.chart.charts.impl.table;

import io.dataease.api.chart.dto.PageInfo;
import io.dataease.chart.charts.impl.DefaultChartHandler;
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
import io.dataease.utils.BeanUtils;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TableInfoHandler extends DefaultChartHandler {
    @Getter
    private String type = "table-info";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        result.getAxisMap().put(ChartAxis.yAxis, new ArrayList<>());
        return result;
    }

    @Override
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var chartExtRequest = view.getChartExtRequest();
        Map<String, Object> mapAttr = view.getCustomAttr();
        Map<String, Object> mapSize = (Map<String, Object>) mapAttr.get("basicStyle");
        var tablePageMode = (String) mapSize.get("tablePageMode");
        formatResult.getContext().put("tablePageMode", tablePageMode);
        if (StringUtils.equalsIgnoreCase(tablePageMode, "page") && !view.getIsExcelExport()) {
            if (chartExtRequest.getGoPage() == null) {
                chartExtRequest.setGoPage(1L);
            }
            if (chartExtRequest.getPageSize() == null) {
                int pageSize = (int) mapSize.get("tablePageSize");
                if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                    chartExtRequest.setPageSize(Math.min(pageSize, view.getResultCount().longValue()));
                } else {
                    chartExtRequest.setPageSize((long) pageSize);
                }
            }
        } else {
            if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                chartExtRequest.setGoPage(1L);
                chartExtRequest.setPageSize(view.getResultCount().longValue());
            } else if (!view.getIsExcelExport()) {
                chartExtRequest.setGoPage(null);
                chartExtRequest.setPageSize(null);
            }
        }
        return (T) new CustomFilterResult(filterList, formatResult.getContext());
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var chartExtRequest = view.getChartExtRequest();
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean crossDs = Utils.isCrossDs(dsMap);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        PageInfo pageInfo = new PageInfo();
        pageInfo.setGoPage(chartExtRequest.getGoPage());
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            pageInfo.setPageSize(Math.min(view.getResultCount() - (chartExtRequest.getGoPage() - 1) * chartExtRequest.getPageSize(), chartExtRequest.getPageSize()));
        } else {
            pageInfo.setPageSize(chartExtRequest.getPageSize());
        }
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        if(view.getIsExcelExport()){
            for (int i = 0; i < xAxis.size(); i++) {
                ChartViewFieldDTO fieldDTO = null;
                for (ChartViewFieldDTO allField : allFields) {
                    if (allField.getId().equals(xAxis.get(i).getId())) {
                        fieldDTO = allField;
                    }
                }
                assert fieldDTO != null;
                if (fieldDTO.isAgg()) {
                    sqlMeta.getXFields().get(i).setFieldName("'-'");
                }
            }
        }

        String originSql = SQLProvider.createQuerySQL(sqlMeta, false, true, view);// 明细表强制加排序
        String limit = ((pageInfo.getGoPage() != null && pageInfo.getPageSize() != null) ? " LIMIT " + pageInfo.getPageSize() + " OFFSET " + (pageInfo.getGoPage() - 1) * chartExtRequest.getPageSize() : "");
        var querySql = originSql + limit;

        var tablePageMode = (String) filterResult.getContext().get("tablePageMode");
        var totalPageSql = "SELECT COUNT(*) FROM (" + SQLProvider.createQuerySQLNoSort(sqlMeta, false, view) + ") COUNT_TEMP";
        if (StringUtils.isNotEmpty(totalPageSql) && StringUtils.equalsIgnoreCase(tablePageMode, "page")) {
            totalPageSql = provider.rebuildSQL(totalPageSql, sqlMeta, crossDs, dsMap);
            datasourceRequest.setQuery(totalPageSql);
            datasourceRequest.setTotalPageFlag(true);
            logger.debug("calcite total sql: " + totalPageSql);
            List<String[]> tmpData = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
            var totalItems = ObjectUtils.isEmpty(tmpData) ? 0 : Long.valueOf(tmpData.get(0)[0]);
            if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                totalItems = totalItems <= view.getResultCount() ? totalItems : view.getResultCount();
            }
            var totalPage = (totalItems / pageInfo.getPageSize()) + (totalItems % pageInfo.getPageSize() > 0 ? 1 : 0);
            view.setTotalItems(totalItems);
            view.setTotalPage(totalPage);
        }

        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        //自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, data);
        //数据重组逻辑可重载
        var result = this.buildResult(view, formatResult, filterResult, data);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        try {
            var dynamicAssistFields = getDynamicThresholdFields(view);
            Set<Long> fieldIds = xAxis.stream().map(ChartViewFieldDTO::getId).collect(Collectors.toSet());
            List<ChartViewFieldDTO> finalXAxis = xAxis;
            dynamicAssistFields.forEach(i -> {
                if (!fieldIds.contains(i.getFieldId())) {
                    ChartViewFieldDTO fieldDTO = new ChartViewFieldDTO();
                    BeanUtils.copyBean(fieldDTO, i.getCurField());
                    finalXAxis.add(fieldDTO);
                }
            });
            var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
            var assistFields = getAssistFields(dynamicAssistFields, yAxis, xAxis);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                var req = new DatasourceRequest();
                req.setDsList(dsMap);
                var assistSql = assistSQL(querySql, assistFields);
                req.setQuery(assistSql);
                logger.debug("calcite assistSql sql: " + assistSql);
                var assistData = (List<String[]>) provider.fetchResultField(req).get("data");
                calcResult.setAssistData(assistData);
                calcResult.setDynamicAssistFields(dynamicAssistFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calcResult;
    }
}
