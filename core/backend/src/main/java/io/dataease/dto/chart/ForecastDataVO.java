package io.dataease.dto.chart;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForecastDataVO<D, Q> extends ForecastDataDTO {
    private D dimension;
    private Q quota;
}
