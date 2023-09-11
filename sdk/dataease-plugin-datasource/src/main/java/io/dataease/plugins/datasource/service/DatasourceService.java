package io.dataease.plugins.datasource.service;

import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.common.service.PluginComponentService;

import java.util.List;

public abstract class DatasourceService extends PluginComponentService {

    abstract public DataSourceType getDataSourceType();
}
