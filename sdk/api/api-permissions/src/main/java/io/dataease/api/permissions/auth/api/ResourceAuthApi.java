package io.dataease.api.permissions.auth.api;

import io.dataease.api.permissions.auth.dto.ResourcePermissionRequest;
import io.dataease.api.permissions.auth.vo.ResourcePermissionVO;

import java.util.List;

public interface ResourceAuthApi {

    List<ResourcePermissionVO> queryResourcePermission(ResourcePermissionRequest request);
}
