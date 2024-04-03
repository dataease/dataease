package io.dataease.api.sync.task.dto;

import io.dataease.api.sync.datasource.dto.SyncDatasourceDTO;
import lombok.Data;

import java.util.List;

/**
 * @author fit2cloud
 * @date 2023/8/10 16:39
 **/
@Data
public class Target {
    private String type;
    private String createTable;
    private List<TableField> fieldList;
    private String tableName;
    private SyncDatasourceDTO datasource;
    private String datasourceId;
    private String targetProperty;
    private String incrementSync;
    private String incrementField;
    private String datasourceVersion;
    private double faultToleranceRate;
}
