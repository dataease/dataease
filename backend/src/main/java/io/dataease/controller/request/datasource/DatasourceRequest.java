package io.dataease.controller.request.datasource;

import io.dataease.base.domain.Datasource;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DatasourceRequest {
    protected String query;
    protected String table;
    protected Datasource datasource;
    private Integer pageSize;
    private Integer page;
    private Integer realSize;
    private Integer fetchSize = 10000;
    private boolean pageable = false;
    private boolean previewData = false;


}
