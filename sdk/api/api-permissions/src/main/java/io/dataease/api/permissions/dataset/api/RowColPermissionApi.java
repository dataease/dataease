package io.dataease.api.permissions.dataset.api;

import io.dataease.api.permissions.dataset.vo.RowColPermissionItem;

import java.util.List;

public interface RowColPermissionApi {

    List<RowColPermissionItem> query(List<Long> datasetIds);
}
