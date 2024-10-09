package io.dataease.chart.charts.impl.table;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author jianneng
 * @date 2024/9/11 11:37
 **/
@Component
public class TableNormalHandler extends YoyChartHandler {
    @Getter
    private String type = "table-normal";

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        var result = (T) super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
        try {
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
}
