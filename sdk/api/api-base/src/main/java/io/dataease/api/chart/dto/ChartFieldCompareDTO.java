package io.dataease.api.chart.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ChartFieldCompareDTO {
    private String type = "none";
    private String resultData = "percent";
    @JsonSerialize(using = ToStringSerializer.class)
    private Long field;
    private ChartFieldCompareCustomDTO custom;
}
