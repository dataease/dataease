package io.dataease.api.report.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportInstanceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7845204306008848053L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Long startTime;

    private Integer execStatus;
}
