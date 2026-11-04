package io.dataease.api.sync.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 人工核实只结束指定的运行，不接收任意结构或数据库语句
 */
@Data
public class TaskExecutionRecoveryRequest {
    @NotBlank
    @Size(max = 255)
    private String taskId;
    @NotBlank
    @Size(max = 255)
    private String logId;
    private String configFingerprint;
    @Pattern(regexp = "SUCCESS|FAIL")
    private String result;
    @Size(max = 2000)
    private String note;
    private boolean confirmed;
}
