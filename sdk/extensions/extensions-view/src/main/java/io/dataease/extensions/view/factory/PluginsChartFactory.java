package io.dataease.extensions.view.factory;

import io.dataease.exception.DEException;
import io.dataease.extensions.view.plugin.AbstractChartPlugin;
import io.dataease.extensions.view.vo.XpackPluginsViewVO;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.license.utils.LogUtil;
import io.dataease.plugins.factory.DataEasePluginFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginsChartFactory {

    private static final Map<String, AbstractChartPlugin> templateMap = new ConcurrentHashMap<>();


    public static AbstractChartPlugin getInstance(String render, String type) {
        if (!LicenseUtil.licenseValid()) DEException.throwException("插件功能只对企业版本可用！");
        String key = render + "_" + type;
        return templateMap.get(key);
    }

    public static void loadPlugin(String render, String type, AbstractChartPlugin template) {
        if (!LicenseUtil.licenseValid()) DEException.throwException("插件功能只对企业版本可用！");
        String key = render + "_" + type;
        if (templateMap.containsKey(key)) return;
        templateMap.put(key, template);
        try {
            String moduleName = template.getPluginInfo().getModuleName();
            DataEasePluginFactory.loadTemplate(moduleName, template);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            DEException.throwException(e);
        }
    }

    public static List<XpackPluginsViewVO> getViewConfigList() {
        if (!LicenseUtil.licenseValid()) DEException.throwException("插件功能只对企业版本可用！");
        return templateMap.values().stream().map(AbstractChartPlugin::getConfig).toList();
    }
}
