package io.dataease.api.report.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportGridVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5178055146669970633L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Long lastExecTime;

    private Integer lastExecStatus;

    private Integer status;

    private Long nextExecTime;

    private String creator;

    private Long createTime;
}
