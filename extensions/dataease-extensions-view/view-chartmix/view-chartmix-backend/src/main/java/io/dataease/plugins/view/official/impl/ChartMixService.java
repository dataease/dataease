package io.dataease.plugins.view.official.impl;

import com.google.gson.Gson;
import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.official.handler.DefaultViewStatHandler;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChartMixService extends ViewPluginService {
    private static final String VIEW_TYPE_VALUE = "chart-mix";

    /* 下版这些常量移到sdk */
    private static final String TYPE = "-type";
    private static final String DATA = "-data";
    private static final String STYLE = "-style";
    private static final String VIEW = "-view";
    private static final String SUFFIX = "svg";
    /* 下版这些常量移到sdk */
    private static final String VIEW_TYPE = VIEW_TYPE_VALUE + TYPE;
    private static final String VIEW_DATA = VIEW_TYPE_VALUE + DATA;
    private static final String VIEW_STYLE = VIEW_TYPE_VALUE + STYLE;
    private static final String VIEW_VIEW = VIEW_TYPE_VALUE + VIEW;

    private static final String[] VIEW_STYLE_PROPERTIES = {
            "color-selector",
            "label-selector",
            "tooltip-selector-ant-v",
            "title-selector-ant-v"
    };

    private static final Map<String, String[]> VIEW_STYLE_PROPERTY_INNER = new HashMap<>();

    static {
        VIEW_STYLE_PROPERTY_INNER.put("color-selector", new String[]{"value"});
        VIEW_STYLE_PROPERTY_INNER.put("label-selector", new String[]{"show", "fontSize", "color"});
        VIEW_STYLE_PROPERTY_INNER.put("tooltip-selector-ant-v", new String[]{"show", "fontSize", "color", "backgroundColor"});
        VIEW_STYLE_PROPERTY_INNER.put("title-selector-ant-v", new String[]{"show", "title", "fontSize", "color", "hPosition", "vPosition", "isItalic", "isBolder"});
    }

    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("antv");
        pluginViewType.setCategory("chart.chart_type_trend");
        pluginViewType.setValue(VIEW_TYPE_VALUE);
        pluginViewType.setProperties(VIEW_STYLE_PROPERTIES);
        pluginViewType.setPropertyInner(VIEW_STYLE_PROPERTY_INNER);
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add(VIEW_VIEW);
        results.add(VIEW_DATA);
        results.add(VIEW_TYPE);
        results.add(VIEW_STYLE);
        return results;
    }

    @Override
    public List<StaticResource> staticResources() {
        List<StaticResource> results = new ArrayList<>();
        StaticResource staticResource = new StaticResource();
        staticResource.setName(VIEW_TYPE_VALUE);
        staticResource.setSuffix(SUFFIX);
        results.add(staticResource);
        results.add(pluginSvg());
        return results;
    }

    private StaticResource pluginSvg() {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("view-chartmix-backend");
        staticResource.setSuffix("svg");
        return staticResource;
    }


    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }

    @Override
    public String generateSQL(PluginViewParam param) {
        List<PluginViewField> xAxis = param.getFieldsByType("xAxis");
        List<PluginViewField> yAxis = param.getFieldsByType("yAxis");
        /*if (yAxis == null) {
            yAxis = new ArrayList<>();
        }
        List<PluginViewField> yAxisExt = param.getFieldsByType("yAxisExt");
        if (CollectionUtils.isNotEmpty(yAxisExt)) {
            yAxis.addAll(yAxisExt);
        }*/
        System.out.println(new Gson().toJson(yAxis));
        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis)) {
            return null;
        }
        String sql = new DefaultViewStatHandler().build(param, this);
        System.out.println(sql);
        return sql;

    }


    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {
        List<PluginViewField> xAxis = new ArrayList<>();
        List<PluginViewField> yAxis = new ArrayList<>();

        System.out.println("pluginViewParam: " + new Gson().toJson(pluginViewParam));

        pluginViewParam.getPluginViewFields().forEach(pluginViewField -> {
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis")) {
                xAxis.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "yAxis")) {
                yAxis.add(pluginViewField);
            }
        });
        Map<String, Object> map = new HashMap<>();

        List<PluginSeries> series = format(pluginViewParam.getPluginViewLimit().getType(), data, xAxis, yAxis);

        map.put("data", series);

        System.out.println(new Gson().toJson(map));

        return map;
    }

    private List<PluginSeries> format(String type, List<String[]> data, List<PluginViewField> xAxis, List<PluginViewField> yAxis) {
        List<PluginSeries> series = new ArrayList<>();
        for (PluginViewField y : yAxis) {
            PluginSeries series1 = new PluginSeries();
            series1.setName(y.getName());
            series1.setType(type);
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

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

        }
        return series;
    }

}
