package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.dto.StaticResource;

import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.official.handler.SymbolMapRSHandler;
import io.dataease.plugins.view.official.handler.SymbolMapStatHandler;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

@Service
public class SymbolMapService extends ViewPluginService {

    private static Gson gson = new Gson();

    @Resource
    private SymbolMapStatHandler symbolMapStatHandler;

    @Resource
    private SymbolMapRSHandler symbolMapRSHandler;

    private static final String VIEW_TYPE_VALUE = "symbol-map";

    private static final String[] VIEW_STYLE_PROPERTIES = {
            "color-selector",
            "size-selector-ant-v",
            "tooltip-selector-ant-v",
            "title-selector-ant-v"
    };

    private static final Map<String, String[]> VIEW_STYLE_PROPERTY_INNER = new HashMap();

    static {
        VIEW_STYLE_PROPERTY_INNER.put("color-selector", new String[] { "value", "alpha" });
        VIEW_STYLE_PROPERTY_INNER.put("size-selector-ant-v",
                new String[] { "scatterSymbolSize", "symbolOpacity", "symbolStrokeWidth" });
        // VIEW_STYLE_PROPERTY_INNER.put("size-selector-ant-v", new
        // String[]{"scatterSymbol",
        // "scatterSymbolSize","symbolOpacity","symbolStrokeWidth"});
        VIEW_STYLE_PROPERTY_INNER.put("tooltip-selector-ant-v", new String[] { "show", "textStyle", "formatter" });
        VIEW_STYLE_PROPERTY_INNER.put("title-selector-ant-v", new String[] { "show", "title", "fontSize", "color",
                "hPosition", "vPosition", "isItalic", "isBolder" });
    }

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

    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("antv");
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
        addImage(results);
        results.add(pluginSvg());
        return results;
    }

    private StaticResource pluginSvg() {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("view-symbolmap-backend");
        staticResource.setSuffix("svg");
        return staticResource;
    }

    private void addImage(List<StaticResource> results) {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("map-marker");
        staticResource.setSuffix(SUFFIX);
        results.add(staticResource);
    }

    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }

    @Override
    public String generateSQL(PluginViewParam pluginViewParam) {
        Type tokenType = new TypeToken<List<PluginViewField>>() {
        }.getType();

        List<PluginViewField> pluginViewFields = gson.fromJson(gson.toJson(pluginViewParam.getPluginViewFields()),
                tokenType);
        List<String> trans2Ykeys = symbolMapRSHandler.getTrans2Ykeys();
        pluginViewParam.getPluginViewFields().forEach(field -> {
            if (trans2Ykeys.contains(field.getTypeField())) {
                field.setTypeField("yAxis");
            }
        });
        List<PluginViewField> xAxis = pluginViewParam.getFieldsByType("xAxis");
        List<PluginViewField> yAxis = pluginViewParam.getFieldsByType("yAxis");
        if (CollectionUtils.isEmpty(xAxis) || xAxis.size() < 2) {
            return null;
        }
        List<PluginViewField> xAxisExt = pluginViewParam.getFieldsByType("xAxisExt");
        if (CollectionUtils.isNotEmpty(xAxisExt)) {
            xAxisExt.forEach(i -> i.setTypeField("xAxis"));
        }
        if (CollectionUtils.isNotEmpty(yAxis)) {
            String generateSQL = super.generateSQL(pluginViewParam);
            pluginViewParam.setPluginViewFields(pluginViewFields);
            return generateSQL;
        }

        // 下面考虑符号大小为空的情况
        String result = symbolMapStatHandler.build(pluginViewParam, this);
        pluginViewParam.setPluginViewFields(pluginViewFields);
        return result;

    }

    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {
        return symbolMapRSHandler.format(pluginViewParam, data, isDrill);
    }
}
