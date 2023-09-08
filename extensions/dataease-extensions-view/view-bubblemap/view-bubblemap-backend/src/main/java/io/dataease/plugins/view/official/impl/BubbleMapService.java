package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.view.entity.PluginViewField;
import io.dataease.plugins.view.entity.PluginViewParam;
import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BubbleMapService extends ViewPluginService {

    private static final String VIEW_TYPE_VALUE = "buddle-map";
    private static final String[] VIEW_STYLE_PROPERTIES =
            {
                    "color-selector",
                    "label-selector",
                    "tooltip-selector",
                    "title-selector",
                    "suspension-selector"
            };

    private static final Map<String, String[]> VIEW_STYLE_PROPERTY_INNER = new HashMap();

    static {
        VIEW_STYLE_PROPERTY_INNER.put("color-selector", new String[]{"value", "alpha"});
        VIEW_STYLE_PROPERTY_INNER.put("label-selector", new String[]{"show", "fontSize", "color", "position", "formatter"});
        VIEW_STYLE_PROPERTY_INNER.put("tooltip-selector", new String[]{"show", "textStyle", "formatter"});
        VIEW_STYLE_PROPERTY_INNER.put("title-selector", new String[]{"show", "title", "fontSize", "color", "hPosition", "vPosition", "isItalic", "isBolder"});
        VIEW_STYLE_PROPERTY_INNER.put("suspension-selector", new String[]{"show"});
    }

    /*下版这些常量移到sdk*/
    private static final String TYPE = "-type";
    private static final String DATA = "-data";
    private static final String STYLE = "-style";
    private static final String VIEW = "-view";
    private static final String SUFFIX = "svg";
    /*下版这些常量移到sdk*/
    private static final String VIEW_TYPE = VIEW_TYPE_VALUE + TYPE;
    private static final String VIEW_DATA = VIEW_TYPE_VALUE + DATA;
    private static final String VIEW_STYLE = VIEW_TYPE_VALUE + STYLE;
    private static final String VIEW_VIEW = VIEW_TYPE_VALUE + VIEW;


    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("echarts");
        pluginViewType.setCategory("chart.chart_type_space");
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
        staticResource.setName("view-bubblemap-backend");
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
        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis)) {
            return null;
        }
        return super.generateSQL(param);
    }

}
