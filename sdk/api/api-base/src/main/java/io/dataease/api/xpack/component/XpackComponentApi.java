package io.dataease.api.xpack.component;

import io.dataease.api.xpack.component.vo.XpackMenuVO;
import io.dataease.extensions.datafilling.vo.XpackPluginsDfVO;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;
import io.dataease.extensions.view.vo.XpackPluginsViewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface XpackComponentApi {

    @GetMapping("/content/{name}")
    String content(@PathVariable("name") String name);

    @GetMapping("/contentPlugin/{name}")
    String pluginContent(@PathVariable("name") String name);

    @GetMapping("/menu")
    List<XpackMenuVO> menu();

    @GetMapping("/viewPlugins")
    List<XpackPluginsViewVO> viewPlugins();

    @GetMapping("/dsPlugins")
    List<XpackPluginsDatasourceVO> dsPlugins();

    @GetMapping("/dfPlugins")
    List<XpackPluginsDfVO> dfPlugins();

    @GetMapping("/syncPlugins")
    List<XpackPluginsSyncDatasourceVO> syncPlugins();

    @GetMapping("/pluginStaticInfo/{moduleName}")
    void pluginStaticInfo(@PathVariable("moduleName") String moduleName);
}
