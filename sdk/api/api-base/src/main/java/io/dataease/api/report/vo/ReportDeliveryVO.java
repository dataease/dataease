package io.dataease.api.report.vo;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

@Data
public class ReportDeliveryVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;
    private String taskName;
    private String resourceName;
    private Integer format;
    private Integer execStatus;
    private Integer triggerType;
    private Long startTime;
    private Long endTime;
    private Boolean legacy;
    private Boolean current;
    // Availability of the original report copies, independent of the initial send result.
    private String snapshotState;
    private String log;
    private List<ReportRecipientVO> recipients;
}
