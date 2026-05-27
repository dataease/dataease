package io.dataease.extensions.datasource.plugin;

import io.dataease.exception.DEException;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.ProtectionDomain;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class DatasourcePluginInfoLoader {

    private static final String PLUGIN_INFO_PATTERN = "^plugin/extensions.*\\.json$";

    private DatasourcePluginInfoLoader() {
    }

    public static DataEasePluginVO load(DataEasePlugin plugin) {
        ProtectionDomain protectionDomain = plugin.getClass().getProtectionDomain();
        try {
            URI uri = protectionDomain.getCodeSource().getLocation().toURI();
            File file = new File(uri);
            if (!file.isFile()) {
                return plugin.getPluginInfo();
            }
            try (JarFile jarFile = new JarFile(file)) {
                JarEntry pluginInfoEntry = findPluginInfoEntry(jarFile);
                Map<String, Object> pluginInfo = readPluginInfo(jarFile, pluginInfoEntry);
                String moduleName = readModuleName(pluginInfo);
                String icon = readIcon(jarFile, moduleName);
                pluginInfo.put("icon", icon);
                pluginInfo.put("config", JsonUtil.toJSONString(pluginInfo.get("config")));
                return JsonUtil.parseObject(JsonUtil.toJSONString(pluginInfo).toString(), DataEasePluginVO.class);
            }
        } catch (Exception e) {
            DEException.throwException(e);
            return null;
        }
    }

    private static JarEntry findPluginInfoEntry(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().matches(PLUGIN_INFO_PATTERN)) {
                return entry;
            }
        }
        DEException.throwException("插件缺失描述文件[extensions-*.json]");
        return null;
    }

    private static Map<String, Object> readPluginInfo(JarFile jarFile, JarEntry pluginInfoEntry) throws Exception {
        try (InputStream inputStream = jarFile.getInputStream(pluginInfoEntry)) {
            Map<String, Object> pluginInfo = JsonUtil.parseObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), Map.class);
            if (pluginInfo == null || pluginInfo.isEmpty()) {
                DEException.throwException("插件描述文件[extensions-*.json]内容为空");
            }
            return pluginInfo;
        }
    }

    private static String readModuleName(Map<String, Object> pluginInfo) {
        Object moduleName = pluginInfo.get("moduleName");
        if (moduleName == null || StringUtils.isBlank(moduleName.toString())) {
            DEException.throwException("插件描述文件[extensions-*.json]中缺失moduleName描述");
        }
        return moduleName.toString();
    }

    private static String readIcon(JarFile jarFile, String moduleName) throws Exception {
        String iconPath = String.format("plugin/%s.svg", moduleName);
        JarEntry iconEntry = jarFile.getJarEntry(iconPath);
        if (iconEntry == null) {
            DEException.throwException(String.format("插件缺失图标文件[%s]", iconPath));
        }
        try (InputStream inputStream = jarFile.getInputStream(iconEntry)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
