package io.dataease.api.chart.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ViewSelectorVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7163837502596313691L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private String type;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;
}
