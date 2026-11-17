package io.dataease.extensions.sync.model.datasource;

import lombok.Data;

/**
 * @author jianneng
 */
@Data
public class DatasourceRequest extends DatasourceDTO {
    private String query;
    private String table;
    private Integer fetchSize = 10000;
    private boolean tableExtract;
    private String targetDbId;
    /**
     * 修改关联数据源连接前，用户已确认后续任务执行的影响
     */
    private boolean connectionChangeConfirmed;

    public DatasourceRequest() {

    }

    public DatasourceRequest(String id) {
        this.setId(id);
    }
}
