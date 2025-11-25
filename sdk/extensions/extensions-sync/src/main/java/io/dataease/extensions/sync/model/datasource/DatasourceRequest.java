package io.dataease.extensions.sync.model.datasource;

import lombok.Data;

/**
 * @author jianneng
 */
@Data
public class DatasourceRequest extends DatasourceDTO {
    private String query;
    private String table;
    private boolean tableExtract;
    private String targetDbId;

    public DatasourceRequest() {

    }

    public DatasourceRequest(String id) {
        this.setId(id);
    }
}
