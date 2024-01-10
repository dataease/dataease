package io.dataease.api.sync.task.dto;

import io.dataease.api.sync.datasource.dto.SyncDatasourceDTO;
import lombok.Data;

import java.util.List;


/**
 * @author fit2cloud
 * @date 2023/8/10 16:38
 **/
@Data
public class Source {
    private String type;
    private String query;
    private String tables;
    private SyncDatasourceDTO datasource;
    private String datasourceId;
    private String tableExtract;
    private List<TableField> fieldList;
    private String incrementField;
}
