package io.dataease.api.report.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportInstanceMsgRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7192222037770564561L;

    private Long taskId;

    private Long instanceId;
}
