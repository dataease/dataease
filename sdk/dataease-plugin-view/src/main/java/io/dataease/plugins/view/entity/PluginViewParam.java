package io.dataease.plugins.view.entity;

import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.view.entity.filter.PluginFilterTreeObj;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class PluginViewParam {

    private PluginViewSet pluginViewSet;

    private List<PluginViewField> pluginViewFields;

    private PluginFilterTreeObj pluginChartFieldCustomFilters;

    private List<PluginChartExtFilter> pluginChartExtFilters;

    private PluginViewLimit pluginViewLimit;

    private Long userId;

    private List<DataSetRowPermissionsTreeDTO> rowPermissionsTree;

    public List<PluginViewField> getFieldsByType(String type) {
        return pluginViewFields.stream().filter(field -> StringUtils.equals(field.getTypeField(), type)).collect(Collectors.toList());
    }
}
