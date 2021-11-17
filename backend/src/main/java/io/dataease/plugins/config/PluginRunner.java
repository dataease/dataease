package io.dataease.plugins.config;

import io.dataease.base.domain.MyPlugin;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.service.sys.PluginService;
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
    public void run(ApplicationArguments args) {
        // 执行加载插件逻辑
        BaseGridRequest request = new BaseGridRequest();
        List<MyPlugin> plugins = pluginService.query(request);
        plugins.stream().forEach(plugin -> {
            String store = plugin.getStore();
            String version = plugin.getVersion();
            String moduleName = plugin.getModuleName();
            String fileName = moduleName + "-" + version + ".jar";
            String path = pluginDir + store + "/" + fileName;

            File jarFile = new File(path);


            String jarPath = jarFile.getAbsolutePath();
            try {
                if (jarFile.exists()) {
                    pluginService.loadJar(jarPath, plugin);
                }else {
                    LogUtil.error("插件路径不存在 {} ", jarPath);
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });

    }

}
