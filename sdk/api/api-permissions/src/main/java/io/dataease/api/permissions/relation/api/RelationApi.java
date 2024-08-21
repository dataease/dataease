package io.dataease.api.permissions.relation.api;

import io.dataease.exception.DEException;

/**
 * @Author Junjun
 */
public interface RelationApi {
    Long getDsResource(Long id);

    Long getDatasetResource(Long id);

    void checkAuth() throws DEException;
}
