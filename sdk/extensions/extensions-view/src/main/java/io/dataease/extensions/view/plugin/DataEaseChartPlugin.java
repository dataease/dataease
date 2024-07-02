package io.dataease.extensions.view.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.view.factory.PluginsChartFactory;
import io.dataease.extensions.view.vo.XpackPluginsViewVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;

public abstract class DataEaseChartPlugin extends AbstractChartPlugin implements DataEasePlugin {

    @Override
    public void loadPlugin() {
        XpackPluginsViewVO viewConfig = getConfig();
        PluginsChartFactory.loadPlugin(viewConfig.getRender(), viewConfig.getChartValue(), this);
    }

    public XpackPluginsViewVO getConfig() {
        DataEasePluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        XpackPluginsViewVO vo = JsonUtil.parseObject(config, XpackPluginsViewVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }
}
