package io.dataease.chart.charts.impl.numeric;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NumericalChartHandler extends DefaultChartHandler {
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
        fillDatasourceRequest(datasourceRequest, ((DatasetGroupInfoDTO) formatResult.getContext().get("dataset")).getIsCross(), dsMap, sqlMap);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        boolean isdrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        Map<String, Object> result = ChartDataBuild.transNormalChartData(xAxis, yAxis, view, data, isdrill);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        return calcResult;
    }

    protected ChartViewFieldDTO getDynamicField(Map<String, Object> target, String type, String field) {
        String maxType = (String) target.get(type);
        if (StringUtils.equalsIgnoreCase("dynamic", maxType)) {
            Map<String, Object> maxField = (Map<String, Object>) target.get(field);
            if (maxField == null || maxField.get("id") == null || StringUtils.isBlank(maxField.get("id").toString())) {
                resetDynamicField(target, type, field);
                return null;
            }
            Long id;
            try {
                id = Long.valueOf(maxField.get("id").toString());
            } catch (NumberFormatException e) {
                resetDynamicField(target, type, field);
                return null;
            }
            String summary = (String) maxField.get("summary");
            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(id);
            if (ObjectUtils.isNotEmpty(datasetTableField)) {
                boolean isText = datasetTableField.getDeType() == 0 || datasetTableField.getDeType() == 1 || datasetTableField.getDeType() == 5;
                if (isText && !StringUtils.containsIgnoreCase(summary, "count")) {
                    DEException.throwException(Translator.get("i18n_gauge_field_change"));
                }
                ChartViewFieldDTO dto = new ChartViewFieldDTO();
                BeanUtils.copyBean(dto, datasetTableField);
                // 文本类型的计算字段时，判断originName是否包含表达式，如果包含，这里取消汇总，后续sql中会有默认表达式count,否则将会套一层count导致报错
                if (isText) {
                    String textSummary = (dto.getExtField() == 2 && StringUtils.isNotEmpty(dto.getOriginName()) &&
                            Pattern.compile("^(.*?)\\(\\[").matcher(dto.getOriginName()).find()) ? "" : "count";
                    dto.setSummary(textSummary);
                } else if (StringUtils.isEmpty(dto.getSummary())) {
                    dto.setSummary(summary);
                }
                return dto;
            } else {
                resetDynamicField(target, type, field);
            }
        }
        return null;
    }

    protected void resetDynamicField(Map<String, Object> target, String type, String field) {
        target.put(type, "fix");
        target.put(field, Map.of("id", "", "summary", ""));
        switch (type) {
            case "gaugeMinType":
                target.putIfAbsent("gaugeMin", 0);
                target.putIfAbsent("gaugeMin", 0);
                break;
            case "gaugeMaxType":
                target.putIfAbsent("gaugeMax", 100);
                target.putIfAbsent("gaugeMax", 100);
                break;
            case "liquidMaxType":
                target.putIfAbsent("liquidMax", 100);
                target.putIfAbsent("liquidMax", 100);
                break;
            default:
                break;
        }
    }
}
