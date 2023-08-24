package io.dataease.plugins.view.service;

import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.view.entity.*;

import java.util.List;

public interface ViewPluginBaseService {


    PluginSingleField buildField(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj, int index);

    String customWhere(String dsType, List<PluginChartFieldCustomFilter> filters, PluginViewSQL tableObj);

    String panelWhere(String dsType, List<PluginChartExtFilter> pluginChartExtFilters, PluginViewSQL tableObj);

    String permissionWhere(String dsType, List<DataSetRowPermissionsTreeDTO> filters, PluginViewSQL tableObj);

    PluginViewSQL getTableObj(PluginViewSet pluginViewSet);

    String sqlLimit(String dsType, String sql, PluginViewLimit view);
}