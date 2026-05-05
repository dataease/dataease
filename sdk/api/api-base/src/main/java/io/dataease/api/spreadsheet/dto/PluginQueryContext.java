package io.dataease.api.spreadsheet.dto;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginQueryContext {
    private Provider provider;
    PluginQueryRequest request;
    private List<ChartViewFieldDTO> allFields;
    private SQLMeta sqlMeta;
    private Map<String, Object> sqlMap;
    private DatasetGroupInfoDTO table;
    private PluginManageApi pluginManage;
}
