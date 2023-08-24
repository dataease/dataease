package io.dataease.auth.service;

import io.dataease.auth.entity.AuthItem;
import io.dataease.commons.model.AuthURD;

import java.util.List;
import java.util.Set;

public interface ExtAuthService {

    Set<Long> userIdsByRD(AuthURD request);

    AuthURD resourceTarget(String resourceId);

    List<AuthItem> dataSourceIdByUser(Long userId);
    List<AuthItem> dataSetIdByUser(Long userId);
    List<AuthItem> panelIdByUser(Long userId);

    List<AuthItem> dataSourceIdByRole(Long roleId);
    List<AuthItem> dataSetIdByRole(Long roleId);
    List<AuthItem> panelIdByRole(Long roleId);

    List<AuthItem> dataSourceIdByDept(Long deptId);
    List<AuthItem> dataSetIdByDept(Long deptId);
    List<AuthItem> panelIdByDept(Long deptId);

    void clearUserResource(Long userId);
    void clearDeptResource(Long deptId);
    void clearRoleResource(Long roleId);

    List<String> parentResource(String resourceId, String type);


}
