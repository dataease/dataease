package io.dataease.dto.chart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ForecastDataDTO {
    @JsonIgnore
    private double xVal;
    @JsonIgnore
    private double yVal;
    private double lower;
    private double upper;
}
