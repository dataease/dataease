package io.dataease.api.sync.task.vo;

import io.dataease.extensions.sync.model.SinkMaintenance;
import io.dataease.extensions.sync.model.TableFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 字段调整上下文和当前操作结果，禁止包含数据源配置
 */
@Data
public class TaskMaintenanceVO {
    private String taskId;
    private String taskName;
    private String sourceName;
    private String targetName;
    private String targetTable;
    private String sourceType;
    private String targetType;
    private String sourceMode;
    private String sourceTable;
    private String sourceQuery;
    private boolean sourceQuerySupported;
    private String beforeSourceQuery;
    private String afterSourceQuery;
    private String operationId;
    private String state;
    private String phase;
    /**
     * 共享运行归属是否仍在推进字段调整，不以查看页面所在节点的本地线程判断其他节点状态
     */
    private Boolean processing;
    private String blockedReason;
    private String blockedCode;
    private String error;
    private String logId;
    private String configFingerprint;
    private String schemaFingerprint;
    private String sourceFingerprint;
    private String planFingerprint;
    /**
     * fields 是当前任务保存配置；beforeFields 和 afterFields 是本次操作意图，不能混作当前生效配置
     */
    private List<TableFieldDTO> fields;
    private List<TableFieldDTO> beforeFields;
    private List<TableFieldDTO> afterFields;
    private boolean structureVerified;
    private String initializationStatus;
    private String initializationError;
    private List<TableFieldDTO> sourceFields;
    private List<String> fieldTypes;
    private Map<String, String> mappingNotes;
    private SinkMaintenance.Structure structure;
    private SinkMaintenance.Plan plan;
    private List<SinkMaintenance.Step> progress;
    private TaskExecutionRecoveryVO executionRecovery;
}
