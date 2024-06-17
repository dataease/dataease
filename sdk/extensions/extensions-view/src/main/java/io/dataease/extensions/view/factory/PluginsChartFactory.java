package io.dataease.extensions.view.factory;

import io.dataease.extensions.view.template.PluginsChartTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginsChartFactory {

    private static final Map<String, PluginsChartTemplate> templateMap = new ConcurrentHashMap<>();

    public static PluginsChartTemplate getInstance(String render, String type) {
        String key = render + "_" + type;
        return templateMap.get(key);
    }

    public static void loadTemplate(String render, String type, PluginsChartTemplate template) {
        String key = render + "_" + type;
        if (templateMap.containsKey(key)) return;
        templateMap.put(key, template);
    }

    public static List<String> getViewConfigList() {
        return templateMap.values().stream().map(PluginsChartTemplate::getConfig).toList();
    }

    public static List<String> getAllPlugins() {
        return null;
    }
}
