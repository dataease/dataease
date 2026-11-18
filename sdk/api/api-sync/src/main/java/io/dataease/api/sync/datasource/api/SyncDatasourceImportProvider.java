package io.dataease.api.sync.datasource.api;

import io.dataease.api.sync.datasource.dto.SyncDatasourceImport;

/** Internal extension point; never expose the returned connection configuration over HTTP. */
public interface SyncDatasourceImportProvider {
    /** Authorize the current user and organization, and load a supported target connection. */
    SyncDatasourceImport loadAuthorized(String sourceId);
}
