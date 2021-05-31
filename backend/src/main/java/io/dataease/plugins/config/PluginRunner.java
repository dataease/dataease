package io.dataease.plugins.config;

import io.dataease.base.domain.MyPlugin;
import io.dataease.commons.utils.DeFileUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.service.sys.PluginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.List;

@Component
public class PluginRunner implements ApplicationRunner {

    @Value("${dataease.plugin.dir:/opt/dataease/plugins/}")
    private String pluginDir;

    @Autowired
    private PluginService pluginService;



    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 执行加载插件逻辑
        BaseGridRequest request = new BaseGridRequest();
        List<MyPlugin> plugins = pluginService.query(request);
        plugins.stream().forEach(plugin -> {
            String name = plugin.getName();
            String version = plugin.getVersion();
            String versionDir = pluginDir + name + "/" + version + "/";
            File fileDir = new File(versionDir);
            File[] jarFiles = fileDir.listFiles(this::isPluginJar);
            File jarFile = jarFiles[0];
            String jarPath = jarFile.getAbsolutePath();
            try {
                pluginService.loadJar(jarPath, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private boolean isPluginJar(File file) {
        String name = file.getName();
        return StringUtils.equals(DeFileUtils.getExtensionName(name), "jar");
    }
}
