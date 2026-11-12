package io.dataease.api.sync.task.vo;

import io.dataease.extensions.sync.model.SinkMaintenance;
import io.dataease.extensions.sync.model.TableFieldDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 单轮字段变更记录，只返回字段元数据和实际执行结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskFieldChangeLogVO extends TaskEventLogVO {
    private String operationId;
    private String operatorName;
    private String targetTable;
    private String beforeSourceQuery;
    private String afterSourceQuery;
    private List<TableFieldDTO> beforeFields;
    private List<TableFieldDTO> afterFields;
    private List<SinkMaintenance.Step> progress;
    private String error;
    private String initializationLogId;
    private String initializationStatus;
    private String initializationError;
    private String successorLogId;
}
