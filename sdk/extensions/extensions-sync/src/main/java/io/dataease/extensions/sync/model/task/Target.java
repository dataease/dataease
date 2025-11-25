package io.dataease.extensions.sync.model.task;

import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceDTO;
import lombok.Data;

import java.util.List;

/**
 * @author fit2cloud
 **/
@Data
public class Target {
    private String type;
    private String createTable;
    private List<TableFieldDTO> fieldList;
    private String tableName;
    private DatasourceDTO datasource;
    private String datasourceId;
    private String targetProperty;
    private String incrementSync;
    private String incrementField;
    private String datasourceVersion;
    private double faultToleranceRate;
    private String incrementFieldType;
    private String remarks;
    private Long incrementOffset;
    private String incrementOffsetUnit;

    public Target() {
    }

    public Target(Target target) {
        this.type = target.type;
        this.createTable = target.createTable;
        this.fieldList = target.fieldList;
        this.tableName = target.tableName;
        this.datasource = target.datasource;
        this.datasourceId = target.datasourceId;
        this.targetProperty = target.targetProperty;
        this.incrementSync = target.incrementSync;
        this.incrementField = target.incrementField;
        this.datasourceVersion = target.datasourceVersion;
        this.faultToleranceRate = target.faultToleranceRate;
        this.incrementFieldType = target.incrementFieldType;
        this.remarks = target.remarks;
        this.incrementOffset = target.incrementOffset;
        this.incrementOffsetUnit = target.incrementOffsetUnit;
    }
}
