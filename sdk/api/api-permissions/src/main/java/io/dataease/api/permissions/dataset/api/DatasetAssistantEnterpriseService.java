package io.dataease.api.permissions.dataset.api;

import java.util.List;
import java.util.Map;

/**
 * Enterprise (xpack) datasource query service for SQLBot assistant.
 * Implemented in de-xpack permissions module using v3's per_permission table
 * and V3UserUtil for user context.
 * <p>
 * Injected into core-backend via {@code @Autowired(required = false)};
 * available only when running with the distributed (xpack) profile.
 */
public interface DatasetAssistantEnterpriseService {

    /**
     * Query datasources + datasets that the current user has permission for,
     * filtered by optional dsId / datasetId.
     */
    List<Map<String, Object>> queryEnterprise(Long dsId, Long datasetId);

    /**
     * Query role info (id, readonly, pid) for the current user.
     */
    List<Map<String, Object>> queryUserRoles();
}
