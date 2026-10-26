package io.dataease.api.report.vo;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

@Data
public class ReportAttemptVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long artifactId;
    private Integer triggerType;
    private Integer attemptNo;
    private Integer status;
    private String stage;
    private String errorCode;
    private String errorMessage;
    private String operatorName;
    private Long createTime;
    private Long endTime;
}
