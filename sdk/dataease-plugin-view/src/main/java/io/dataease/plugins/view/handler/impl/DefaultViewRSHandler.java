package io.dataease.plugins.view.handler.impl;

import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.handler.PluginViewRSHandler;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认结果处理器
 */
public class DefaultViewRSHandler implements PluginViewRSHandler<Map> {


    /**
     * 默认结果处理器
     * 简单echarts数据
     * @param pluginViewParam
     * @param data
     * @param isDrill
     * @return
     */
    @Override
    public Map format(PluginViewParam pluginViewParam, List<String[]> data, boolean isDrill) {
        List<PluginViewField> xAxis = new ArrayList<>();
        List<PluginViewField> yAxis = new ArrayList<>();

        pluginViewParam.getPluginViewFields().forEach(pluginViewField -> {
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis")) {
                xAxis.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "yAxis")) {
                yAxis.add(pluginViewField);
            }
        });
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<PluginSeries> series = new ArrayList<>();
        for (PluginViewField y : yAxis) {
            PluginSeries series1 = new PluginSeries();
            series1.setName(y.getName());
            series1.setType(pluginViewParam.getPluginViewLimit().getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<PluginChartDimension> dimensionList = new ArrayList<>();
                List<PluginChartQuota> quotaList = new ArrayList<>();
                PluginAxisChartData axisChartDataDTO = new PluginAxisChartData();

                for (int j = 0; j < xAxis.size(); j++) {
                    PluginChartDimension chartDimensionDTO = new PluginChartDimension();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                PluginChartQuota chartQuotaDTO = new PluginChartQuota();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                series.get(j).getData().add(axisChartDataDTO);
            }
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }
}
