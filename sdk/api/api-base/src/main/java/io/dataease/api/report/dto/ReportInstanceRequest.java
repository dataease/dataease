package io.dataease.api.report.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ReportInstanceRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6928403022356816279L;

    private Long taskId;

    private List<Integer> execStatusList;

    private List<Long> timeList;

    private String keyword;

    private Boolean timeDesc;
}
