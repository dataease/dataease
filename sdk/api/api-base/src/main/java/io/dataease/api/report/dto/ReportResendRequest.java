package io.dataease.api.report.dto;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

@Data
public class ReportResendRequest {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;
    private List<Long> recipientIds;
    private String idempotencyKey;
    private Boolean confirmUnknown;
}
