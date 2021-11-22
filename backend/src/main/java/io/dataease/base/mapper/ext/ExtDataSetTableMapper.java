package io.dataease.base.mapper.ext;

import io.dataease.base.domain.DatasetRowPermissions;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.dto.dataset.DataSetRowPermissionsDTO;
import io.dataease.dto.dataset.DataSetTableDTO;

import java.util.List;

public interface ExtDataSetTableMapper {
    List<DataSetTableDTO> search(DataSetTableRequest request);

    DataSetTableDTO searchOne(DataSetTableRequest request);

    List<DataSetRowPermissionsDTO> searchRowPermissons(GridExample example);

    List<RoleUserItem> searchAuthUsers(DatasetRowPermissions example);

    List<RoleUserItem> searchAuthRoles(DatasetRowPermissions example);

    List<RoleUserItem> searchAuthDepts(DatasetRowPermissions example);
}
