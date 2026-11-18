package io.dataease.api.sync.datasource.dto;

import lombok.Getter;
import lombok.Setter;

/** Server-only configuration snapshot. Deliberately has no generated toString(). */
@Getter
@Setter
public class SyncDatasourceImport {
    private String type;
    private String description;
    private String configuration;
}
