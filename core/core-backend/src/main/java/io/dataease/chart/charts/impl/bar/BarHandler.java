package io.dataease.chart.charts.impl.bar;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.utils.SqlUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.ExtWhere2Str;
import io.dataease.engine.utils.SQLUtils;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.view.dto.*;
import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.extensions.view.model.SQLMeta;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.JsonUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BarHandler extends YoyChartHandler {

    @Override
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), "bar", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "bar-horizontal", this);
    }

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var yAxis = result.getAxisMap().get(ChartAxis.yAxis);
        yAxis.addAll(view.getExtLabel());
        yAxis.addAll(view.getExtTooltip());
        result.getAxisMap().put(ChartAxis.extLabel, view.getExtLabel());
        result.getAxisMap().put(ChartAxis.extTooltip, view.getExtTooltip());
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, CalciteProvider provider) {
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
