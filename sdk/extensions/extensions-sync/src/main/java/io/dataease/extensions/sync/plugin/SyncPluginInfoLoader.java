package io.dataease.extensions.sync.plugin;

import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.vo.DataEasePluginVO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * 从插件自身代码位置读取描述和图标，兼容 JAR 及开发环境的类目录
 * 避免依赖应用类加载器可转为 URLClassLoader，与驱动覆盖选择无关
 */
final class SyncPluginInfoLoader {
    private SyncPluginInfoLoader() {
    }

    static DataEasePluginVO load(Class<?> pluginClass) throws Exception {
        Path location = Path.of(pluginClass.getProtectionDomain().getCodeSource().getLocation().toURI());
        String json;
        String icon;
        if (Files.isDirectory(location)) {
            List<Path> descriptions;
            try (var files = Files.list(location.resolve("plugin"))) {
                descriptions = files.filter(p -> p.getFileName().toString().matches("extensions.*\\.json")).toList();
            }
            if (descriptions.size() != 1) throw new IOException("插件描述文件数量应为一");
            json = Files.readString(descriptions.getFirst());
            Map<?, ?> info = JsonUtil.parseObject(json, Map.class);
            icon = Files.readString(location.resolve("plugin").resolve(moduleName(info) + ".svg"));
        } else {
            try (JarFile jar = new JarFile(location.toFile())) {
                var descriptions = jar.stream().filter(e -> e.getName().matches("plugin/extensions.*\\.json")).toList();
                if (descriptions.size() != 1) throw new IOException("插件描述文件数量应为一");
                try (var input = jar.getInputStream(descriptions.getFirst())) {
                    json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }
                Map<?, ?> info = JsonUtil.parseObject(json, Map.class);
                var entry = jar.getJarEntry("plugin/" + moduleName(info) + ".svg");
                if (entry == null) throw new IOException("插件缺失图标文件");
                try (var input = jar.getInputStream(entry)) {
                    icon = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        Map<String, Object> info = JsonUtil.parseObject(json, Map.class);
        info.put("icon", icon);
        info.put("config", JsonUtil.toJSONString(info.get("config")));
        return JsonUtil.parseObject(JsonUtil.toJSONString(info).toString(), DataEasePluginVO.class);
    }

    private static String moduleName(Map<?, ?> info) throws IOException {
        Object name = info.get("moduleName");
        if (!(name instanceof String value) || !value.matches("[A-Za-z0-9_-]+")) throw new IOException("无效的插件名称");
        return value;
    }
}
