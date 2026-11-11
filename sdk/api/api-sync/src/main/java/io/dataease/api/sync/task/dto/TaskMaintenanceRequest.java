package io.dataease.api.sync.task.dto;

import io.dataease.extensions.sync.model.SinkMaintenance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段调整只提交最终差异，不接收整份任务或调度配置
 */
@Data
public class TaskMaintenanceRequest {
    @NotBlank
    @Size(max = 255)
    private String taskId;
    /**
     * 当前提交的幂等标识，配合 taskId 确定操作，不要求不同任务之间全局唯一
     */
    @NotBlank
    @Size(max = 64)
    private String operationId;
    /**
     * 分别对应已保存配置、实际结构、来源元数据和预检查计划，正式应用时重新核对
     */
    private String configFingerprint;
    private String schemaFingerprint;
    private String sourceFingerprint;
    private String planFingerprint;
    @NotNull
    @Size(max = 2048)
    private List<SinkMaintenance.Change> changes = new ArrayList<>();
    private boolean confirmed;
    private boolean deleteConfirmed;
    private boolean exclusiveTargetConfirmed;
    private boolean riskConfirmed;
    /**
     * 仅将历史未指定的数值精度校准为已读取的实际值，不修改目标表
     */
    private boolean alignTargetPrecision;
}
