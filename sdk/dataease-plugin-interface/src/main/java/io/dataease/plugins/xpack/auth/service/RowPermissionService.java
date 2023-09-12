package io.dataease.plugins.xpack.auth.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.auth.dto.request.DataSetRowPermissionsDTO;
import io.dataease.plugins.xpack.auth.dto.request.DatasetRowPermissions;

import java.util.List;

public abstract class RowPermissionService extends PluginComponentService {
    public abstract List<DataSetRowPermissionsDTO> searchRowPermissions(DataSetRowPermissionsDTO request);
    public abstract List<DataSetRowPermissionsDTO> queryRowPermissions(String tableId) ;
    public abstract void save(DatasetRowPermissions datasetRowPermissions);
    public abstract void delete(String id);
    public abstract List<? extends Object> authObjs(DataSetRowPermissionsDTO request);
    public abstract DataSetRowPermissionsDTO dataSetRowPermissionInfo(DataSetRowPermissionsDTO datasetRowPermissions);

}
