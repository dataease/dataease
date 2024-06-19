package io.dataease.extensions.view.template;

import io.dataease.exception.DEException;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.factory.PluginsChartFactory;
import io.dataease.extensions.view.model.SQLMeta;
import io.dataease.extensions.view.vo.XpackPluginsViewVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;

import java.util.List;
import java.util.Map;

public abstract class PluginsChartTemplate implements DataEasePlugin {

    @Override
    public void loadPlugin() {
        XpackPluginsViewVO viewConfig = getConfig();
        PluginsChartFactory.loadTemplate(viewConfig.getRender(), viewConfig.getCategory(), this);
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


    public abstract Map<String, List<ChartViewFieldDTO>> formatChartAxis(ChartViewDTO view);


    public abstract ChartViewDTO calcResult(SQLMeta sqlMeta, List<ChartViewFieldDTO> xaxis, List<ChartViewFieldDTO> yaxis,
                                            List<DatasetTableFieldDTO> allFields, boolean crossDs, Map<Long, String> dsTypeMap);

}
