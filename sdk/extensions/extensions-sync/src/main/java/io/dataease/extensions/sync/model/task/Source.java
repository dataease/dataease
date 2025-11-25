package io.dataease.extensions.sync.model.task;

import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceDTO;
import lombok.Data;

import java.util.List;


/**
 * @author fit2cloud
 **/
@Data
public class Source {
    private String type;
    private String query;
    private String tables;
    private DatasourceDTO datasource;
    private String datasourceId;
    private String tableExtract;
    private List<TableFieldDTO> fieldList;
    private String incrementField;
    private String esQuery;

    public Source() {
    }

    public Source(Source source) {
        this.type = source.type;
        this.query = source.query;
        this.tables = source.tables;
        this.datasource = source.datasource;
        this.datasourceId = source.datasourceId;
        this.tableExtract = source.tableExtract;
        this.fieldList = source.fieldList;
        this.incrementField = source.incrementField;
        this.esQuery = source.esQuery;
    }
}
