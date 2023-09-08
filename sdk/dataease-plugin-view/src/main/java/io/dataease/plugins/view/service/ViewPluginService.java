package io.dataease.plugins.view.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.common.util.PluginSpringContextUtil;
import io.dataease.plugins.view.entity.PluginViewParam;
import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.handler.PluginViewRSHandler;
import io.dataease.plugins.view.handler.PluginViewStatHandler;
import io.dataease.plugins.view.handler.impl.DefaultViewRSHandler;
import io.dataease.plugins.view.handler.impl.DefaultViewStatHandler;
import java.util.List;
import java.util.Map;


public abstract class ViewPluginService extends PluginComponentService {

    protected ViewPluginBaseService viewPluginBaseService;

    private PluginViewStatHandler statHandler;

    private PluginViewRSHandler<Map> rsHandler;

    public abstract PluginViewType viewType();

    public abstract Object format(Object param);

    public String generateSQL(PluginViewParam param) {
        statHandler = new DefaultViewStatHandler();
        return statHandler.build(param, this);
    }

    public Map<String, Object> formatResult(PluginViewParam param, List<String[]> lists, Boolean isDrill) {
        rsHandler = new DefaultViewRSHandler();
        return rsHandler.format(param, lists, isDrill);
    }

    public ViewPluginBaseService getBaseService() {
        return viewPluginBaseService;
    }

    public ViewPluginService() {
        this.viewPluginBaseService = (ViewPluginBaseService)PluginSpringContextUtil.getBean("viewPluginBaseServiceImpl");
    }
}
