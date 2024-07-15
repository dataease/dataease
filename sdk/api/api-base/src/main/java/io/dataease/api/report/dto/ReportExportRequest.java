package io.dataease.api.report.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportExportRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5372551595882128201L;

    private Long resourceId;

    private String busiType;

    private String pixel;

    private Integer extWaitTime = 0;

    private Integer resultFormat = 0;
}
