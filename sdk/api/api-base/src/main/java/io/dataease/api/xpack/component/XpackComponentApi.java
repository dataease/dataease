package io.dataease.api.xpack.component;

import io.dataease.api.xpack.component.vo.XpackMenuVO;
import io.dataease.api.xpack.component.vo.XpackPluginsViewVO;
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
}
