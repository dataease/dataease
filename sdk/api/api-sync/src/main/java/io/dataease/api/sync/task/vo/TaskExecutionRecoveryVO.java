package io.dataease.api.sync.task.vo;

import lombok.Data;

/**
 * 展示执行或替表的核对进度和人工处理入口，不传输数据库凭据或内部归属令牌
 */
@Data
public class TaskExecutionRecoveryVO {
    private String logId;
    private String status;
    private String kind;
    private String reason;
    private String configFingerprint;
    private Long lastChecked;
    private int checks;
    private boolean manualRequired;
    private boolean canResolve;
}
