package io.dataease.extensions.datafilling.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datafilling.factory.ExtDDLProviderFactory;
import io.dataease.extensions.datafilling.provider.ExtDDLProvider;
import io.dataease.extensions.datafilling.vo.XpackPluginsDfVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;

public abstract class DataFillingPlugin extends ExtDDLProvider implements DataEasePlugin {

    @Override
    public void loadPlugin() {
        XpackPluginsDfVO viewConfig = getConfig();
        ExtDDLProviderFactory.loadPlugin(viewConfig.getType(), this);
    }

    public XpackPluginsDfVO getConfig() {
        DataEasePluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        XpackPluginsDfVO vo = JsonUtil.parseObject(config, XpackPluginsDfVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    @Override
    public void unloadPlugin() {

    }
}
