package io.dataease.api.chart.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ChartBaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2445689467486374464L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long chartId;

    private String chartType;

    private String chartName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    private String resourceType;

    private String resourceName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tableId;
}
