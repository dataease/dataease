package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.view.entity.PluginViewField;
import io.dataease.plugins.view.entity.PluginViewParam;
import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.official.handler.RaceBarViewStatHandler;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RaceBarService extends ViewPluginService {
    private static final String VIEW_TYPE_VALUE = "race-bar";

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
            "tooltip-selector",
            "title-selector",
    };

    private static final Map<String, String[]> VIEW_STYLE_PROPERTY_INNER = new HashMap<>();

    static {
        VIEW_STYLE_PROPERTY_INNER.put("color-selector", new String[]{"value", "alpha"});
        VIEW_STYLE_PROPERTY_INNER.put("label-selector", new String[]{"show", "fontSize", "color", "position", "formatter"});
        VIEW_STYLE_PROPERTY_INNER.put("tooltip-selector", new String[]{"show", "textStyle", "formatter"});
        VIEW_STYLE_PROPERTY_INNER.put("title-selector", new String[]{"show", "title", "fontSize", "color", "hPosition", "vPosition", "isItalic", "isBolder"});
    }

    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("echarts");
        pluginViewType.setCategory("chart.chart_type_compare");
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
        staticResource.setName("view-racebar-backend");
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

        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis) || xAxis.size() < 2) {
            return null;
        }
        String sql = new RaceBarViewStatHandler().build(param, this);
        return sql;

    }

    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {
        return format(pluginViewParam, data, isDrill);
    }


    public Map format(PluginViewParam pluginViewParam, List<String[]> data, boolean isDrill) {

        Map<String, Object> map = new HashMap<>();

        map.put("data", data);

        Map<String, Integer> encode = new HashMap<>();

        String type = null;

        for (int i = 0; i < pluginViewParam.getPluginViewFields().size(); i++) {
            PluginViewField p = pluginViewParam.getPluginViewFields().get(i);
            if (StringUtils.equals(p.getTypeField(), "yAxis")) {
                encode.put("x", i);
                type = p.getType();
            } else if (StringUtils.equals(p.getTypeField(), "xAxis")) {
                if (p.getExtField() == 1) {
                    map.put("extIndex", i);
                } else {
                    encode.put("y", i);
                }
            }
        }
        map.put("encode", encode);

        Set<Object> xs = new HashSet<>();
        data.forEach(ss -> {
            xs.add(ss[encode.get("y")]);
        });

        Map<String, List<String[]>> groupData = data.stream().collect(Collectors.toMap(
                k -> k[(Integer) map.get("extIndex")],
                v -> {
                    List<String[]> list = new ArrayList<>();
                    list.add(v);
                    return list;
                },
                (oldList, newList) -> {
                    oldList.addAll(newList);
                    return oldList;
                })
        );

        for (String key : groupData.keySet()) {
            String finalType = type;
            groupData.put(key, groupData.get(key).stream().sorted((o1, o2) -> {
                if (StringUtils.equals(finalType, "LONG")) {
                    return Long.valueOf(o2[encode.get("x")]).compareTo(Long.valueOf(o1[encode.get("x")]));
                } else if (StringUtils.equals(finalType, "DOUBLE")) {
                    return Double.valueOf(o2[encode.get("x")]).compareTo(Double.valueOf(o1[encode.get("x")]));
                }
                return o2[encode.get("x")].compareTo(o1[encode.get("x")]);
            }).collect(Collectors.toList()));

        }

        map.put("groupData", groupData);

        map.put("extXs", new ArrayList<>(groupData.keySet()).stream().sorted().collect(Collectors.toList()));

        map.put("xs", new ArrayList<>(xs).stream().sorted().collect(Collectors.toList()));

        return map;
    }

}
