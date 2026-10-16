package io.dataease.api.report.vo;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

@Data
public class ReportRecipientVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String recipientName;
    private String recipientAddress;
    private Integer channel;
    private Integer initialStatus;
    private Integer currentStatus;
    private Integer executionAction;
    private String initialStage;
    private String initialErrorCode;
    private String initialErrorMessage;
    private String retryBlock;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long artifactId;
    private String title;
    private String filter;
    private Long generatedTime;
    private List<String> files;
    private List<ReportAttemptVO> attempts;
}
