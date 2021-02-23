package io.dataease.datasource.request;

import io.dataease.base.domain.Datasource;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;

@Getter
@Setter
public class DatasourceRequest {
    protected String query;
    protected String table;
    protected Datasource datasource;

}
