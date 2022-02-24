package io.dataease.plugins.server.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.service.ViewPluginService;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RequestMapping("/plugin/view")
@RestController
public class PluginViewServer {

    @PostMapping("/types")
    public List<PluginViewType> types() {
        List<PluginViewType> result = new ArrayList<>();
        Map<String, ViewPluginService> beanMap = SpringContextUtil.getApplicationContext()
                .getBeansOfType(ViewPluginService.class);
        if (beanMap.keySet().size() == 0) {
            return result;
        }
        for (Entry<String, ViewPluginService> entry : beanMap.entrySet()) {
            result.add(entry.getValue().viewType());
        }
        return result;
    }
}
