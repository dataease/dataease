package io.dataease.api.report.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReportEditor extends ReportCreator{

    @Serial
    private static final long serialVersionUID = 4580864230335912932L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

}
