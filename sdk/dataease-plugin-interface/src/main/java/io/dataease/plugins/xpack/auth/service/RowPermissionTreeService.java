package io.dataease.plugins.xpack.auth.service;

import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeRequest;
import io.dataease.plugins.common.service.PluginComponentService;

import java.util.List;

public abstract class RowPermissionTreeService extends PluginComponentService {
    public abstract List<DataSetRowPermissionsTreeDTO> list(DatasetRowPermissionsTreeRequest request);

    public abstract DataSetRowPermissionsTreeDTO get(DataSetRowPermissionsTreeDTO request);

    public abstract void save(DataSetRowPermissionsTreeDTO request);

    public abstract void delete(String id);

    public abstract void mergeOldPermissions();
}
