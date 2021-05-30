package io.dataease.plugins.util;

import io.dataease.plugins.common.dto.PluginSysMenu;
import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.config.SpringContextUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginUtils {

    public static List<PluginSysMenu> pluginMenus() {
        Map<String, PluginMenuService> pluginMenuServiceMap = SpringContextUtil.getApplicationContext().getBeansOfType(PluginMenuService.class);
        List<PluginSysMenu> menus = pluginMenuServiceMap.values().stream().flatMap(item -> item.menus().stream()).collect(Collectors.toList());
        return menus;
    }




}
